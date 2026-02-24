package com.midas.consulting.service.storage;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.model.StorageConfig;
import com.midas.consulting.service.EnhancedTenantEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TenantConfigService {

    private static final Logger logger = LoggerFactory.getLogger(TenantConfigService.class);

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    @Autowired
    private EnhancedTenantEmailService tenantEmailService;

    // Cache for email configurations
    private final Map<String, EmailConfig> emailConfigCache = new ConcurrentHashMap<>();

    // ===== EMAIL CONFIGURATION METHODS =====

    public EmailConfig getEmailConfig(String tenantId) {
        // Check cache first
        if (emailConfigCache.containsKey(tenantId)) {
            return emailConfigCache.get(tenantId);
        }

        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("tenantId").is(tenantId).and("active").is(true));
            EmailConfig config = mongoTemplate.findOne(query, EmailConfig.class);

            // Cache the result
            if (config != null) {
                emailConfigCache.put(tenantId, config);
            }

            return config;
        } catch (Exception e) {
            logger.error("Error getting email config for tenant {}: {}", tenantId, e.getMessage(), e);
            return null;
        }
    }

    public EmailConfig saveEmailConfig(EmailConfig emailConfig) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Check if config already exists
            Query existingQuery = new Query(Criteria.where("id").is(emailConfig.getId()));
            EmailConfig existing = mongoTemplate.findOne(existingQuery, EmailConfig.class);

            if (existing != null) {
                // Update existing
                emailConfig.setId(existing.getId());
                emailConfig.setDateCreated(existing.getDateCreated());
                emailConfig.setDateModified(new Date());
            } else {
                // Create new
                emailConfig.setDateCreated(new Date());
                emailConfig.setDateModified(new Date());
            }

            EmailConfig saved = mongoTemplate.save(emailConfig);

            // Update cache
            emailConfigCache.put(emailConfig.getId(), saved);

            return saved;
        } catch (Exception e) {
            logger.error("Error saving email config for tenant {}: {}", emailConfig.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save email configuration", e);
        }
    }

    public boolean deleteEmailConfig(String tenantId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            EmailConfig config = mongoTemplate.findOne(query, EmailConfig.class);

            if (config != null) {
                config.setActive(false);
                config.setDateModified(new Date());
                mongoTemplate.save(config);

                // Remove from cache
                emailConfigCache.remove(tenantId);

                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting email config for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }

    // ===== STORAGE CONFIGURATION METHODS =====

    public StorageConfig getStorageConfig(String tenantId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("tenantId").is(tenantId).and("active").is(true));
            return mongoTemplate.findOne(query, StorageConfig.class);
        } catch (Exception e) {
            logger.error("Error getting storage config for tenant {}: {}", tenantId, e.getMessage(), e);
            return null;
        }
    }

    public StorageConfig saveStorageConfig(StorageConfig storageConfig) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Check if config already exists
            Query existingQuery = new Query(Criteria.where("tenantId").is(storageConfig.getTenantId()));
            StorageConfig existing = mongoTemplate.findOne(existingQuery, StorageConfig.class);

            if (existing != null) {
                // Update existing
                storageConfig.setId(existing.getId());
                storageConfig.setDateCreated(existing.getDateCreated());
                storageConfig.setDateModified(new Date());
            } else {
                // Create new
                storageConfig.setDateCreated(new Date());
                storageConfig.setDateModified(new Date());
            }

            return mongoTemplate.save(storageConfig);
        } catch (Exception e) {
            logger.error("Error saving storage config for tenant {}: {}", storageConfig.getTenantId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save storage configuration", e);
        }
    }

    public boolean deleteStorageConfig(String tenantId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            StorageConfig config = mongoTemplate.findOne(query, StorageConfig.class);

            if (config != null) {
                config.setActive(false);
                config.setDateModified(new Date());
                mongoTemplate.save(config);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting storage config for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }

    // ===== UTILITY METHODS =====

    public boolean isEmailConfigured(String tenantId) {
        EmailConfig config = getEmailConfig(tenantId);
        return config != null && config.getProvider() != null;
    }

    public boolean isStorageConfigured(String tenantId) {
        StorageConfig config = getStorageConfig(tenantId);
        return config != null && config.getPrimaryProvider() != null;
    }

    public TenantConfigStatus getTenantConfigStatus(String tenantId) {
        TenantConfigStatus status = new TenantConfigStatus();
        status.setTenantId(tenantId);
        status.setEmailConfigured(isEmailConfigured(tenantId));
        status.setStorageConfigured(isStorageConfigured(tenantId));

        // Get configuration details
        EmailConfig emailConfig = getEmailConfig(tenantId);
        if (emailConfig != null) {
            status.setEmailProvider(emailConfig.getProvider().getValue());
            status.setDefaultFromEmail(emailConfig.getDefaultFromEmail());
        }

        StorageConfig storageConfig = getStorageConfig(tenantId);
        if (storageConfig != null) {
            status.setPrimaryStorageProvider(storageConfig.getPrimaryProvider().getValue());
            status.setBackupEnabled(storageConfig.isEnableBackup());
        }

        return status;
    }

    // Clear cache method for configuration updates
    public void clearEmailConfigCache(String tenantId) {
        emailConfigCache.remove(tenantId);
        // Also clear the mail sender cache in TenantEmailService
        if (tenantEmailService != null) {
            tenantEmailService.clearMailSenderCache(tenantId);
        }
    }

    public void clearAllEmailConfigCache() {
        emailConfigCache.clear();
        // Also clear all mail sender caches in TenantEmailService
        if (tenantEmailService != null) {
            tenantEmailService.clearAllMailSenderCache();
        }
    }

    // Helper class for status
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class TenantConfigStatus {
        private String tenantId;
        private boolean emailConfigured;
        private boolean storageConfigured;
        private String emailProvider;
        private String defaultFromEmail;
        private String primaryStorageProvider;
        private boolean backupEnabled;
    }
}