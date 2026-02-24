package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class TenantConnectionInitService {

    private static final Logger logger = LoggerFactory.getLogger(TenantConnectionInitService.class);

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;



    /**
     * Initialize connections for all tenants at application startup
     */
    @EventListener(ApplicationReadyEvent.class)


    public void initializeAllTenantConnections() {

        logger.info("Initializing connections for all tenants");

        // Use default connection to get the list of tenants
        MongoTemplate defaultTemplate = mongoTemplateProvider.getMongoTemplate();
        List<Tenant> tenants = defaultTemplate.findAll(Tenant.class);

        logger.info("Found {} tenants to initialize", tenants.size());

        // Initialize connections for each tenant asynchronously
        CompletableFuture<?>[] futures = tenants.stream()
                .map(tenant -> warmupTenantConnection(tenant.getId()))
                .toArray(CompletableFuture[]::new);

        // Wait for all initializations to complete
        CompletableFuture.allOf(futures).join();

        logger.info("All tenant connections initialized successfully");
    }

    /**
     * Warm up a tenant connection by executing a simple query
     */
    @Async
    public CompletableFuture<Void> warmupTenantConnection(String tenantId) {
        {
            logger.info("Warming up connection for tenant: {}", tenantId);
            try {
                // Set tenant context
                TenantContext.setCurrentTenant(tenantId);

                // Get tenant-specific template
                MongoTemplate template = mongoTemplateProvider.getMongoTemplate();

                // Execute a simple query to establish connection
                template.getCollectionNames();

                logger.info("Connection for tenant {} warmed up successfully", tenantId);
                return CompletableFuture.completedFuture(null);
            } catch (Exception e) {
                logger.error("Failed to warm up connection for tenant {}: {}", tenantId, e.getMessage(), e);
                CompletableFuture<Void> future = new CompletableFuture<>();
                future.completeExceptionally(e);
                return future;
            } finally {
                // Clear tenant context
                TenantContext.clear();
            }
        }
    }
}