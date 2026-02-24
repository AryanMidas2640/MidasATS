package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.api.ComprehensiveTenantController.ComprehensiveEmailConfig;
import com.midas.consulting.controller.v1.api.ComprehensiveTenantController.ComprehensiveEmailSetupResult;
import com.midas.consulting.controller.v1.api.ComprehensiveTenantController.ComprehensiveEmailStatusResult;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.service.storage.TenantConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;

//@Service
//public class ComprehensiveEmailSetupService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveEmailSetupService.class);
//
//    @Autowired private TenantConfigService tenantConfigService;
//    @Autowired private EmailTemplateService emailTemplateService;
//    @Autowired private EnhancedTenantEmailService tenantEmailService;
//    @Autowired private MongoTemplateProvider mongoTemplateProvider;
//
//    public ComprehensiveEmailSetupResult setupComprehensiveEmailSystem(
//            String tenantId, ComprehensiveEmailConfig config, String adminEmail) {
//
//        logger.info("Setting up comprehensive email system for tenant: {} with provider: {}",
//                   tenantId, config.getProvider());
//
//        ComprehensiveEmailSetupResult result = new ComprehensiveEmailSetupResult();
//
//        try {
//            // Set tenant context
//            TenantContext.setCurrentTenant(tenantId);
//
//            // Deactivate any existing email configurations
//            deactivateAllEmailConfigs(tenantId);
//
//            // Create new email configuration based on provider
//            EmailConfig emailConfig = createEmailConfigFromRequest(tenantId, config, adminEmail);
//            EmailConfig saved = tenantConfigService.saveEmailConfig(emailConfig);
//
//            // Create default email templates
//            emailTemplateService.createDefaultTemplates(tenantId);
//
//            // Test the configuration
//            boolean testPassed = testEmailConfiguration(saved);
//
//            result.setSuccess(true)
//                  .setConfigId(saved.getId())
//                  .setProvider(config.getProvider().name())
//                  .setFromEmail(config.getFromEmail())
//                  .setTestPassed(testPassed)
//                  .setMessage("Email system configured successfully with " + config.getProvider().name());
//
//            logger.info("Email system setup completed successfully for tenant: {}", tenantId);
//
//        } catch (Exception e) {
//            logger.error("Email system setup failed for tenant {}: {}", tenantId, e.getMessage(), e);
//            result.setSuccess(false)
//                  .setMessage("Email setup failed: " + e.getMessage());
//        } finally {
//            TenantContext.clear();
//        }
//
//        return result;
//    }
//
//    public void deactivateAllEmailConfigs(String tenantId) {
//        try {
//            logger.info("Deactivating all email configurations for tenant: {}", tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            Query query = new Query(Criteria.where("tenantId").is(tenantId));
//            Update update = new Update()
//                .set("active", false)
//                .set("dateModified", new Date());
//
//            mongoTemplate.updateMulti(query, update, EmailConfig.class);
//
//            // Clear mail sender cache
//            tenantEmailService.clearAllMailSenderCache();
//
//            logger.info("All email configurations deactivated for tenant: {}", tenantId);
//
//        } catch (Exception e) {
//            logger.error("Failed to deactivate email configs for tenant {}: {}", tenantId, e.getMessage());
//        }
//    }
//
//    public ComprehensiveEmailStatusResult getComprehensiveEmailStatus(String tenantId) {
//        ComprehensiveEmailStatusResult status = new ComprehensiveEmailStatusResult();
//
//        try {
//            EmailConfig activeConfig = tenantConfigService.getEmailConfig(tenantId);
//
//            if (activeConfig != null && activeConfig.isActive()) {
//                status.setConfigured(true)
//                      .setActiveProvider(activeConfig.getProvider().getValue())
//                      .setActiveFromEmail(activeConfig.getEmailAddress())
//                      .setConfigId(activeConfig.getId());
//
//                // Test the configuration
//                boolean testPassed = testEmailConfiguration(activeConfig);
//                status.setTestPassed(testPassed)
//                      .setLastTested(new Date());
//            } else {
//                status.setConfigured(false);
//            }
//
//        } catch (Exception e) {
//            logger.error("Failed to get email status for tenant {}: {}", tenantId, e.getMessage());
//            status.setConfigured(false);
//        }
//
//        return status;
//    }
//
//    private EmailConfig createEmailConfigFromRequest(String tenantId,
//            ComprehensiveEmailConfig config, String adminEmail) {
//
//        logger.debug("Creating email config for provider: {}", config.getProvider());
//
//        EmailConfig emailConfig = new EmailConfig()
//            .setTenantId(tenantId)
//            .setConfigName("primary")
//            .setDescription("Primary email configuration - " + config.getProvider().name())
//            .setEmailAddress(config.getFromEmail())
//            .setDisplayName(config.getFromName() != null ? config.getFromName() : "ATS System")
//            .setDefaultFromEmail(config.getFromEmail())
//            .setDefaultFromName(config.getFromName() != null ? config.getFromName() : "ATS System")
//            .setDefaultReplyTo(config.getReplyTo() != null ? config.getReplyTo() : config.getFromEmail())
//            .setActive(true)
//            .setPriority(1)
//            .setDateCreated(new Date())
//            .setDateModified(new Date());
//
//        switch (config.getProvider()) {
//            case OUTLOOK:
//                validateOutlookConfig(config);
//                emailConfig.setProvider(EmailConfig.EmailServiceProvider.OUTLOOK);
//                EmailConfig.OutlookConfig outlookConfig = new EmailConfig.OutlookConfig()
//                    .setClientId(config.getClientId())
//                    .setClientSecret(config.getClientSecret())
//                    .setTenantId(config.getTenantId())
//                    .setFromEmail(config.getFromEmail());
//                emailConfig.setOutlookConfig(outlookConfig);
//                break;
//
//            case GMAIL:
//                validateGmailConfig(config);
//                emailConfig.setProvider(EmailConfig.EmailServiceProvider.GMAIL);
//                EmailConfig.GmailConfig gmailConfig = new EmailConfig.GmailConfig()
//                    .setClientId(config.getGmailClientId())
//                    .setClientSecret(config.getGmailClientSecret())
//                    .setFromEmail(config.getFromEmail())
//                    .setCredentialsPath(config.getCredentialsPath());
//                emailConfig.setGmailConfig(gmailConfig);
//                break;
//
//            case SMTP:
//            default:
//                validateSmtpConfig(config);
//                emailConfig.setProvider(EmailConfig.EmailServiceProvider.SMTP);
//                EmailConfig.SmtpConfig smtpConfig = new EmailConfig.SmtpConfig()
//                    .setHost(config.getSmtpHost())
//                    .setPort(config.getSmtpPort())
//                    .setUsername(config.getSmtpUsername())
//                    .setPassword(config.getSmtpPassword())
//                    .setEnableTls(config.getEnableTls())
//                    .setEnableSsl(config.getEnableSsl());
//                emailConfig.setSmtpConfig(smtpConfig);
//                break;
//        }
//
//        // Set rate limiting
//        EmailConfig.RateLimitConfig rateLimitConfig = new EmailConfig.RateLimitConfig()
//            .setEnabled(true)
//            .setMaxPerHour(100)
//            .setMaxPerDay(1000)
//            .setMaxPerMonth(10000);
//        emailConfig.setRateLimitConfig(rateLimitConfig);
//
//        return emailConfig;
//    }
//
//    private void validateOutlookConfig(ComprehensiveEmailConfig config) {
//        if (config.getClientId() == null || config.getClientId().trim().isEmpty()) {
//            throw new IllegalArgumentException("Outlook Client ID is required");
//        }
//        if (config.getClientSecret() == null || config.getClientSecret().trim().isEmpty()) {
//            throw new IllegalArgumentException("Outlook Client Secret is required");
//        }
//        if (config.getTenantId() == null || config.getTenantId().trim().isEmpty()) {
//            throw new IllegalArgumentException("Outlook Tenant ID is required");
//        }
//    }
//
//    private void validateGmailConfig(ComprehensiveEmailConfig config) {
//        if (config.getGmailClientId() == null || config.getGmailClientId().trim().isEmpty()) {
//            throw new IllegalArgumentException("Gmail Client ID is required");
//        }
//        if (config.getGmailClientSecret() == null || config.getGmailClientSecret().trim().isEmpty()) {
//            throw new IllegalArgumentException("Gmail Client Secret is required");
//        }
//    }
//
//    private void validateSmtpConfig(ComprehensiveEmailConfig config) {
//        if (config.getSmtpHost() == null || config.getSmtpHost().trim().isEmpty()) {
//            throw new IllegalArgumentException("SMTP Host is required");
//        }
//        if (config.getSmtpUsername() == null || config.getSmtpUsername().trim().isEmpty()) {
//            throw new IllegalArgumentException("SMTP Username is required");
//        }
//        if (config.getSmtpPassword() == null || config.getSmtpPassword().trim().isEmpty()) {
//            throw new IllegalArgumentException("SMTP Password is required");
//        }
//    }
//
//    private boolean testEmailConfiguration(EmailConfig config) {
//        try {
//            logger.info("Testing email configuration: {}", config.getId());
//            return tenantEmailService.testEmailConfiguration(config.getId());
//        } catch (Exception e) {
//            logger.warn("Email configuration test failed: {}", e.getMessage());
//            return false;
//        }
//    }
//}
@Service
public class ComprehensiveEmailSetupService {

    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveEmailSetupService.class);

    @Autowired private TenantConfigService tenantConfigService;
    @Autowired private EmailTemplateService emailTemplateService;
    @Autowired private EnhancedTenantEmailService tenantEmailService;
    @Autowired private MongoTemplateProvider mongoTemplateProvider;

    public ComprehensiveEmailSetupResult setupComprehensiveEmailSystem(
            String tenantId, ComprehensiveEmailConfig config, String adminEmail) {

        logger.info("Setting up comprehensive email system for tenant: {} with provider: {}",
                tenantId, config.getProvider());

        ComprehensiveEmailSetupResult result = new ComprehensiveEmailSetupResult();

        try {
            // Set tenant context
            TenantContext.setCurrentTenant(tenantId);

            // Deactivate any existing email configurations
            deactivateAllEmailConfigs(tenantId);

            // Create new email configuration based on provider
            EmailConfig emailConfig = createEmailConfigFromRequest(tenantId, config, adminEmail);
            EmailConfig saved = tenantConfigService.saveEmailConfig(emailConfig);

            // Create default email templates
            emailTemplateService.createDefaultTemplates(tenantId);

            // Test the configuration
            boolean testPassed = testEmailConfiguration(saved);

            result.setSuccess(true)
                    .setConfigId(saved.getId())
                    .setProvider(config.getProvider().name())
                    .setFromEmail(config.getFromEmail())
                    .setTestPassed(testPassed)
                    .setMessage("Email system configured successfully with " + config.getProvider().name());

            logger.info("Email system setup completed successfully for tenant: {}", tenantId);

        } catch (Exception e) {
            logger.error("Email system setup failed for tenant {}: {}", tenantId, e.getMessage(), e);
            result.setSuccess(false)
                    .setMessage("Email setup failed: " + e.getMessage());
        } finally {
            TenantContext.clear();
        }

        return result;
    }

    public void deactivateAllEmailConfigs(String tenantId) {
        try {
            logger.info("Deactivating all email configurations for tenant: {}", tenantId);

            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            Update update = new Update()
                    .set("active", false)
                    .set("dateModified", new Date());

            mongoTemplate.updateMulti(query, update, EmailConfig.class);

            // Clear mail sender cache
            tenantEmailService.clearAllMailSenderCache();

            logger.info("All email configurations deactivated for tenant: {}", tenantId);

        } catch (Exception e) {
            logger.error("Failed to deactivate email configs for tenant {}: {}", tenantId, e.getMessage());
        }
    }

    public ComprehensiveEmailStatusResult getComprehensiveEmailStatus(String tenantId) {
        ComprehensiveEmailStatusResult status = new ComprehensiveEmailStatusResult();

        try {
            EmailConfig activeConfig = tenantConfigService.getEmailConfig(tenantId);

            if (activeConfig != null && activeConfig.isActive()) {
                status.setConfigured(true)
                        .setActiveProvider(activeConfig.getProvider().getValue())
                        .setActiveFromEmail(activeConfig.getEmailAddress())
                        .setConfigId(activeConfig.getId());

                // Test the configuration
                boolean testPassed = testEmailConfiguration(activeConfig);
                status.setTestPassed(testPassed)
                        .setLastTested(new Date());
            } else {
                status.setConfigured(false);
            }

        } catch (Exception e) {
            logger.error("Failed to get email status for tenant {}: {}", tenantId, e.getMessage());
            status.setConfigured(false);
        }

        return status;
    }

    private EmailConfig createEmailConfigFromRequest(String tenantId,
                                                     ComprehensiveEmailConfig config, String adminEmail) {

        logger.debug("Creating email config for provider: {}", config.getProvider());

        EmailConfig emailConfig = new EmailConfig()
//                .setTenantId(tenantId)
                .setConfigName("primary")
                .setDescription("Primary email configuration - " + config.getProvider().name())
                .setEmailAddress(config.getFromEmail())
                .setDisplayName(config.getFromName() != null ? config.getFromName() : "ATS System")
                .setDefaultFromEmail(config.getFromEmail())
                .setDefaultFromName(config.getFromName() != null ? config.getFromName() : "ATS System")
                .setDefaultReplyTo(config.getReplyTo() != null ? config.getReplyTo() : config.getFromEmail())
                .setActive(true)
                .setPriority(1)
                .setDateCreated(new Date())
                .setDateModified(new Date());

        switch (config.getProvider()) {
            case OUTLOOK:
                validateOutlookConfig(config);
                emailConfig.setProvider(EmailConfig.EmailServiceProvider.OUTLOOK);
                EmailConfig.OutlookConfig outlookConfig = new EmailConfig.OutlookConfig()
                        .setClientId(config.getClientId())
                        .setClientSecret(config.getClientSecret())
                        .setTenantId(config.getTenantId())
                        .setFromEmail(config.getFromEmail());
                emailConfig.setOutlookConfig(outlookConfig);
                break;

            case GMAIL:
                validateGmailConfig(config);
                emailConfig.setProvider(EmailConfig.EmailServiceProvider.GMAIL);
                EmailConfig.GmailConfig gmailConfig = new EmailConfig.GmailConfig()
                        .setClientId(config.getGmailClientId())
                        .setClientSecret(config.getGmailClientSecret())
                        .setFromEmail(config.getFromEmail())
                        .setCredentialsPath(config.getCredentialsPath());
                emailConfig.setGmailConfig(gmailConfig);
                break;

            case SMTP:
            default:
                validateSmtpConfig(config);
                emailConfig.setProvider(EmailConfig.EmailServiceProvider.SMTP);
                EmailConfig.SmtpConfig smtpConfig = new EmailConfig.SmtpConfig()
                        .setHost(config.getSmtpHost())
                        .setPort(config.getSmtpPort())
                        .setUsername(config.getSmtpUsername())
                        .setPassword(config.getSmtpPassword())
                        .setEnableTls(config.getEnableTls())
                        .setEnableSsl(config.getEnableSsl());
                emailConfig.setSmtpConfig(smtpConfig);
                break;
        }

        // Set rate limiting
        EmailConfig.RateLimitConfig rateLimitConfig = new EmailConfig.RateLimitConfig()
                .setEnabled(true)
                .setMaxPerHour(100)
                .setMaxPerDay(1000)
                .setMaxPerMonth(10000);
        emailConfig.setRateLimitConfig(rateLimitConfig);

        return emailConfig;
    }

    private void validateOutlookConfig(ComprehensiveEmailConfig config) {
        if (config.getClientId() == null || config.getClientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Outlook Client ID is required");
        }
        if (config.getClientSecret() == null || config.getClientSecret().trim().isEmpty()) {
            throw new IllegalArgumentException("Outlook Client Secret is required");
        }
        if (config.getTenantId() == null || config.getTenantId().trim().isEmpty()) {
            throw new IllegalArgumentException("Outlook Tenant ID is required");
        }
    }

    private void validateGmailConfig(ComprehensiveEmailConfig config) {
        if (config.getGmailClientId() == null || config.getGmailClientId().trim().isEmpty()) {
            throw new IllegalArgumentException("Gmail Client ID is required");
        }
        if (config.getGmailClientSecret() == null || config.getGmailClientSecret().trim().isEmpty()) {
            throw new IllegalArgumentException("Gmail Client Secret is required");
        }
    }

    private void validateSmtpConfig(ComprehensiveEmailConfig config) {
        if (config.getSmtpHost() == null || config.getSmtpHost().trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP Host is required");
        }
        if (config.getSmtpUsername() == null || config.getSmtpUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP Username is required");
        }
        if (config.getSmtpPassword() == null || config.getSmtpPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("SMTP Password is required");
        }
    }

    private boolean testEmailConfiguration(EmailConfig config) {
        try {
            logger.info("Testing email configuration: {}", config.getId());
            return tenantEmailService.testEmailConfiguration(config.getId());
        } catch (Exception e) {
            logger.warn("Email configuration test failed: {}", e.getMessage());
            return false;
        }
    }
}
