package com.midas.consulting.service;

import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.service.storage.TenantConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultEmailConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultEmailConfigService.class);
    
    @Autowired
    private TenantConfigService tenantConfigService;
    
    public void createDefaultEmailConfig(String tenantId, String adminEmail) {
        try {
            // Check if default config already exists
            EmailConfig existingConfig = tenantConfigService.getEmailConfig(tenantId);
            if (existingConfig != null) {
                logger.info("Email config already exists for tenant: {}", tenantId);
                return;
            }
            
            // Create default SMTP configuration
            EmailConfig defaultConfig = new EmailConfig()
//                    .setTenantId(tenantId)
                    .setConfigName("default")
                    .setDescription("Default email configuration for tenant")
                    .setEmailAddress(adminEmail)
                    .setDisplayName("System Notifications")
                    .setProvider(EmailConfig.EmailServiceProvider.SMTP)
                    .setActive(true)
                    .setPriority(1)
                    .setDefaultFromEmail(adminEmail)
                    .setDefaultFromName("ATS System");
            
            // Set basic SMTP config (tenant will need to update with real credentials)
            EmailConfig.SmtpConfig smtpConfig = new EmailConfig.SmtpConfig()
                    .setHost("smtp.gmail.com")  // Default to Gmail
                    .setPort(587)
                    .setEnableTls(true)
                    .setEnableSsl(false)
                    .setUsername("") // Empty - tenant needs to configure
                    .setPassword(""); // Empty - tenant needs to configure
            
            defaultConfig.setSmtpConfig(smtpConfig);
            
            // Set rate limiting
            EmailConfig.RateLimitConfig rateLimitConfig = new EmailConfig.RateLimitConfig()
                    .setEnabled(true)
                    .setMaxPerHour(100)
                    .setMaxPerDay(1000)
                    .setMaxPerMonth(10000);
            
            defaultConfig.setRateLimitConfig(rateLimitConfig);
            
            // Save the configuration
            EmailConfig savedConfig = tenantConfigService.saveEmailConfig(defaultConfig);
            
            logger.info("Created default email configuration for tenant: {} with ID: {}", 
                       tenantId, savedConfig.getId());
            
        } catch (Exception e) {
            logger.error("Failed to create default email configuration for tenant {}: {}", 
                        tenantId, e.getMessage(), e);
        }
    }
}