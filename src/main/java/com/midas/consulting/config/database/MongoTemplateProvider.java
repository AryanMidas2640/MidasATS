package com.midas.consulting.config.database;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.midas.consulting.exception.DatabaseConnectionException;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.service.ConnectionMetricsService;
import com.midas.consulting.service.TenantContext;
import com.midas.consulting.service.TenantService;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MongoTemplateProvider {


    @Value("${mongodb.base.property}")
    private String mongoBaseConnection;
    private static final Logger logger = LoggerFactory.getLogger(MongoTemplateProvider.class);

    // Enhanced cache with better stats and eviction handling
    private static final Cache<String, MongoTemplate> templateCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .recordStats()
            .removalListener((String key, MongoTemplate value, com.github.benmanes.caffeine.cache.RemovalCause cause) -> {
                logger.info("MongoTemplate cache entry removed: key={}, cause={}", key, cause);
            })
            .build();

    private static final ConcurrentMap<String, RetryableMongoTemplate> retryableTemplateCache = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, MongoClient> mongoClientCache = new ConcurrentHashMap<>();

    @Autowired
    private ConnectionMetricsService metricsService;

    @Autowired
    private TenantService tenantService;

    @Autowired
    @Lazy
    private Cache<String, Tenant> tenantCache;

    @Autowired
    private ApplicationContext applicationContext;

    // Enhanced connection string with better timeout settings
    @Value("${mongodb.base.property}")
    private  String DEFAULT_CONNECTION_STRING ;
//            "mongodb://admin:demonUser@192.168.1.35:27017/hrms-onprime" +
//                    "?authSource=admin" +
//                    "&maxPoolSize=50" +
//                    "&minPoolSize=10" +
//                    "&waitQueueTimeoutMS=5000" +        // Increased from 3000
//                    "&socketTimeoutMS=60000" +          // 60 second socket timeout
//                    "&connectTimeoutMS=10000" +         // 10 second connection timeout
//                    "&serverSelectionTimeoutMS=5000" +  // 5 second server selection timeout
//                    "&maxIdleTimeMS=120000" +           // 2 minute idle timeout
//                    "&heartbeatFrequencyMS=10000" +     // 10 second heartbeat
//                    "&retryWrites=true" +               // Enable retry writes
//                    "&retryReads=true";                 // Enable retry reads

    // Circuit breaker for tenant lookups
    private final AtomicBoolean tenantLookupHealthy = new AtomicBoolean(true);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private static final long CIRCUIT_RESET_TIME_MS = 60000; // 1 minute
    private static final int FAILURE_THRESHOLD = 3;
    private static final int DB_TIMEOUT_SECONDS = 5;

    // Your existing methods remain unchanged
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        CacheStats templateStats = templateCache.stats();
        stats.put("templateCacheSize", templateCache.estimatedSize());
        stats.put("templateCacheHitCount", templateStats.hitCount());
        stats.put("templateCacheMissCount", templateStats.missCount());
        stats.put("templateCacheHitRate", templateStats.hitRate());
        stats.put("templateCacheEvictionCount", templateStats.evictionCount());
        return stats;
    }

    public MongoTemplate refreshMongoTemplate(String tenantId) {
        Tenant tenant = fetchConnectionStringFromDB(tenantId);
        if (tenant != null && tenant.getConnectionString() != null) {
            String connectionString = tenant.getConnectionString();
            templateCache.invalidate(connectionString);
            return templateCache.get(connectionString, this::createMongoTemplate);
        }
        throw new DatabaseConnectionException(tenantId, "Cannot refresh connection: Tenant not found or connection string not available");
    }

    public RetryableMongoTemplate getRetryableMongoTemplate() {
        MongoTemplate mongoTemplate = getMongoTemplate();
        String cacheKey = mongoTemplate.toString();
        return retryableTemplateCache.computeIfAbsent(cacheKey,
                k -> new RetryableMongoTemplate(mongoTemplate, 3, 1000));
    }

    // Enhanced tenant lookup with circuit breaker (your existing method enhanced)
    private Tenant fetchConnectionStringFromDB(String tenantId) {
        logger.info("Fetching connection string from DB for tenant: {}", tenantId);

        // Check if circuit is open
        if (!tenantLookupHealthy.get()) {
            long timeSinceFailure = System.currentTimeMillis() - lastFailureTime.get();
            if (timeSinceFailure < CIRCUIT_RESET_TIME_MS) {
                logger.warn("Circuit breaker open for tenant lookups. Using default tenant. Time remaining: {}ms",
                        CIRCUIT_RESET_TIME_MS - timeSinceFailure);
                return createDefaultTenant(tenantId);
            } else {
                logger.info("Circuit breaker timeout elapsed. Moving to half-open state for tenant lookups");
                consecutiveFailures.set(0);
            }
        }

        CompletableFuture<Tenant> future = CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Creating default MongoTemplate to fetch tenant");
                MongoTemplate mongoTemplate = createMongoTemplate(DEFAULT_CONNECTION_STRING);

                Query query = new Query(Criteria.where("_id").is(tenantId));
                // Add timeout to the query itself
                query.maxTime(DB_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                logger.info("Executing find query for tenant: {}", tenantId);
                Tenant tenant = mongoTemplate.findOne(query, Tenant.class, "tenant");

                logger.info("Query completed. Tenant found: {}", tenant != null);
                consecutiveFailures.set(0);
                tenantLookupHealthy.set(true);
                return tenant;
            } catch (Exception e) {
                int failures = consecutiveFailures.incrementAndGet();
                lastFailureTime.set(System.currentTimeMillis());

                if (failures >= FAILURE_THRESHOLD) {
                    tenantLookupHealthy.set(false);
                    logger.error("Opening circuit breaker due to {} consecutive tenant lookup failures", failures);
                }

                logger.error("Error in tenant lookup: {}", e.getMessage(), e);
                return createDefaultTenant(tenantId);
            }
        });

        try {
            return future.get(DB_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Timeout occurred while fetching tenant: {}", tenantId);
            int failures = consecutiveFailures.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());

            if (failures >= FAILURE_THRESHOLD) {
                tenantLookupHealthy.set(false);
                logger.error("Opening circuit breaker due to {} consecutive tenant lookup failures (timeout)", failures);
            }

            future.cancel(true);
            return createDefaultTenant(tenantId);
        } catch (Exception e) {
            logger.error("Error fetching tenant: {}", e.getMessage(), e);
            int failures = consecutiveFailures.incrementAndGet();
            lastFailureTime.set(System.currentTimeMillis());

            if (failures >= FAILURE_THRESHOLD) {
                tenantLookupHealthy.set(false);
                logger.error("Opening circuit breaker due to {} consecutive tenant lookup failures (exception)", failures);
            }

            return createDefaultTenant(tenantId);
        }
    }

    private Tenant createDefaultTenant(String tenantId) {
        logger.info("Creating default tenant for ID: {}", tenantId);
        Tenant defaultTenant = new Tenant();
        defaultTenant.setId(tenantId);
        defaultTenant.setConnectionString(DEFAULT_CONNECTION_STRING);
        defaultTenant.setTenantName("Default");
        return defaultTenant;
    }

    public Tenant getConnectionString(String tenantId) {
        logger.info("Getting connection string for tenant: {}", tenantId);
        Tenant tenant = tenantCache.get(tenantId, this::fetchConnectionStringFromDB);
        logger.info("Retrieved tenant for {}: {}", tenantId, tenant != null ? "Found" : "Not found");
        return tenant;
    }

    // Enhanced main method with better error handling and timeouts
    public MongoTemplate getMongoTemplate() {
        String tenantId = TenantContext.getCurrentTenant();
        logger.info("getMongoTemplate called with tenantId: {}", tenantId);

        if (tenantId != null) {
            try {
                metricsService.recordConnectionRequest(tenantId);

                // Set a timeout for getting the connection string
                Tenant tenant = getConnectionString(tenantId);

                if (tenant == null) {
                    logger.warn("Tenant not found for ID: {}, using default connection", tenantId);
                    return getFallbackTemplate();
                }

                String connectionValue = tenant.getConnectionString();
                logger.info("Connection string for tenant {}: {}", tenantId, connectionValue != null ? "Found" : "Not found");

                if (connectionValue != null && !connectionValue.isEmpty()) {
                    try {
                        // Add timeout for template creation with enhanced error handling
                        CompletableFuture<MongoTemplate> future = CompletableFuture.supplyAsync(() -> {
                            return templateCache.get(connectionValue, connString -> {
                                metricsService.recordConnectionCreation(tenantId);
                                logger.info("Creating new MongoTemplate for tenant: {}", tenantId);
                                return createMonitoredMongoTemplate(connectionValue, tenantId);
                            });
                        });

                        return future.get(10, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        logger.error("Timeout creating template for tenant {}: {}", tenantId, e.getMessage());
                        return getFallbackTemplate();
                    } catch (Exception e) {
                        logger.error("Error creating template for tenant {}: {}", tenantId, e.getMessage(), e);
                        return getFallbackTemplate();
                    }
                }

                logger.warn("Connection string is null or empty for tenant: {}", tenantId);
                return getFallbackTemplate();
            } catch (Exception e) {
                logger.error("Error in getMongoTemplate: {}", e.getMessage(), e);
                return getFallbackTemplate();
            }
        }

        return getFallbackTemplate();
    }

    private MongoTemplate getFallbackTemplate() {
        logger.info("Using fallback/default connection");
        return templateCache.get(DEFAULT_CONNECTION_STRING, connString -> {
            metricsService.recordConnectionCreation("default");
            return createMonitoredMongoTemplate(DEFAULT_CONNECTION_STRING, "default");
        });
    }

    // Enhanced MongoClient creation with better connection pooling
    private MongoClient createPooledMongoClient(String connectionString) {
        logger.info("Starting to create pooled MongoClient");
        try {
            ConnectionString connString = new ConnectionString(connectionString);
            logger.info("Parsed connection string successfully");

            logger.info("Building connection settings with enhanced timeouts");
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(10, TimeUnit.SECONDS)    // Increased from 5
                                    .readTimeout(60, TimeUnit.SECONDS))       // Increased from 10
                    .applyToConnectionPoolSettings(builder -> builder
                            .maxSize(50)
                            .minSize(10)
                            .maxWaitTime(5, TimeUnit.SECONDS)                // Reduced wait time
                            .maxConnectionIdleTime(2, TimeUnit.MINUTES)     // Connection idle time
                            .maxConnectionLifeTime(10, TimeUnit.MINUTES))   // Connection lifetime
                    .applyToServerSettings(builder ->
                            builder.heartbeatFrequency(10, TimeUnit.SECONDS)
                                    .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS))
                    .build();

            logger.info("Settings built, creating client");
            MongoClient client = MongoClients.create(settings);
            logger.info("Client created successfully");
            return client;
        } catch (Exception e) {
            logger.error("Error creating pooled MongoClient: {}", e.getMessage(), e);
            throw e;
        }
    }

    // Enhanced createMonitoredMongoTemplate
    private MongoTemplate createMonitoredMongoTemplate(String connectionString, String tenantId) {
        logger.info("DEBUG: Starting createMonitoredMongoTemplate for tenant: {}", tenantId);

        try {
            ConnectionString connString = new ConnectionString(connectionString);
            logger.info("DEBUG: About to create pooled MongoClient");

            MongoClient mongoClient = createPooledMongoClient(connectionString);
            logger.info("DEBUG: MongoClient created successfully");

            logger.info("DEBUG: About to create MongoTemplate");

            // Create and return the monitored template with enhanced error handling
            MongoTemplate template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, extractDatabaseName(connectionString))) {
                // You can add custom behavior here if needed
            };

            // Test the connection
            try {
                template.getCollectionNames();
                logger.info("MongoTemplate connection test successful for tenant: {}", tenantId);
            } catch (Exception e) {
                logger.warn("MongoTemplate connection test failed for tenant: {}, but template created: {}", tenantId, e.getMessage());
            }

            return template;
        } catch (Exception e) {
            logger.error("Error creating MongoTemplate for tenant {}: {}", tenantId, e.getMessage(), e);
            throw e;
        }
    }

    // Your existing createMongoTemplate method (keeping for compatibility)
    private MongoTemplate createMongoTemplate(String connectionString) {
        logger.info("Creating MongoTemplate for connection string: {}", maskConnectionString(connectionString));

        try {
            boolean clientExists = mongoClientCache.containsKey(connectionString);
            logger.info("MongoClient for this connection {} in cache", clientExists ? "exists" : "does not exist");

            MongoClient mongoClient = null;
            try {
                mongoClient = mongoClientCache.computeIfAbsent(connectionString, connString -> {
                    logger.info("Creating new pooled MongoClient for connection");
                    return createPooledMongoClient(connString);
                });
            } catch (Exception ee) {
                logger.error("Error creating MongoClient: {}", ee.getMessage(), ee);
                throw ee;
            }

            String databaseName = extractDatabaseName(connectionString);
            logger.info("Extracted database name: {}", databaseName);

            if (databaseName == null) {
                logger.error("Failed to extract database name from connection string");
                throw new IllegalArgumentException("Could not extract database name from connection string");
            }

            logger.info("Creating new MongoTemplate with database: {}", databaseName);
            MongoTemplate template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, databaseName));
            logger.info("Successfully created MongoTemplate");

            return template;
        } catch (Exception e) {
            logger.error("Error creating MongoTemplate: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create MongoTemplate", e);
        }
    }

    // Enhanced diagnostics
    public void diagnoseMongoConnection() {
        logger.info("Starting enhanced MongoDB connection diagnosis");

        try {
            logger.info("Creating test connection to MongoDB");
            MongoClient client = MongoClients.create(DEFAULT_CONNECTION_STRING);

            logger.info("Testing database listing");
            client.listDatabaseNames().forEach(name -> logger.info("Found database: {}", name));

            logger.info("Testing tenant collection access");
            MongoTemplate template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(client, extractDatabaseName(DEFAULT_CONNECTION_STRING)));

            // Test with multiple timeouts
            Query query = new Query();
            query.maxTime(5000, TimeUnit.MILLISECONDS);
            long count = template.count(query, Tenant.class);
            logger.info("Tenant collection has {} documents", count);

            // Test a simple find operation
            Query findQuery = new Query().limit(1);
            findQuery.maxTime(3000, TimeUnit.MILLISECONDS);
            Tenant testTenant = template.findOne(findQuery, Tenant.class);
            logger.info("Test query returned: {}", testTenant != null ? "success" : "no results");

            client.close();
            logger.info("Enhanced MongoDB connection diagnosis completed successfully");
        } catch (Exception e) {
            logger.error("Enhanced MongoDB connection diagnosis failed: {}", e.getMessage(), e);
        }
    }

    // Your existing utility methods remain the same
    private void logConnectionActivity(String tenantId, String activity) {
        logger.info("Tenant [{}] connection {}", tenantId, activity);
    }

    private String extractDatabaseName(String connectionString) {
        logger.debug("Extracting database name from: {}", maskConnectionString(connectionString));

        String regex = "mongodb://.*?/(.*?)(\\?|$)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(connectionString);

        if (matcher.find()) {
            String databaseName = matcher.group(1);
            logger.debug("Extracted database name: {}", databaseName);
            return databaseName;
        }

        logger.warn("Failed to extract database name from connection string: {}", maskConnectionString(connectionString));
        return null;
    }

    private String maskConnectionString(String connectionString) {
        return connectionString.replaceAll("://([^:]+):([^@]+)@", "://*****:*****@");
    }

    // Enhanced shutdown with better cleanup
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down MongoDB connections");

        // Close all cached mongo clients
        mongoClientCache.forEach((connStr, client) -> {
            try {
                logger.info("Closing MongoDB client for connection: {}", maskConnectionString(connStr));
                client.close();
            } catch (Exception e) {
                logger.warn("Error closing MongoDB client: {}", e.getMessage());
            }
        });

        mongoClientCache.clear();
        templateCache.cleanUp();
        retryableTemplateCache.clear();

        logger.info("All MongoDB connections closed successfully");
    }

    // Add health check method
    public boolean isHealthy() {
        try {
            MongoTemplate template = getFallbackTemplate();
            template.getCollectionNames();
            return true;
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    // Add method to get connection pool statistics
    public Map<String, Object> getConnectionPoolStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCachedClients", mongoClientCache.size());
        stats.put("templateCacheStats", getCacheStatistics());
        stats.put("circuitBreakerHealthy", tenantLookupHealthy.get());
        stats.put("consecutiveFailures", consecutiveFailures.get());
        stats.put("lastFailureTime", lastFailureTime.get());
        return stats;
    }
}
//package com.midas.consulting.config.database;
//
//import com.github.benmanes.caffeine.cache.Cache;
//import com.github.benmanes.caffeine.cache.Caffeine;
//import com.github.benmanes.caffeine.cache.stats.CacheStats;
//import com.midas.consulting.exception.DatabaseConnectionException;
//import com.midas.consulting.model.Tenant;
//import com.midas.consulting.service.ConnectionMetricsService;
//import com.midas.consulting.service.TenantContext;
//import com.midas.consulting.service.TenantService;
//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.PreDestroy;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.concurrent.atomic.AtomicLong;
//
//@Component
//public class MongoTemplateProvider {
//    private static final Logger logger = LoggerFactory.getLogger(MongoTemplateProvider.class);
//    private static final Cache<String, MongoTemplate> templateCache = Caffeine.newBuilder()
//            .maximumSize(100)
//            .expireAfterAccess(30, TimeUnit.MINUTES)
//            .recordStats()
//            .removalListener((String key, MongoTemplate value, com.github.benmanes.caffeine.cache.RemovalCause cause) -> {
//                logger.info("MongoTemplate cache entry removed: key={}, cause={}", key, cause);
//            })
//            .build();
//
//    private static final ConcurrentMap<String, RetryableMongoTemplate> retryableTemplateCache = new ConcurrentHashMap<>();
//
//
//
//    // Add a method to get cache statistics
//    public Map<String, Object> getCacheStatistics() {
//        Map<String, Object> stats = new HashMap<>();
//
//        CacheStats templateStats = templateCache.stats();
//        stats.put("templateCacheSize", templateCache.estimatedSize());
//        stats.put("templateCacheHitCount", templateStats.hitCount());
//        stats.put("templateCacheMissCount", templateStats.missCount());
//        stats.put("templateCacheHitRate", templateStats.hitRate());
//        stats.put("templateCacheEvictionCount", templateStats.evictionCount());
//
//        return stats;
//    }
//
//    // Add a method to manually refresh the template for a tenant
//    public MongoTemplate refreshMongoTemplate(String tenantId) {
//        Tenant tenant = fetchConnectionStringFromDB(tenantId);
//
//        if (tenant != null && tenant.getConnectionString() != null) {
//            String connectionString = tenant.getConnectionString();
//
//            // Remove existing template from cache
//            templateCache.invalidate(connectionString);
//
//            // Create and cache a new template
//            return templateCache.get(connectionString, this::createMongoTemplate);
//        }
//
//        throw new DatabaseConnectionException(tenantId, "Cannot refresh connection: Tenant not found or connection string not available");
//    }
//    @Autowired
//    private ConnectionMetricsService metricsService;
//    @Autowired
//    private TenantService tenantService;
//
//
//
////    private static final String DEFAULT_CONNECTION_STRING =
////            "mongodb://admin:DBSH5463XSDDjd@157.20.215.191:27017/hrms-onprime?authSource=admin&maxPoolSize=10000&minPoolSize=10&waitQueueTimeoutMS=3000";
////
//
//    private static final String DEFAULT_CONNECTION_STRING =
//            "mongodb://admin:DBSH5463XSDDjd@157.20.215.191:27017/hrms-onprime" +
//                    "?authSource=admin" +
//                    "&maxPoolSize=50" +
//                    "&minPoolSize=10" +
//                    "&waitQueueTimeoutMS=3000" +
//                    "&socketTimeoutMS=60000" +      // 60 second socket timeout
//                    "&connectTimeoutMS=10000" +     // 10 second connection timeout
//                    "&serverSelectionTimeoutMS=5000" + // 5 second server selection timeout
//                    "&maxIdleTimeMS=120000" +       // 2 minute idle timeout
//                    "&heartbeatFrequencyMS=10000";  // 10 second heartbeat
////    spring.data.mongodb.socket-timeout=60000
////    spring.data.mongodb.connect-timeout=30000
////    spring.data.mongodb.server-selection-timeout=30000
////    spring.data.mongodb.max-wait-time=120000
//
////    admin:MidasAdmin22111@150.241.245.112:27017/
////    private static final String DEFAULT_CONNECTION_STRING =
////            "mongodb://admin:MidasAdmin22111@150.241.245.112:27017/hrms-onprime?authSource=admin&maxPoolSize=50&minPoolSize=10&waitQueueTimeoutMS=5000&socketTimeoutMS=10000&connectTimeoutMS=5000";
//
//    public RetryableMongoTemplate getRetryableMongoTemplate() {
//        MongoTemplate mongoTemplate = getMongoTemplate();
//        String cacheKey = mongoTemplate.toString();
//
//        return retryableTemplateCache.computeIfAbsent(cacheKey,
//                k -> new RetryableMongoTemplate(mongoTemplate, 3, 1000));
//    }
//    private void logConnectionActivity(String tenantId, String activity) {
//        logger.info("Tenant [{}] connection {}", tenantId, activity);
//    }
//    private MongoClient createPooledMongoClient(String connectionString) {
//        logger.info("Starting to create pooled MongoClient");
//        try {
//            ConnectionString connString = new ConnectionString(connectionString);
//            logger.info("Parsed connection string successfully");
//
//            // Log each step
//            logger.info("Building connection settings");
//            MongoClientSettings settings = MongoClientSettings.builder()
//                    .applyConnectionString(connString)
//                    .applyToSocketSettings(builder ->
//                            builder.connectTimeout(5000, TimeUnit.MILLISECONDS)
//                                    .readTimeout(10000, TimeUnit.MILLISECONDS))
//                    .applyToConnectionPoolSettings(builder -> builder
//                            .maxSize(50)
//                            .minSize(10)
//                            .maxWaitTime(10000, TimeUnit.MILLISECONDS))
//                    .build();
//
//            logger.info("Settings built, creating client");
//            MongoClient client = MongoClients.create(settings);
//            logger.info("Client created successfully");
//            return client;
//        } catch (Exception e) {
//            logger.error("Error creating pooled MongoClient: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
//    private static final ConcurrentMap<String, MongoClient> mongoClientCache = new ConcurrentHashMap<>();
//
//    @Autowired
//    @Lazy
//    private Cache<String, Tenant> tenantCache;
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    public Tenant getConnectionString(String tenantId) {
//        logger.info("Getting connection string for tenant: {}", tenantId);
//        Tenant tenant = tenantCache.get(tenantId, this::fetchConnectionStringFromDB);
//        logger.info("Retrieved tenant for {}: {}", tenantId, tenant != null ? "Found" : "Not found");
//        return tenant;
//    }
//
////    private Tenant fetchConnectionStringFromDB(String tenantId) {
////        logger.info("Fetching connection string from DB for tenant: {}", tenantId);
////        try {
////            // Fetch connection string for the tenant from the database
////            MongoTemplate mongoTemplate = createMongoTemplate(DEFAULT_CONNECTION_STRING);
////            Tenant tenant = mongoTemplate.findById(tenantId, Tenant.class);
////            logger.info("Tenant from DB for {}: {}", tenantId, tenant != null ? "Found" : "Not found");
////            return tenant;
////        } catch (Exception e) {
////            logger.error("Error fetching tenant from DB: {}", e.getMessage(), e);
////            return null;
////        }
////    }
//// At the class level
//private final AtomicBoolean tenantLookupHealthy = new AtomicBoolean(true);
//    private final AtomicLong lastFailureTime = new AtomicLong(0);
//    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
//    private static final long CIRCUIT_RESET_TIME_MS = 60000; // 1 minute
//    private static final int FAILURE_THRESHOLD = 3; // Number of failures to trigger open circuit
//    private static final int DB_TIMEOUT_SECONDS = 5; // Timeout for database operations
//
//    private Tenant fetchConnectionStringFromDB(String tenantId) {
//        logger.info("Fetching connection string from DB for tenant: {}", tenantId);
//
//        // Check if circuit is open
//        if (!tenantLookupHealthy.get()) {
//            long timeSinceFailure = System.currentTimeMillis() - lastFailureTime.get();
//            if (timeSinceFailure < CIRCUIT_RESET_TIME_MS) {
//                logger.warn("Circuit breaker open for tenant lookups. Using default tenant. Time remaining: {}ms",
//                        CIRCUIT_RESET_TIME_MS - timeSinceFailure);
//                return createDefaultTenant(tenantId);
//            } else {
//                // Reset circuit breaker after timeout - move to half-open state
//                logger.info("Circuit breaker timeout elapsed. Moving to half-open state for tenant lookups");
//                // Don't reset health yet - we'll test with this request
//                consecutiveFailures.set(0);
//            }
//        }
//
//        CompletableFuture<Tenant> future = CompletableFuture.supplyAsync(() -> {
//            try {
//                // Create a template with the default connection string
//                logger.info("Creating default MongoTemplate to fetch tenant");
//                MongoTemplate mongoTemplate = createMongoTemplate(DEFAULT_CONNECTION_STRING);
//
//                // Use explicit Query with criteria
//                Query query = new Query(Criteria.where("_id").is(tenantId));
//
//                // Execute the query
//                logger.info("Executing find query for tenant: {}", tenantId);
//                Tenant tenant = mongoTemplate.findOne(query, Tenant.class, "tenant");
//
//                // Log results and reset failure counter on success
//                logger.info("Query completed. Tenant found: {}", tenant != null);
//                consecutiveFailures.set(0);
//                tenantLookupHealthy.set(true);
//                return tenant;
//            } catch (Exception e) {
//                int failures = consecutiveFailures.incrementAndGet();
//                lastFailureTime.set(System.currentTimeMillis());
//
//                if (failures >= FAILURE_THRESHOLD) {
//                    tenantLookupHealthy.set(false);
//                    logger.error("Opening circuit breaker due to {} consecutive tenant lookup failures", failures);
//                }
//
//                logger.error("Error in tenant lookup: {}", e.getMessage(), e);
//                return createDefaultTenant(tenantId);
//            }
//        });
//
//        try {
//            // Use the configured timeout for the database operation
//            return future.get(DB_TIMEOUT_SECONDS, TimeUnit.SECONDS);
//        } catch (TimeoutException e) {
//            logger.error("Timeout occurred while fetching tenant: {}", tenantId);
//            // Count timeouts as failures too
//            int failures = consecutiveFailures.incrementAndGet();
//            lastFailureTime.set(System.currentTimeMillis());
//
//            if (failures >= FAILURE_THRESHOLD) {
//                tenantLookupHealthy.set(false);
//                logger.error("Opening circuit breaker due to {} consecutive tenant lookup failures (timeout)", failures);
//            }
//
//            future.cancel(true);
//            return createDefaultTenant(tenantId);
//        } catch (Exception e) {
//            logger.error("Error fetching tenant: {}", e.getMessage(), e);
//            // Count other exceptions as failures too
//            int failures = consecutiveFailures.incrementAndGet();
//            lastFailureTime.set(System.currentTimeMillis());
//
//            if (failures >= FAILURE_THRESHOLD) {
//                tenantLookupHealthy.set(false);
//                logger.error("Opening circuit breaker due to {} consecutive tenant lookup failures (exception)", failures);
//            }
//
//            return createDefaultTenant(tenantId);
//        }
//    }
//    private Tenant createDefaultTenant(String tenantId) {
//        logger.info("Creating default tenant for ID: {}", tenantId);
//        Tenant defaultTenant = new Tenant();
//        defaultTenant.setId(tenantId);
//        defaultTenant.setConnectionString(DEFAULT_CONNECTION_STRING);
//        defaultTenant.setTenantName("Default");
//        return defaultTenant;
//    }
////    public Tenant getConnectionString(String tenantId) {
////        return tenantCache.get(tenantId, this::fetchConnectionStringFromDB);
////    }
////
////    private Tenant fetchConnectionStringFromDB(String tenantId) {
////        // Fetch connection string for the tenant from the database
////        MongoTemplate mongoTemplate = createMongoTemplate(DEFAULT_CONNECTION_STRING);
////        Tenant tenant = mongoTemplate.findById(tenantId, Tenant.class);
////        return tenant;
////    }
//
//
//    public void diagnoseMongoConnection() {
//        logger.info("Starting MongoDB connection diagnosis");
//
//        try {
//            // Create a simple MongoDB client
//            logger.info("Creating test connection to MongoDB");
//            MongoClient client = MongoClients.create(DEFAULT_CONNECTION_STRING);
//
//            // Test basic operations
//            logger.info("Testing database listing");
//            client.listDatabaseNames().forEach(name -> logger.info("Found database: {}", name));
//
//            // Test tenant collection
//            logger.info("Testing tenant collection access");
//            MongoTemplate template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(client, extractDatabaseName(DEFAULT_CONNECTION_STRING)));
//
//            // Count documents with timeout
//            Query query = new Query();
//            query.maxTime(3000, TimeUnit.MILLISECONDS);
//            long count = template.count(query, Tenant.class);
//            logger.info("Tenant collection has {} documents", count);
//
//            // Close client
//            client.close();
//            logger.info("MongoDB connection diagnosis completed successfully");
//        } catch (Exception e) {
//            logger.error("MongoDB connection diagnosis failed: {}", e.getMessage(), e);
//        }
//    }
//
//
//    public MongoTemplate getMongoTemplate() {
//        String tenantId = TenantContext.getCurrentTenant();
//        logger.info("getMongoTemplate called with tenantId: {}", tenantId);
//
//        if (tenantId != null) {
//            try {
//                metricsService.recordConnectionRequest(tenantId);
//
//                // Set a timeout for getting the connection string
//                Tenant tenant = getConnectionString(tenantId);
//
//                if (tenant == null) {
//                    logger.warn("Tenant not found for ID: {}, using default connection", tenantId);
//                    return getFallbackTemplate();
//                }
//
//                String connectionValue = tenant.getConnectionString();
//                logger.info("Connection string for tenant {}: {}", tenantId, connectionValue != null ? "Found" : "Not found");
//
//                if (connectionValue != null && !connectionValue.isEmpty()) {
//                    try {
//                        // Add timeout for template creation
//                        CompletableFuture<MongoTemplate> future = CompletableFuture.supplyAsync(() -> {
//                            return templateCache.get(connectionValue, connString -> {
//                                metricsService.recordConnectionCreation(tenantId);
//                                logger.info("Creating new MongoTemplate for tenant: {}", tenantId);
//                                return createMonitoredMongoTemplate(connectionValue, tenantId);
//                            });
//                        });
//
//                        return future.get(10, TimeUnit.SECONDS);
//                    } catch (Exception e) {
//                        logger.error("Error creating template for tenant {}: {}", tenantId, e.getMessage(), e);
//                        return getFallbackTemplate();
//                    }
//                }
//
//                logger.warn("Connection string is null or empty for tenant: {}", tenantId);
//                return getFallbackTemplate();
//            } catch (Exception e) {
//                logger.error("Error in getMongoTemplate: {}", e.getMessage(), e);
//                return getFallbackTemplate();
//            }
//        }
//
//        return getFallbackTemplate();
//    }
//
//    private MongoTemplate getFallbackTemplate() {
//        logger.info("Using fallback/default connection");
//        return templateCache.get(DEFAULT_CONNECTION_STRING, connString -> {
//            metricsService.recordConnectionCreation("default");
//            return createMonitoredMongoTemplate(DEFAULT_CONNECTION_STRING, "default");
//        });
//    }
////
////    public MongoTemplate getMongoTemplate() {
////        String tenantId = TenantContext.getCurrentTenant();
////        logger.info("getMongoTemplate called with tenantId: {}", tenantId);
////
////        if (tenantId != null) {
////            metricsService.recordConnectionRequest(tenantId);
////            Tenant tenant = getConnectionString(tenantId);
////
////            if (tenant == null) {
////                logger.warn("Tenant not found for ID: {}, using default connection", tenantId);
////                return templateCache.get(DEFAULT_CONNECTION_STRING, connString -> {
////                    logger.info("Creating new MongoTemplate with default connection string and 'default' tenant");
////                    return createMonitoredMongoTemplate(DEFAULT_CONNECTION_STRING, "default");
////                });
////            }
////
////            String connectionValue = tenant.getConnectionString();
////            logConnectionActivity(tenantId, "requested");
////            logger.info("Connection string retrieved for tenant {}: {}", tenantId, connectionValue != null ? "Found" : "Not found");
////
////            if (connectionValue != null) {
////                // Use Caffeine's get method with a mapping function
////                return templateCache.get(connectionValue, connString -> {
////                    metricsService.recordConnectionCreation(tenantId);
////                    logger.info("Creating new MongoTemplate for tenant: {}", tenantId);
////                    return createMonitoredMongoTemplate(connectionValue, tenantId);
////                });
////            }
////
////            String defaultTenantId = null;
////            logger.info("No connection string found for tenant {}, falling back to default", tenantId);
////            metricsService.recordConnectionRequest(defaultTenantId);
////
////            // Use Caffeine's get method for default connection
////            return templateCache.get(DEFAULT_CONNECTION_STRING, connString -> {
////                metricsService.recordConnectionCreation(defaultTenantId);
////                logger.info("Creating new MongoTemplate with default connection string");
////                return createMonitoredMongoTemplate(DEFAULT_CONNECTION_STRING, defaultTenantId);
////            });
////        }
////
////        // For default case when tenant is null
////        logger.info("Tenant ID is null, using default connection string");
////        return templateCache.get(DEFAULT_CONNECTION_STRING, connString -> {
////            logger.info("Creating new MongoTemplate with default connection string and 'default' tenant");
////            return createMonitoredMongoTemplate(DEFAULT_CONNECTION_STRING, "default");
////        });
////    }
////
////    public MongoTemplate getMongoTemplate() {
////        String tenantId = TenantContext.getCurrentTenant();
////        if (tenantId != null) {
////            metricsService.recordConnectionRequest(tenantId);
////            String connectionValue = getConnectionString(tenantId).getConnectionString();
////            logConnectionActivity(tenantId, "requested");
////
////            if (connectionValue != null) {
////                // Use Caffeine's get method with a mapping function
////                return templateCache.get(connectionValue, connString -> {
////                    metricsService.recordConnectionCreation(tenantId);
////                    return createMonitoredMongoTemplate(connectionValue, tenantId);
////                });
////            }
////
////            String defaultTenantId = null;
////            metricsService.recordConnectionRequest(defaultTenantId);
////
////            // Use Caffeine's get method for default connection
////            return templateCache.get(DEFAULT_CONNECTION_STRING, connString -> {
////                metricsService.recordConnectionCreation(defaultTenantId);
////                return createMonitoredMongoTemplate(DEFAULT_CONNECTION_STRING, defaultTenantId);
////            });
////        }
////
////        // For default case when tenant is null
////        return templateCache.get(DEFAULT_CONNECTION_STRING, connString ->
////                createMonitoredMongoTemplate(DEFAULT_CONNECTION_STRING, "default"));
////    }
//
//    // Create a MongoTemplate that monitors query execution
//    private MongoTemplate createMonitoredMongoTemplate(String connectionString, String tenantId) {
//        // Add logging at the beginning of this method
//        logger.info("DEBUG: Starting createMonitoredMongoTemplate");
//
//        try {
//            ConnectionString connString = new ConnectionString(connectionString);
//            // Add logging before creating the client
//            logger.info("DEBUG: About to create pooled MongoClient");
//
//            MongoClient mongoClient = createPooledMongoClient(connectionString);
//            // Add logging after creating the client
//            logger.info("DEBUG: MongoClient created successfully");
//
//            // Add more logging
//            logger.info("DEBUG: About to create MongoTemplate");
//
//            // Create and return the monitored template
//            return new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, extractDatabaseName(connectionString))) {
//                // Proxy implementation
//            };
//        } catch (Exception e) {
//            // Critical: Log any exceptions here
//            logger.error("Error creating MongoTemplate: {}", e.getMessage(), e);
//            throw e;
//        }
//    }
////    public MongoTemplate getMongoTemplate() {
////        String tenantId = TenantContext.getCurrentTenant();
////        if (tenantId != null) {
////            String connectionValue = getConnectionString(tenantId).getConnectionString();
////            if (connectionValue != null) {
////                System.err.println("Connection string is present for the tenant, connecting with default: " + connectionValue);
////                return templateCache.computeIfAbsent(connectionValue, this::createMongoTemplate);
////            }
////        } else {
////            System.err.println("Connection string is not present for the tenant, connecting with default.");
////            return templateCache.computeIfAbsent(DEFAULT_CONNECTION_STRING, this::createMongoTemplate);
////        }
////        return null;
////    }
//private MongoTemplate createMongoTemplate(String connectionString) {
//    logger.info("Creating MongoTemplate for connection string: {}", maskConnectionString(connectionString));
//
//    try {
//        // Check if the MongoClient for the connection string already exists in the cache
//        boolean clientExists = mongoClientCache.containsKey(connectionString);
//        logger.info("MongoClient for this connection {} in cache", clientExists ? "exists" : "does not exist");
//        MongoClient mongoClient=null;
//        try{
//    mongoClient= mongoClientCache.computeIfAbsent(connectionString, connString -> {
//          logger.info("Creating new pooled MongoClient for connection");
//          return createPooledMongoClient(connString);
//      });
//  }
//  catch (Exception ee){
//      logger.info("Extracted database name: {}", ee.getMessage());
//  }
//
//        String databaseName = extractDatabaseName(connectionString);
//        logger.info("Extracted database name: {}", databaseName);
//
//        if (databaseName == null) {
//            logger.error("Failed to extract database name from connection string");
//            throw new IllegalArgumentException("Could not extract database name from connection string");
//        }
//
//        logger.info("Creating new MongoTemplate with database: {}", databaseName);
//        MongoTemplate template = new MongoTemplate(new SimpleMongoClientDatabaseFactory(mongoClient, databaseName));
//        logger.info("Successfully created MongoTemplate");
//
//        return template;
//    } catch (Exception e) {
//        logger.error("Error creating MongoTemplate: {}", e.getMessage(), e);
//        throw new RuntimeException("Failed to create MongoTemplate", e);
//    }
//}
//
//
//    private String extractDatabaseName(String connectionString) {
//        logger.debug("Extracting database name from: {}", maskConnectionString(connectionString));
//
//        String regex = "mongodb://.*?/(.*?)(\\?|$)";
//        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
//        java.util.regex.Matcher matcher = pattern.matcher(connectionString);
//
//        if (matcher.find()) {
//            String databaseName = matcher.group(1);
//            logger.debug("Extracted database name: {}", databaseName);
//            return databaseName;
//        }
//
//        logger.warn("Failed to extract database name from connection string: {}", maskConnectionString(connectionString));
//        return null;
//    }
//
//
//    // Shutdown hook to clean up resources (close all unused connections)
//    @PreDestroy
//    public void shutdown() {
//        logger.info("Shutting down MongoDB connections");
//        mongoClientCache.forEach((connStr, client) -> {
//            logger.info("Closing MongoDB client for connection: {}", maskConnectionString(connStr));
//            client.close();
//        });
//        mongoClientCache.clear();
//        templateCache.cleanUp();
//        retryableTemplateCache.clear();
//        logger.info("All MongoDB connections closed successfully");
//    }
//    // Utility to mask credentials in logs
//    private String maskConnectionString(String connectionString) {
//        // Mask username:password in connection string for security
//        return connectionString.replaceAll("://([^:]+):([^@]+)@", "://*****:*****@");
//    }
//}
