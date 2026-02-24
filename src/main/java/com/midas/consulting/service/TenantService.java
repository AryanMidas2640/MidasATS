package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.model.TenantPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    private final MongoTemplateProvider  multiTenantMongoTemplateFactory;
    private final HttpServletRequest request;

    private static final Map<String, TenantPoolConfig> tenantPoolConfigs = new ConcurrentHashMap<>();

    public TenantPoolConfig getTenantPoolConfig(String tenantId) {
        return tenantPoolConfigs.computeIfAbsent(tenantId, id -> {
            // Load from database or use default
            Tenant tenant = multiTenantMongoTemplateFactory.getMongoTemplate().findById(id, Tenant.class);

            TenantPoolConfig config = new TenantPoolConfig();
            config.setMaxPoolSize(50); // Default
            config.setMinPoolSize(10);
            config.setMaxWaitTimeMs(3000);

            if (tenant != null && tenant.getConnectionPoolConfig() != null) {
                // If tenant has custom pool configuration, use it
                // You would need to add this field to the Tenant model
                Map<String, Object> poolConfig = tenant.getConnectionPoolConfig();
                if (poolConfig.containsKey("maxPoolSize")) {
                    config.setMaxPoolSize((Integer) poolConfig.get("maxPoolSize"));
                }
                if (poolConfig.containsKey("minPoolSize")) {
                    config.setMinPoolSize((Integer) poolConfig.get("minPoolSize"));
                }
                if (poolConfig.containsKey("maxWaitTimeMs")) {
                    config.setMaxWaitTimeMs((Integer) poolConfig.get("maxWaitTimeMs"));
                }
            }

            return config;
        });
    }

    // Add method to update tenant pool configuration
    public void updateTenantPoolConfig(String tenantId, TenantPoolConfig config) {
        tenantPoolConfigs.put(tenantId, config);

        // Optionally persist to database
        Tenant tenant = multiTenantMongoTemplateFactory.getMongoTemplate().findById(tenantId, Tenant.class);
        if (tenant != null) {
            Map<String, Object> poolConfig = new HashMap<>();
            poolConfig.put("maxPoolSize", config.getMaxPoolSize());
            poolConfig.put("minPoolSize", config.getMinPoolSize());
            poolConfig.put("maxWaitTimeMs", config.getMaxWaitTimeMs());

            tenant.setConnectionPoolConfig(poolConfig);
            multiTenantMongoTemplateFactory.getMongoTemplate().save(tenant);
        }
    }

    @Autowired
    public TenantService( HttpServletRequest request, MongoTemplateProvider  multiTenantMongoTemplateFactory) {
        this. multiTenantMongoTemplateFactory =  multiTenantMongoTemplateFactory;
        this.request = request;
    }

    private String getTenantIdFromHeader() {
        String tenantId = request.getHeader("X-Tenant");
        if (tenantId == null || tenantId.isEmpty()) {
            throw new IllegalArgumentException("Tenant ID must not be null or empty");
        }
        return tenantId;

    }

    public List<Tenant> getAllTenants() {
        return  multiTenantMongoTemplateFactory.getMongoTemplate().findAll(Tenant.class);
    }

    public Tenant getTenantById(String id) throws Exception {
        Tenant tenant=
//        tenantRepository.findById(id);

                multiTenantMongoTemplateFactory.getMongoTemplate().findById(id,Tenant.class);
        if (tenant!=null){
            return  tenant;
        }
        throw new Exception("Could ot found");
    }

    public Tenant createTenant(Tenant tenant) {
        logger.info("Creating new tenant with subdomain: {}", tenant.getSubdomain());
        return  multiTenantMongoTemplateFactory.getMongoTemplate().save(tenant);
    }

    public Tenant updateTenant(String id, Tenant tenant) {
        logger.info("Updating tenant with id: {}", id);
        // Create MongoTemplate for the specific tenant
        MongoTemplate mongoTemplate =  multiTenantMongoTemplateFactory.getMongoTemplate();

        // Check if the tenant exists before updating
        Query query = new Query(Criteria.where("id").is(id));
        boolean exists = mongoTemplate.exists(query, Tenant.class);

        if (exists) {
            tenant.setId(id);
            return mongoTemplate.save(tenant); // Save the updated tenant
        }
        return null; // Or throw an exception if tenant not found
    }

    public void deleteTenant(String id) {
        logger.info("Deleting tenant with id: {}", id);
        // Create MongoTemplate for the specific tenant
        MongoTemplate mongoTemplate =  multiTenantMongoTemplateFactory.getMongoTemplate();

        // Create a Query to find the tenant by ID
        Query query = new Query(Criteria.where("id").is(id));

        // Remove the tenant from the collection
        mongoTemplate.remove(query, Tenant.class);
    }

    public Tenant getTenantBySubdomain(String subdomain) {
        logger.info("Finding tenant by subdomain: {}", subdomain);
        Query query = new Query(Criteria.where("subdomain").is(subdomain));
        return multiTenantMongoTemplateFactory.getMongoTemplate().findOne(query, Tenant.class);
    }

    /**
     * Check if a subdomain is available for use
     * @param subdomain The subdomain to check
     * @return true if available, false if taken
     */
    public boolean isSubdomainAvailable(String subdomain) {
        logger.info("Checking availability for subdomain: {}", subdomain);
        return getTenantBySubdomain(subdomain) == null;
    }

    /**
     * Validate subdomain format
     * @param subdomain The subdomain to validate
     * @return true if valid format
     */
    public boolean isValidSubdomain(String subdomain) {
        if (subdomain == null || subdomain.trim().isEmpty()) {
            return false;
        }

        // Check length (3-30 characters)
        if (subdomain.length() < 3 || subdomain.length() > 30) {
            return false;
        }

        // Check format: lowercase letters, numbers, hyphens (not at start/end)
        return subdomain.matches("^[a-z0-9]([a-z0-9-]*[a-z0-9])?$");
    }

    /**
     * Get tenant by connection string
     * @param connectionString The connection string to search for
     * @return Tenant if found
     */
    public Tenant getTenantByConnectionString(String connectionString) {
        Query query = new Query(Criteria.where("connectionString").is(connectionString));
        return multiTenantMongoTemplateFactory.getMongoTemplate().findOne(query, Tenant.class);
    }
}