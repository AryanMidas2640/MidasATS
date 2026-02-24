package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.EmailRequest;
//import com.midas.consulting.dto.response.Response;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.model.StorageConfig;
import com.midas.consulting.service.EnhancedTenantEmailService;
import com.midas.consulting.service.TenantContext;
import com.midas.consulting.service.storage.TenantConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/tenant-config")
@Api(value = "tenant-configuration", description = "Operations for managing tenant-specific configurations")
public class TenantConfigController {
    
    private static final Logger logger = LoggerFactory.getLogger(TenantConfigController.class);
    
    @Autowired
    private TenantConfigService tenantConfigService;
    
    @Autowired
    private EnhancedTenantEmailService tenantEmailService;
    
    // ===== EMAIL CONFIGURATION ENDPOINTS =====
    
    @PostMapping("/email/smtp")
    @ApiOperation(value = "Configure SMTP email for tenant", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response configureSmtpEmail(Principal principal, @Valid @RequestBody SmtpConfigRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig emailConfig = createEmailConfigFromSmtp(tenantId, request);
            EmailConfig savedConfig = tenantConfigService.saveEmailConfig(emailConfig);
            
            // Clear mail sender cache to force reconfiguration
            tenantEmailService.clearMailSenderCache(tenantId);
            
            return Response.ok().setPayload(maskSensitiveEmailData(savedConfig));
        } catch (Exception e) {
            logger.error("Error configuring SMTP email for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to configure SMTP email", e);
        }
    }
    
    @PostMapping("/email/sendgrid")
    @ApiOperation(value = "Configure SendGrid email for tenant", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response configureSendGridEmail(Principal principal, @Valid @RequestBody SendGridConfigRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig emailConfig = createEmailConfigFromSendGrid(tenantId, request);
            EmailConfig savedConfig = tenantConfigService.saveEmailConfig(emailConfig);
            
            tenantEmailService.clearMailSenderCache(tenantId);
            
            return Response.ok().setPayload(maskSensitiveEmailData(savedConfig));
        } catch (Exception e) {
            logger.error("Error configuring SendGrid email for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to configure SendGrid email", e);
        }
    }
    
    @PostMapping("/email/aws-ses")
    @ApiOperation(value = "Configure AWS SES email for tenant", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response configureAwsSesEmail(Principal principal, @Valid @RequestBody AwsSesConfigRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig emailConfig = createEmailConfigFromAwsSes(tenantId, request);
            EmailConfig savedConfig = tenantConfigService.saveEmailConfig(emailConfig);
            
            tenantEmailService.clearMailSenderCache(tenantId);
            
            return Response.ok().setPayload(maskSensitiveEmailData(savedConfig));
        } catch (Exception e) {
            logger.error("Error configuring AWS SES email for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to configure AWS SES email", e);
        }
    }
    
    @GetMapping("/email/current")
    @ApiOperation(value = "Get current email configuration", authorizations = {@Authorization(value = "apiKey")})
    public Response getCurrentEmailConfig(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig config = tenantConfigService.getEmailConfig(tenantId);
            
            if (config != null) {
                return Response.ok().setPayload(maskSensitiveEmailData(config));
            } else {
                return Response.ok().setPayload("No email configuration found for tenant");
            }
        } catch (Exception e) {
            logger.error("Error getting email configuration for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to get email configuration", e);
        }
    }
    
    @PostMapping("/email/test")
    @ApiOperation(value = "Test email configuration", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response testEmailConfig(Principal principal, @RequestBody EmailTestRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            boolean sent = tenantEmailService.sendSimpleEmail(tenantId, request.getTestEmail(),
                    "Email Configuration Test", 
                    "<h2>Email Test Successful</h2><p>Your email configuration is working correctly for tenant: " + tenantId + "</p>");
            
            if (sent) {
                return Response.ok().setPayload("Test email sent successfully to " + request.getTestEmail());
            } else {
                return Response.exception().addErrorMsgToResponse("Failed to send test email", new RuntimeException("Email send failed"));
            }
        } catch (Exception e) {
            logger.error("Error testing email configuration for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to test email configuration", e);
        }
    }
    
    // ===== STORAGE CONFIGURATION ENDPOINTS =====
    
    @PostMapping("/storage/onedrive")
    @ApiOperation(value = "Configure OneDrive storage for tenant", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response configureOneDriveStorage(Principal principal, @Valid @RequestBody OneDriveStorageRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            StorageConfig storageConfig = createStorageConfigFromOneDrive(tenantId, request);
            StorageConfig savedConfig = tenantConfigService.saveStorageConfig(storageConfig);
            
            return Response.ok().setPayload(maskSensitiveStorageData(savedConfig));
        } catch (Exception e) {
            logger.error("Error configuring OneDrive storage for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to configure OneDrive storage", e);
        }
    }
    
    @PostMapping("/storage/google-drive")
    @ApiOperation(value = "Configure Google Drive storage for tenant", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response configureGoogleDriveStorage(Principal principal, @Valid @RequestBody GoogleDriveStorageRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            StorageConfig storageConfig = createStorageConfigFromGoogleDrive(tenantId, request);
            StorageConfig savedConfig = tenantConfigService.saveStorageConfig(storageConfig);
            
            return Response.ok().setPayload(maskSensitiveStorageData(savedConfig));
        } catch (Exception e) {
            logger.error("Error configuring Google Drive storage for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to configure Google Drive storage", e);
        }
    }
    
    @GetMapping("/storage/current")
    @ApiOperation(value = "Get current storage configuration", authorizations = {@Authorization(value = "apiKey")})
    public Response getCurrentStorageConfig(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            StorageConfig config = tenantConfigService.getStorageConfig(tenantId);
            
            if (config != null) {
                return Response.ok().setPayload(maskSensitiveStorageData(config));
            } else {
                return Response.ok().setPayload("No storage configuration found for tenant");
            }
        } catch (Exception e) {
            logger.error("Error getting storage configuration for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to get storage configuration", e);
        }
    }
    
    // ===== GENERAL CONFIGURATION ENDPOINTS =====
    
    @GetMapping("/status")
    @ApiOperation(value = "Get tenant configuration status", authorizations = {@Authorization(value = "apiKey")})
    public Response getTenantConfigStatus(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            TenantConfigService.TenantConfigStatus status = tenantConfigService.getTenantConfigStatus(tenantId);
            return Response.ok().setPayload(status);
        } catch (Exception e) {
            logger.error("Error getting tenant configuration status for {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to get configuration status", e);
        }
    }
    
    @DeleteMapping("/email")
    @ApiOperation(value = "Delete email configuration", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response deleteEmailConfig(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            boolean deleted = tenantConfigService.deleteEmailConfig(tenantId);
            
            if (deleted) {
                tenantEmailService.clearMailSenderCache(tenantId);
                return Response.ok().setPayload("Email configuration deleted successfully");
            } else {
                return Response.notFound().addErrorMsgToResponse("Email configuration not found", new RuntimeException());
            }
        } catch (Exception e) {
            logger.error("Error deleting email configuration for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to delete email configuration", e);
        }
    }
    
    @DeleteMapping("/storage")
    @ApiOperation(value = "Delete storage configuration", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response deleteStorageConfig(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            boolean deleted = tenantConfigService.deleteStorageConfig(tenantId);
            
            if (deleted) {
                return Response.ok().setPayload("Storage configuration deleted successfully");
            } else {
                return Response.notFound().addErrorMsgToResponse("Storage configuration not found", new RuntimeException());
            }
        } catch (Exception e) {
            logger.error("Error deleting storage configuration for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to delete storage configuration", e);
        }
    }
    
    // ===== EMAIL SENDING ENDPOINTS =====
    
    @PostMapping("/email/send")
    @ApiOperation(value = "Send email using tenant configuration", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('RECRUITER')")
    public Response sendEmail(Principal principal, @RequestBody SendEmailRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            boolean sent;
            
            if (request.getTemplateName() != null && request.getTemplateVariables() != null) {
                sent = tenantEmailService.sendTemplatedEmail(tenantId, request.getTo(), 
                        request.getSubject(), request.getTemplateName(), request.getTemplateVariables());
            } else {
                sent = tenantEmailService.sendSimpleEmail(tenantId, request.getTo(), 
                        request.getSubject(), request.getContent());
            }
            
            if (sent) {
                return Response.ok().setPayload("Email sent successfully");
            } else {
                return Response.exception().addErrorMsgToResponse("Failed to send email", new RuntimeException("Email send failed"));
            }
        } catch (Exception e) {
            logger.error("Error sending email for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to send email", e);
        }
    }
    
    @PostMapping("/email/send-bulk")
    @ApiOperation(value = "Send bulk email using tenant configuration", authorizations = {@Authorization(value = "apiKey")})
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public Response sendBulkEmail(Principal principal, @RequestBody List<EmailRequest> request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig emailConfig = tenantConfigService.getEmailConfig(tenantId);
            tenantEmailService.sendBulkEmail(request, emailConfig.getConfigName());
            return Response.ok().setPayload("Bulk email sent successfully to " + request.size() + " recipients");

        } catch (Exception e) {
            logger.error("Error sending bulk email for tenant {}: {}", TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to send bulk email", e);
        }
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private EmailConfig createEmailConfigFromSmtp(String tenantId, SmtpConfigRequest request) {
        EmailConfig config = new EmailConfig();
//        config.setTenantId(tenantId);
        config.setProvider(EmailConfig.EmailServiceProvider.SMTP);
        config.setDefaultFromEmail(request.getFromEmail());
        config.setDefaultFromName(request.getFromName());
        config.setDefaultReplyTo(request.getReplyTo());
        config.setActive(true);
        
        EmailConfig.SmtpConfig smtpConfig = new EmailConfig.SmtpConfig()
                .setHost(request.getHost())
                .setPort(request.getPort())
                .setUsername(request.getUsername())
                .setPassword(request.getPassword())
                .setEnableTls(request.isEnableTls())
                .setEnableSsl(request.isEnableSsl());
        
        config.setSmtpConfig(smtpConfig);
        return config;
    }
    
    private EmailConfig createEmailConfigFromSendGrid(String tenantId, SendGridConfigRequest request) {
        EmailConfig config = new EmailConfig();
//        config.setTenantId(tenantId);
        config.setProvider(EmailConfig.EmailServiceProvider.SENDGRID);
        config.setDefaultFromEmail(request.getFromEmail());
        config.setDefaultFromName(request.getFromName());
        config.setActive(true);
        
        EmailConfig.SendGridConfig sendGridConfig = new EmailConfig.SendGridConfig()
                .setApiKey(request.getApiKey())
                .setFromEmail(request.getFromEmail())
                .setFromName(request.getFromName())
                .setEnableTracking(request.isEnableTracking());
        
        config.setSendGridConfig(sendGridConfig);
        return config;
    }
    
    private EmailConfig createEmailConfigFromAwsSes(String tenantId, AwsSesConfigRequest request) {
        EmailConfig config = new EmailConfig();
//        config.se(tenantId);
        config.setProvider(EmailConfig.EmailServiceProvider.AWS_SES);
        config.setDefaultFromEmail(request.getFromEmail());
        config.setDefaultFromName(request.getFromName());
        config.setActive(true);
        
        EmailConfig.AwsSesConfig awsSesConfig = new EmailConfig.AwsSesConfig()
                .setAccessKey(request.getAccessKey())
                .setSecretKey(request.getSecretKey())
                .setRegion(request.getRegion())
                .setFromEmail(request.getFromEmail())
                .setFromName(request.getFromName());
        
        config.setAwsSesConfig(awsSesConfig);
        return config;
    }
    
    private StorageConfig createStorageConfigFromOneDrive(String tenantId, OneDriveStorageRequest request) {
        StorageConfig config = tenantConfigService.getStorageConfig(tenantId);
        if (config == null) {
            config = new StorageConfig();
            config.setTenantId(tenantId);
            config.setBaseFolderPath("/" + tenantId + "/");
            config.setActive(true);
        }
        
        if (request.isSetPrimary()) {
            config.setPrimaryProvider(StorageConfig.StorageProvider.ONEDRIVE);
        } else if (config.getPrimaryProvider() == null) {
            config.setPrimaryProvider(StorageConfig.StorageProvider.ONEDRIVE);
        }
        
        StorageConfig.OneDriveConfig oneDriveConfig = new StorageConfig.OneDriveConfig()
                .setClientId(request.getClientId())
                .setClientSecret(request.getClientSecret())
                .setTenantId(request.getTenantId())
                .setDriveId(request.getDriveId());
        
        config.setOneDriveConfig(oneDriveConfig);
        config.setEnableBackup(request.isEnableBackup());
        
        return config;
    }
    
    private StorageConfig createStorageConfigFromGoogleDrive(String tenantId, GoogleDriveStorageRequest request) {
        StorageConfig config = tenantConfigService.getStorageConfig(tenantId);
        if (config == null) {
            config = new StorageConfig();
            config.setTenantId(tenantId);
            config.setBaseFolderPath("/" + tenantId + "/");
            config.setActive(true);
        }
        
        if (request.isSetPrimary()) {
            config.setPrimaryProvider(StorageConfig.StorageProvider.GOOGLE_DRIVE);
        } else if (config.getPrimaryProvider() == null) {
            config.setPrimaryProvider(StorageConfig.StorageProvider.GOOGLE_DRIVE);
        }
        
        StorageConfig.GoogleDriveConfig googleDriveConfig = new StorageConfig.GoogleDriveConfig()
                .setServiceAccountKeyPath(request.getServiceAccountKeyPath())
                .setFolderId(request.getFolderId())
                .setCredentialsFilePath(request.getCredentialsFilePath());
        
        config.setGoogleDriveConfig(googleDriveConfig);
        config.setEnableBackup(request.isEnableBackup());
        
        return config;
    }
    
    private EmailConfig maskSensitiveEmailData(EmailConfig config) {
        EmailConfig masked = new EmailConfig();
        // Copy non-sensitive fields
        masked.setId(config.getId());
//        masked.setTenantId(config.getTenantId());
        masked.setProvider(config.getProvider());
        masked.setDefaultFromEmail(config.getDefaultFromEmail());
        masked.setDefaultFromName(config.getDefaultFromName());
        masked.setDefaultReplyTo(config.getDefaultReplyTo());
        masked.setDateCreated(config.getDateCreated());
        masked.setDateModified(config.getDateModified());
        masked.setActive(config.isActive());
        
        // Mask sensitive configurations
        if (config.getSmtpConfig() != null) {
            EmailConfig.SmtpConfig maskedSmtp = new EmailConfig.SmtpConfig();
            maskedSmtp.setHost(config.getSmtpConfig().getHost());
            maskedSmtp.setPort(config.getSmtpConfig().getPort());
            maskedSmtp.setUsername(config.getSmtpConfig().getUsername());
            maskedSmtp.setPassword("****"); // Mask password
            masked.setSmtpConfig(maskedSmtp);
        }
        
        if (config.getSendGridConfig() != null) {
            EmailConfig.SendGridConfig maskedSendGrid = new EmailConfig.SendGridConfig();
            maskedSendGrid.setFromEmail(config.getSendGridConfig().getFromEmail());
            maskedSendGrid.setFromName(config.getSendGridConfig().getFromName());
            maskedSendGrid.setApiKey("****"); // Mask API key
            masked.setSendGridConfig(maskedSendGrid);
        }
        
        return masked;
    }
    
    private StorageConfig maskSensitiveStorageData(StorageConfig config) {
        StorageConfig masked = new StorageConfig();
        // Copy non-sensitive fields
        masked.setId(config.getId());
        masked.setTenantId(config.getTenantId());
        masked.setPrimaryProvider(config.getPrimaryProvider());
        masked.setSecondaryProvider(config.getSecondaryProvider());
        masked.setBaseFolderPath(config.getBaseFolderPath());
        masked.setEnableBackup(config.isEnableBackup());
        masked.setDateCreated(config.getDateCreated());
        masked.setDateModified(config.getDateModified());
        masked.setActive(config.isActive());
        
        // Mask sensitive configurations
        if (config.getOneDriveConfig() != null) {
            StorageConfig.OneDriveConfig maskedOneDrive = new StorageConfig.OneDriveConfig();
            maskedOneDrive.setClientId(maskString(config.getOneDriveConfig().getClientId()));
            maskedOneDrive.setTenantId(config.getOneDriveConfig().getTenantId());
            maskedOneDrive.setDriveId(config.getOneDriveConfig().getDriveId());
            // Don't include sensitive data
            masked.setOneDriveConfig(maskedOneDrive);
        }
        
        return masked;
    }
    
    private String maskString(String input) {
        if (input == null || input.length() < 8) {
            return "****";
        }
        return input.substring(0, 4) + "****" + input.substring(input.length() - 4);
    }
    
    @ModelAttribute
    public void setTenantContext(HttpServletRequest request) {
        String tenantId = request.getHeader("X-Tenant");
        logger.debug("Setting tenant context in config controller: {}", tenantId);
        TenantContext.setCurrentTenant(tenantId);
    }
    
    // ===== REQUEST/RESPONSE DTOs =====
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class SmtpConfigRequest {
        @javax.validation.constraints.NotBlank
        private String host;
        private int port = 587;
        @javax.validation.constraints.NotBlank
        private String username;
        @javax.validation.constraints.NotBlank
        private String password;
        @javax.validation.constraints.NotBlank
        private String fromEmail;
        private String fromName;
        private String replyTo;
        private boolean enableTls = true;
        private boolean enableSsl = false;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class SendGridConfigRequest {
        @javax.validation.constraints.NotBlank
        private String apiKey;
        @javax.validation.constraints.NotBlank
        private String fromEmail;
        private String fromName;
        private boolean enableTracking = true;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class AwsSesConfigRequest {
        @javax.validation.constraints.NotBlank
        private String accessKey;
        @javax.validation.constraints.NotBlank
        private String secretKey;
        @javax.validation.constraints.NotBlank
        private String region;
        @javax.validation.constraints.NotBlank
        private String fromEmail;
        private String fromName;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class OneDriveStorageRequest {
        @javax.validation.constraints.NotBlank
        private String clientId;
        @javax.validation.constraints.NotBlank
        private String clientSecret;
        @javax.validation.constraints.NotBlank
        private String tenantId;
        private String driveId;
        private boolean setPrimary = true;
        private boolean enableBackup = false;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class GoogleDriveStorageRequest {
        private String serviceAccountKeyPath;
        private String credentialsFilePath;
        private String folderId;
        private boolean setPrimary = false;
        private boolean enableBackup = true;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class EmailTestRequest {
        @javax.validation.constraints.NotBlank
        @javax.validation.constraints.Email
        private String testEmail;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class SendEmailRequest {
        @javax.validation.constraints.NotBlank
        private String to;
        @javax.validation.constraints.NotBlank
        private String subject;
        private String content;
        private String templateName;
        private Map<String, Object> templateVariables;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    public static class SendBulkEmailRequest {
        @javax.validation.constraints.NotEmpty
        private List<String> recipients;
        @javax.validation.constraints.NotBlank
        private String subject;
        @javax.validation.constraints.NotBlank
        private String content;
    }
}