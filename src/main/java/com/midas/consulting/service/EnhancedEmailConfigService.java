package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.model.EmailTemplateMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class EnhancedEmailConfigService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedEmailConfigService.class);

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    // Cache for email configurations
    private final Map<String, EmailConfig> configCache = new ConcurrentHashMap<>();
    private final Map<String, EmailTemplateMapping> templateMappingCache = new ConcurrentHashMap<>();

    // ===== EMAIL CONFIGURATION METHODS =====


    /**
     * Delete email configuration and cascade delete all associated template mappings
     */
    @CacheEvict(value = {"emailConfigs", "templateConfigs", "templateMappings"}, allEntries = true)
    public boolean deleteEmailConfigWithMappings(String configId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Check if config exists
            EmailConfig config = mongoTemplate.findById(configId, EmailConfig.class);
            if (config == null) {
                logger.warn("Email config not found for deletion: {}", configId);
                return false;
            }

            // Delete all associated template mappings
            Query mappingQuery = new Query(Criteria.where("emailConfigId").is(configId));
            long deletedMappings = mongoTemplate.remove(mappingQuery, EmailTemplateMapping.class).getDeletedCount();
            logger.info("Deleted {} template mappings for config {}", deletedMappings, configId);

            // Delete the configuration
            Query deleteQuery = new Query(Criteria.where("id").is(configId));
            mongoTemplate.remove(deleteQuery, EmailConfig.class);

            logger.info("Email configuration and mappings deleted successfully: {}", configId);
            return true;

        } catch (Exception e) {
            logger.error("Error deleting email config with mappings {}: {}", configId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete email configuration with mappings", e);
        }
    }

    /**
     * Get email configuration by ID
     */
    @Cacheable(value = "emailConfigs", key = "#configId")
    public EmailConfig getEmailConfigById(String configId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            return mongoTemplate.findById(configId, EmailConfig.class);
        } catch (Exception e) {
            logger.error("Error getting email config by ID {}: {}", configId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get email configuration by tenant and config name
     */
    @Cacheable(value = "emailConfigs", key = "#tenantId + '_' + #configName")
    public EmailConfig getEmailConfigByName(String tenantId, String configName) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.refreshMongoTemplate(tenantId);
            Query query = new Query(Criteria.where("configName").is(configName)
                    .and("active").is(true));
            return mongoTemplate.findOne(query, EmailConfig.class);
        } catch (Exception e) {
            logger.error("Error getting email config for tenant {} and name {}: {}",
                    tenantId, configName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get all email configurations for a tenant
     */
    public List<EmailConfig> getTenantEmailConfigs(String tenantId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("active").is(true))
                    .with(Sort.by(Sort.Direction.ASC, "priority"));
            return mongoTemplate.find(query, EmailConfig.class);
        } catch (Exception e) {
            logger.error("Error getting email configs for tenant {}: {}", tenantId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get email configuration for a specific template
     */
    @Cacheable(value = "templateConfigs", key = "#tenantId + '_' + #templateName")
    public EmailConfig getEmailConfigForTemplate(String tenantId, String templateName) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // First, check template mapping
            EmailTemplateMapping mapping = getTemplateMapping(tenantId, templateName);
            if (mapping != null && mapping.isActive()) {
                // Get config by ID or name from mapping
                if (mapping.getEmailConfigId() != null) {
                    return getEmailConfigById(mapping.getEmailConfigId());
                } else if (mapping.getEmailConfigName() != null) {
                    return getEmailConfigByName(tenantId, mapping.getEmailConfigName());
                }
            }

            // Fallback: Find config that includes this template in its mappings
            Query query = new Query(Criteria
                    .where("templateMappings").in(templateName)
                    .and("active").is(true))
                    .with(Sort.by(Sort.Direction.ASC, "priority"));

            List<EmailConfig> configs = mongoTemplate.find(query, EmailConfig.class);
            if (!configs.isEmpty()) {
                return configs.get(0); // Return highest priority config
            }

            // Last fallback: Get default config (lowest priority number)
            return getDefaultEmailConfig(tenantId);

        } catch (Exception e) {
            logger.error("Error getting email config for template {} in tenant {}: {}",
                    templateName, tenantId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get default email configuration for tenant
     */
    public EmailConfig getDefaultEmailConfig(String tenantId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("active").is(true))
                    .with(Sort.by(Sort.Direction.ASC, "priority"))
                    .limit(1);
            return mongoTemplate.findOne(query, EmailConfig.class);
        } catch (Exception e) {
            logger.error("Error getting default email config for tenant {}: {}", tenantId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save or update email configuration
     */
    @CacheEvict(value = {"emailConfigs", "templateConfigs"}, allEntries = true)
    public EmailConfig saveEmailConfig(EmailConfig emailConfig) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Check if config with same name exists
            Query existingQuery = new Query(Criteria.where("id").is(emailConfig.getId())
                    .and("configName").is(emailConfig.getConfigName()));
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

            return mongoTemplate.save(emailConfig);

        } catch (Exception e) {
            logger.error("Error saving email config: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save email configuration", e);
        }
    }

    // ===== TEMPLATE MAPPING METHODS =====

    /**
     * Get template mapping
     */
    @Cacheable(value = "templateMappings", key = "#tenantId + '_' + #templateName")
    public EmailTemplateMapping getTemplateMapping(String tenantId, String templateName) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("templateName").is(templateName)
                    .and("active").is(true));
            return mongoTemplate.findOne(query, EmailTemplateMapping.class);
        } catch (Exception e) {
            logger.error("Error getting template mapping for {} in tenant {}: {}",
                    templateName, tenantId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Save template mapping
     */
    @CacheEvict(value = {"templateMappings", "templateConfigs"}, allEntries = true)
    public EmailTemplateMapping saveTemplateMapping(EmailTemplateMapping mapping) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Check if mapping exists
            Query existingQuery = new Query(Criteria.where("templateName").is(mapping.getTemplateName()));
            EmailTemplateMapping existing = mongoTemplate.findOne(existingQuery, EmailTemplateMapping.class);

            if (existing != null) {
                mapping.setId(existing.getId());
                mapping.setDateCreated(existing.getDateCreated());
                mapping.setDateModified(new Date());
            } else {
                mapping.setDateCreated(new Date());
                mapping.setDateModified(new Date());
            }

            return mongoTemplate.save(mapping);

        } catch (Exception e) {
            logger.error("Error saving template mapping: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save template mapping", e);
        }
    }

    /**
     * Bulk update template mappings
     */
    @CacheEvict(value = {"templateMappings", "templateConfigs"}, allEntries = true)
    public void updateTemplateMappings(String tenantId, String emailConfigId, List<String> templateNames) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Remove existing mappings for these templates
            Query removeQuery = new Query(Criteria.where("templateName").in(templateNames));
            mongoTemplate.remove(removeQuery, EmailTemplateMapping.class);

            // Create new mappings
            for (String templateName : templateNames) {
                EmailTemplateMapping mapping = new EmailTemplateMapping()
                        .setTenantId(tenantId)
                        .setTemplateName(templateName)
                        .setEmailConfigId(emailConfigId)
                        .setActive(true)
                        .setDateCreated(new Date());
                mongoTemplate.save(mapping);
            }

        } catch (Exception e) {
            logger.error("Error updating template mappings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update template mappings", e);
        }
    }

    // ===== UTILITY METHODS =====

    /**
     * Check if email is configured for tenant
     */
    public boolean isEmailConfigured(String tenantId) {
        List<EmailConfig> configs = getTenantEmailConfigs(tenantId);
        return configs != null && !configs.isEmpty();
    }

    /**
     * Check if specific template is configured
     */
    public boolean isTemplateConfigured(String tenantId, String templateName) {
        EmailConfig config = getEmailConfigForTemplate(tenantId, templateName);
        return config != null;
    }

    /**
     * Get email configuration status for tenant
     */
    public EmailConfigStatus getTenantEmailConfigStatus(String tenantId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Get all configs
            List<EmailConfig> configs = getTenantEmailConfigs(tenantId);

            // Get all template mappings
            Query mappingQuery = new Query(Criteria.where("active").is(true));
            List<EmailTemplateMapping> mappings = mongoTemplate.find(mappingQuery, EmailTemplateMapping.class);

            EmailConfigStatus status = new EmailConfigStatus();
            status.setTenantId(tenantId);
            status.setTotalConfigs(configs != null ? configs.size() : 0);
            status.setActiveConfigs(configs != null ? (int) configs.stream().filter(EmailConfig::isActive).count() : 0);
            status.setTotalMappings(mappings.size());
            status.setConfigured(status.getActiveConfigs() > 0);

            return status;

        } catch (Exception e) {
            logger.error("Error getting email config status for tenant {}: {}", tenantId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Clear all caches
     */
    @CacheEvict(value = {"emailConfigs", "templateConfigs", "templateMappings"}, allEntries = true)
    public void clearAllCaches() {
        configCache.clear();
        templateMappingCache.clear();
        logger.info("All email configuration caches cleared");
    }

    // Helper class for status
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class EmailConfigStatus {
        private String tenantId;
        private int totalConfigs;
        private int activeConfigs;
        private int totalMappings;
        private boolean configured;
        private List<String> availableTemplates;
        private List<String> unconfiguredTemplates;
    }
}