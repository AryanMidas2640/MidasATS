package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.EmailRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.model.EmailTemplateMapping;
import com.midas.consulting.service.EnhancedEmailConfigService;
import com.midas.consulting.service.EnhancedTenantEmailService;
import com.midas.consulting.service.TenantContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/email-configs")
@Api(value = "email-configuration", description = "Operations for managing multiple email configurations")
public class EmailConfigurationController {

    private static final Logger logger = LoggerFactory.getLogger(EmailConfigurationController.class);

    @Autowired
    private EnhancedEmailConfigService emailConfigService;

    @Autowired
    private EnhancedTenantEmailService emailService;

    // ===== EMAIL CONFIGURATION ENDPOINTS =====


    @DeleteMapping("/configs/{id}")
    @ApiOperation(value = "Delete email configuration", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteEmailConfig(Principal principal, @PathVariable String id) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig existingConfig = emailConfigService.getEmailConfigById(id);

            if (existingConfig == null) {
                return Response.notFound().addErrorMsgToResponse("Email configuration not found", new RuntimeException());
            }

            // Optional: Check if config is in use before deleting
            // You might want to prevent deletion if templates are mapped to this config

            emailConfigService.deleteEmailConfigWithMappings(id);

            logger.info("Email configuration deleted: {} by user: {}", id, principal.getName());

            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("configName", existingConfig.getConfigName());
            result.put("message", "Email configuration deleted successfully");

            return Response.ok().setPayload(result);
        } catch (Exception e) {
            logger.error("Error deleting email configuration: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to delete email configuration", e);
        }
    }

    @PostMapping("/configs")
    @ApiOperation(value = "Create a new email configuration", authorizations = {@Authorization(value = "apiKey")})
    public Response createEmailConfig(Principal principal,
                                      @Valid @RequestBody CreateEmailConfigRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();

            EmailConfig emailConfig = new EmailConfig();
//            emailConfig.setTenantId(tenantId);
            emailConfig.setConfigName(request.getConfigName());
            emailConfig.setDescription(request.getDescription());
            emailConfig.setEmailAddress(request.getEmailAddress());
            emailConfig.setDisplayName(request.getDisplayName());
            emailConfig.setTemplateMappings(request.getTemplateMappings());
            emailConfig.setPriority(request.getPriority());
            emailConfig.setActive(true);
            emailConfig.setCcList(request.getCcList());
            emailConfig.setBccList(request.getBccList());
            emailConfig.setCreatedBy(principal.getName());

            // Set provider configuration
            emailConfig.setProvider(request.getProvider());

            switch (request.getProvider()) {
                case SMTP:
                    emailConfig.setSmtpConfig(request.getSmtpConfig());
                    break;
                case SENDGRID:
                    emailConfig.setSendGridConfig(request.getSendGridConfig());
                    break;
                case AWS_SES:
                    emailConfig.setAwsSesConfig(request.getAwsSesConfig());
                    break;
                // Add other providers as needed
            }

            EmailConfig savedConfig = emailConfigService.saveEmailConfig(emailConfig);

            return Response.ok().setPayload(maskSensitiveData(savedConfig));
        } catch (Exception e) {
            logger.error("Error creating email configuration: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to create email configuration", e);
        }
    }

    @GetMapping("/configs")
    @ApiOperation(value = "Get all email configurations for tenant", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllEmailConfigs(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            List<EmailConfig> configs = emailConfigService.getTenantEmailConfigs(tenantId);

            // Mask sensitive data in list
            configs.forEach(this::maskSensitiveData);

            return Response.ok().setPayload(configs);
        } catch (Exception e) {
            logger.error("Error getting email configurations: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to get email configurations", e);
        }
    }

    @GetMapping("/configs/{configName}")
    @ApiOperation(value = "Get email configuration by name", authorizations = {@Authorization(value = "apiKey")})
    public Response getEmailConfigByName(Principal principal, @PathVariable String configName) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig config = emailConfigService.getEmailConfigByName(tenantId, configName);

            if (config != null) {
                return Response.ok().setPayload(maskSensitiveData(config));
            } else {
                return Response.notFound().addErrorMsgToResponse("Email configuration not found", new RuntimeException());
            }
        } catch (Exception e) {
            logger.error("Error getting email configuration: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to get email configuration", e);
        }
    }


    // ===== FIXED PUT MAPPING =====

    @PutMapping("/configs/{id}")
    @ApiOperation(value = "Update email configuration", authorizations = {@Authorization(value = "apiKey")})
    public Response updateEmailConfig(Principal principal,
                                      @PathVariable String id,
                                      @Valid @RequestBody com.midas.consulting.controller.v1.request.UpdateEmailConfigRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig existingConfig = emailConfigService.getEmailConfigById( id);

            if (existingConfig == null) {
                return Response.notFound().addErrorMsgToResponse("Email configuration not found", new RuntimeException());
            }

            // Update basic fields
            if (request.getDescription() != null) {
                existingConfig.setDescription(request.getDescription());
            }
            if (request.getDisplayName() != null) {
                existingConfig.setDisplayName(request.getDisplayName());
            }
            if (request.getTemplateMappings() != null) {
                existingConfig.setTemplateMappings(request.getTemplateMappings());
            }
            if (request.getPriority() != null) {
                existingConfig.setPriority(request.getPriority());
            }
            if (request.getCcList() != null) {
                existingConfig.setCcList(request.getCcList());
            }
            if (request.getBccList() != null) {
                existingConfig.setBccList(request.getBccList());
            }

            // Update provider configurations if provided
            if (request.getSmtpConfig() != null) {
                existingConfig.setProvider(EmailConfig.EmailServiceProvider.SMTP);
                existingConfig.setSmtpConfig(request.getSmtpConfig());
                // Clear other provider configs
                existingConfig.setSendGridConfig(null);
                existingConfig.setAwsSesConfig(null);
            }
            if (request.getSendGridConfig() != null) {
                existingConfig.setProvider(EmailConfig.EmailServiceProvider.SENDGRID);
                existingConfig.setSendGridConfig(request.getSendGridConfig());
                // Clear other provider configs
                existingConfig.setSmtpConfig(null);
                existingConfig.setAwsSesConfig(null);
            }
            if (request.getAwsSesConfig() != null) {
                existingConfig.setProvider(EmailConfig.EmailServiceProvider.AWS_SES);
                existingConfig.setAwsSesConfig(request.getAwsSesConfig());
                // Clear other provider configs
                existingConfig.setSmtpConfig(null);
                existingConfig.setSendGridConfig(null);
            }

            // Update active status if provided
            if (request.getActive() != null) {
                existingConfig.setActive(request.getActive());
            }

            // Set audit fields
            existingConfig.setModifiedBy(principal.getName());
            existingConfig.setDateModified(new Date()); // Add this field to your EmailConfig model if missing

            // Save updated configuration
            EmailConfig updatedConfig = emailConfigService.saveEmailConfig(existingConfig);

            return Response.ok().setPayload(maskSensitiveData(updatedConfig));
        } catch (Exception e) {
            logger.error("Error updating email configuration: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to update email configuration", e);
        }
    }

// ===== UPDATED REQUEST DTO =====

    @Data
    @NoArgsConstructor
    public static class UpdateEmailConfigRequest {
        private String description;
        private String displayName;
        private List<String> templateMappings;
        private List<String> ccList;
        private List<String> bccList;
        private Integer priority;
        private Boolean active;

        // Provider configurations for updating email settings
        private EmailConfig.SmtpConfig smtpConfig;
        private EmailConfig.SendGridConfig sendGridConfig;
        private EmailConfig.AwsSesConfig awsSesConfig;
    }

// ===== ALSO FIX THE POST MAPPING =====



    // ===== TEMPLATE MAPPING ENDPOINTS =====

    @PostMapping("/template-mappings")
    @ApiOperation(value = "Create or update template mapping", authorizations = {@Authorization(value = "apiKey")})
    public Response createTemplateMapping(Principal principal, @Valid @RequestBody CreateTemplateMappingRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();

            EmailTemplateMapping mapping = new EmailTemplateMapping();
//            mapping.setTenantId(tenantId);
            mapping.setTemplateName(request.getTemplateName());
            mapping.setEmailConfigId(request.getEmailConfigId());
            mapping.setEmailConfigName(request.getEmailConfigName());
            mapping.setOverrides(request.getOverrides());
            mapping.setSettings(request.getSettings());
            mapping.setActive(true);

            EmailTemplateMapping savedMapping = emailConfigService.saveTemplateMapping(mapping);

            return Response.ok().setPayload(savedMapping);
        } catch (Exception e) {
            logger.error("Error creating template mapping: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to create template mapping", e);
        }
    }

    @PostMapping("/template-mappings/bulk")
    @ApiOperation(value = "Bulk update template mappings", authorizations = {@Authorization(value = "apiKey")})
    public Response bulkUpdateTemplateMappings(Principal principal, @Valid @RequestBody BulkTemplateMappingRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();

            emailConfigService.updateTemplateMappings(
                    tenantId,
                    request.getEmailConfigId(),
                    request.getTemplateNames()
            );

            return Response.ok().setPayload("Template mappings updated successfully");
        } catch (Exception e) {
            logger.error("Error updating template mappings: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to update template mappings", e);
        }
    }

    // ===== EMAIL SENDING ENDPOINTS =====

    @PostMapping("/send")
    @ApiOperation(value = "Send email using specific configuration", authorizations = {@Authorization(value = "apiKey")})
    public Response sendEmail(Principal principal, @Valid @RequestBody SendEmailWithConfigRequest request) {
        try {
            emailService.sendEmailWithConfig(request.getConfigName(), request.getEmailRequest());
            return Response.ok().setPayload("Email sent successfully");
        } catch (Exception e) {
            logger.error("Error sending email: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to send email", e);
        }
    }

    @PostMapping("/test/{configName}")
    @ApiOperation(value = "Test email configuration", authorizations = {@Authorization(value = "apiKey")})
    public Response testEmailConfig(Principal principal,
                                    @PathVariable String configName,
                                    @RequestBody TestEmailRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EmailConfig config = emailConfigService.getEmailConfigByName(tenantId, configName);

            if (config == null) {
                return Response.notFound().addErrorMsgToResponse("Email configuration not found", new RuntimeException());
            }

            boolean testResult = emailService.testEmailConfiguration(tenantId);

            Map<String, Object> result = new HashMap<>();
            result.put("configName", configName);
            result.put("testPassed", testResult);
            result.put("message", testResult ? "Configuration test successful" : "Configuration test failed");

            return Response.ok().setPayload(result);
        } catch (Exception e) {
            logger.error("Error testing email configuration: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to test email configuration", e);
        }
    }

    // ===== STATUS ENDPOINTS =====

    @GetMapping("/status")
    @ApiOperation(value = "Get email configuration status", authorizations = {@Authorization(value = "apiKey")})
    public Response getEmailConfigStatus(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            EnhancedEmailConfigService.EmailConfigStatus status = emailConfigService.getTenantEmailConfigStatus(tenantId);
            return Response.ok().setPayload(status);
        } catch (Exception e) {
            logger.error("Error getting email configuration status: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to get configuration status", e);
        }
    }

    // ===== HELPER METHODS =====

    private EmailConfig maskSensitiveData(EmailConfig config) {
        EmailConfig masked = new EmailConfig();
        // Copy non-sensitive fields
        masked.setId(config.getId());
//        masked.setTenantId(config.getTenantId());
        masked.setConfigName(config.getConfigName());
        masked.setDescription(config.getDescription());
        masked.setEmailAddress(config.getEmailAddress());
        masked.setDisplayName(config.getDisplayName());
        masked.setTemplateMappings(config.getTemplateMappings());
        masked.setPriority(config.getPriority());
        masked.setProvider(config.getProvider());
        masked.setActive(config.isActive());
        masked.setDefaultFromEmail(config.getDefaultFromEmail());
        masked.setDateCreated(config.getDateCreated());
        masked.setDateModified(config.getDateModified());
masked.setCcList(config.getCcList());
masked.setBccList(config.getBccList());
        // Mask sensitive configurations
        if (config.getSmtpConfig() != null) {
            EmailConfig.SmtpConfig maskedSmtp = new EmailConfig.SmtpConfig();
            maskedSmtp.setHost(config.getSmtpConfig().getHost());
            maskedSmtp.setPort(config.getSmtpConfig().getPort());
            maskedSmtp.setUsername(config.getSmtpConfig().getUsername());
            maskedSmtp.setPassword(config.getSmtpConfig().getPassword());
            masked.setSmtpConfig(maskedSmtp);
        }

        return masked;
    }

    // ===== REQUEST DTOs =====

    @Data
    @NoArgsConstructor
    public static class CreateEmailConfigRequest {
        @NotBlank
        private String configName;

        private String description;


        private List<String> ccList;
        private List<String> bccList;

        @NotBlank
        @Email
        private String emailAddress;

        private String displayName;

        private List<String> templateMappings;

        private int priority = 100;

        @NotNull
        private EmailConfig.EmailServiceProvider provider;

        private EmailConfig.SmtpConfig smtpConfig;
        private EmailConfig.SendGridConfig sendGridConfig;
        private EmailConfig.AwsSesConfig awsSesConfig;
    }



    @Data
    @NoArgsConstructor
    public static class CreateTemplateMappingRequest {
        @NotBlank
        private String templateName;

        private String emailConfigId;
        private String emailConfigName;

        private EmailTemplateMapping.TemplateOverrides overrides;
        private EmailTemplateMapping.TemplateSettings settings;
    }

    @Data
    @NoArgsConstructor
    public static class BulkTemplateMappingRequest {
        @NotBlank
        private String emailConfigId;

        @NotEmpty
        private List<String> templateNames;
    }

    @Data
    @NoArgsConstructor
    public static class SendEmailWithConfigRequest {
        @NotBlank
        private String configName;

        @NotNull
        private EmailRequest emailRequest;
    }

    @Data
    @NoArgsConstructor
    public static class TestEmailRequest {
        @Email
        private String testRecipient;
    }
}