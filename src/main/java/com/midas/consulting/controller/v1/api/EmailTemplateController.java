package com.midas.consulting.controller.v1.api;

//import com.midas.consulting.dto.response.Response;
//import com.midas.consulting.model.EmailTemplate;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.model.tenant.EmailTemplate;
import com.midas.consulting.service.EmailTemplateService;
import com.midas.consulting.service.TenantContext;
//import com.midas.consulting.service.storage.EmailTemplateService;
import com.midas.consulting.service.storage.TenantConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/email-templates")
@Api(value = "email-template-management", description = "Dynamic Email Template CRUD Operations")
public class EmailTemplateController {

    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateController.class);

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private TenantConfigService tenantConfigService;

    // ===== TEMPLATE CRUD OPERATIONS =====

    @PostMapping
    @ApiOperation(value = "Create new email template", authorizations = {@Authorization(value = "apiKey")})
    public Response createTemplate(Principal principal, @Valid @RequestBody CreateTemplateRequest request) {
        try {
            String tenantId = TenantContext.getCurrentTenant();

            // Validate email configuration exists
            if (!tenantConfigService.isEmailConfigured(tenantId)) {
                return Response.badRequest()
                        .addErrorMsgToResponse("Email configuration required before creating templates",
                                new RuntimeException("No email configuration found"));
            }

            EmailTemplate template = mapToTemplate(request);
            EmailTemplate savedTemplate = emailTemplateService.createTemplate(template);

            return Response.ok().setPayload(maskSensitiveTemplateData(savedTemplate));

        } catch (Exception e) {
            logger.error("Error creating email template for tenant {}: {}",
                    TenantContext.getCurrentTenant(), e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to create email template", e);
        }
    }

    @PutMapping("/{templateId}")
    @ApiOperation(value = "Update existing email template", authorizations = {@Authorization(value = "apiKey")})
    public Response updateTemplate(
            Principal principal,
            @PathVariable String templateId,
            @Valid @RequestBody UpdateTemplateRequest request) {
        try {
            EmailTemplate template = mapToTemplate(request);
            EmailTemplate updatedTemplate = emailTemplateService.updateTemplate(templateId, template);

            if (updatedTemplate != null) {
                return Response.ok().setPayload(maskSensitiveTemplateData(updatedTemplate));
            } else {
                return Response.notFound()
                        .addErrorMsgToResponse("Template not found", new RuntimeException());
            }

        } catch (Exception e) {
            logger.error("Error updating email template {}: {}", templateId, e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to update email template", e);
        }
    }

    @GetMapping("/{templateId}")
    @ApiOperation(value = "Get email template by ID", authorizations = {@Authorization(value = "apiKey")})
    public Response getTemplateById(Principal principal, @PathVariable String templateId) {
        try {
            EmailTemplate template = emailTemplateService.getTemplateById(templateId);

            if (template != null) {
                return Response.ok().setPayload(maskSensitiveTemplateData(template));
            } else {
                return Response.notFound()
                        .addErrorMsgToResponse("Template not found", new RuntimeException());
            }

        } catch (Exception e) {
            logger.error("Error getting email template {}: {}", templateId, e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to get email template", e);
        }
    }

    @GetMapping("/name/{templateName}")
    @ApiOperation(value = "Get email template by name", authorizations = {@Authorization(value = "apiKey")})
    public Response getTemplateByName(Principal principal, @PathVariable String templateName) {
        try {
            EmailTemplate template = emailTemplateService.getTemplateByName(templateName);

            if (template != null) {
                return Response.ok().setPayload(maskSensitiveTemplateData(template));
            } else {
                return Response.notFound()
                        .addErrorMsgToResponse("Template not found with name: " + templateName,
                                new RuntimeException());
            }

        } catch (Exception e) {
            logger.error("Error getting email template by name {}: {}", templateName, e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to get email template", e);
        }
    }

    @GetMapping
    @ApiOperation(value = "Get paginated list of email templates", authorizations = {@Authorization(value = "apiKey")})
    public Response getTemplates(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<EmailTemplate> templates = emailTemplateService.getTemplates(pageable, category, search, active);

            // Mask sensitive data for all templates
            Page<EmailTemplate> maskedTemplates = templates.map(this::maskSensitiveTemplateData);

            Map<String, Object> result = new HashMap<>();
            result.put("templates", maskedTemplates.getContent());
            result.put("totalElements", maskedTemplates.getTotalElements());
            result.put("totalPages", maskedTemplates.getTotalPages());
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("hasNext", maskedTemplates.hasNext());
            result.put("hasPrevious", maskedTemplates.hasPrevious());

            return Response.ok().setPayload(result);

        } catch (Exception e) {
            logger.error("Error getting email templates: {}", e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to get email templates", e);
        }
    }

    @DeleteMapping("/{templateId}")
    @ApiOperation(value = "Delete email template", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteTemplate(Principal principal, @PathVariable String templateId) {
        try {
            boolean deleted = emailTemplateService.deleteTemplate(templateId);

            if (deleted) {
                return Response.ok().setPayload("Template deleted successfully");
            } else {
                return Response.notFound()
                        .addErrorMsgToResponse("Template not found", new RuntimeException());
            }

        } catch (Exception e) {
            logger.error("Error deleting email template {}: {}", templateId, e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to delete email template", e);
        }
    }

    // ===== TEMPLATE PROCESSING AND VALIDATION =====

    @PostMapping("/{templateName}/process")
    @ApiOperation(value = "Process template with variables", authorizations = {@Authorization(value = "apiKey")})
    public Response processTemplate(
            Principal principal,
            @PathVariable String templateName,
            @RequestBody Map<String, Object> variables) {
        try {
            String tenantId = TenantContext.getCurrentTenant();

            // Validate email configuration
            if (!tenantConfigService.isEmailConfigured(tenantId)) {
                return Response.badRequest()
                        .addErrorMsgToResponse("Email configuration required",
                                new RuntimeException("No email configuration found"));
            }

            String processedContent = emailTemplateService.processTemplate(templateName, variables);
            String processedSubject = emailTemplateService.processSubject(templateName, variables);

            Map<String, Object> result = new HashMap<>();
            result.put("subject", processedSubject);
            result.put("content", processedContent);
            result.put("templateName", templateName);

            return Response.ok().setPayload(result);

        } catch (Exception e) {
            logger.error("Error processing template {}: {}", templateName, e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to process template", e);
        }
    }

    @PostMapping("/{templateName}/preview")
    @ApiOperation(value = "Preview template with sample data", authorizations = {@Authorization(value = "apiKey")})
    public Response previewTemplate(
            Principal principal,
            @PathVariable String templateName,
            @RequestBody(required = false) Map<String, Object> sampleData) {
        try {
            if (sampleData == null) {
                sampleData = generateSampleData();
            }

            EmailTemplate preview = emailTemplateService.previewTemplate(templateName, sampleData);
            return Response.ok().setPayload(preview);

        } catch (Exception e) {
            logger.error("Error previewing template {}: {}", templateName, e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to preview template", e);
        }
    }

    @PostMapping("/validate")
    @ApiOperation(value = "Validate email template", authorizations = {@Authorization(value = "apiKey")})
    public Response validateTemplate(Principal principal, @Valid @RequestBody EmailTemplate template) {
        try {
            List<String> errors = emailTemplateService.validateTemplate(template);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", errors.isEmpty());
            result.put("errors", errors);

            if (!errors.isEmpty()) {
                return Response.badRequest().setPayload(result);
            }

            return Response.ok().setPayload(result);

        } catch (Exception e) {
            logger.error("Error validating template: {}", e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to validate template", e);
        }
    }

    // ===== TEMPLATE CATEGORIES AND METADATA =====

    @GetMapping("/categories")
    @ApiOperation(value = "Get available template categories", authorizations = {@Authorization(value = "apiKey")})
    public Response getTemplateCategories(Principal principal) {
        try {
            EmailTemplate.TemplateCategory[] categories = EmailTemplate.TemplateCategory.values();
            return Response.ok().setPayload(categories);

        } catch (Exception e) {
            logger.error("Error getting template categories: {}", e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to get template categories", e);
        }
    }

    @GetMapping("/variable-types")
    @ApiOperation(value = "Get available variable types", authorizations = {@Authorization(value = "apiKey")})
    public Response getVariableTypes(Principal principal) {
        try {
            EmailTemplate.VariableType[] types = EmailTemplate.VariableType.values();
            return Response.ok().setPayload(types);

        } catch (Exception e) {
            logger.error("Error getting variable types: {}", e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to get variable types", e);
        }
    }

    // ===== SYSTEM TEMPLATES =====

    @PostMapping("/initialize-defaults")
    @ApiOperation(value = "Initialize default system templates", authorizations = {@Authorization(value = "apiKey")})
    public Response initializeDefaultTemplates(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            emailTemplateService.createDefaultTemplates(tenantId);

            return Response.ok().setPayload("Default templates initialized successfully");

        } catch (Exception e) {
            logger.error("Error initializing default templates: {}", e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to initialize default templates", e);
        }
    }

    // ===== TEMPLATE CACHE MANAGEMENT =====

    @PostMapping("/cache/clear")
    @ApiOperation(value = "Clear template cache", authorizations = {@Authorization(value = "apiKey")})
    public Response clearTemplateCache(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            emailTemplateService.clearTemplateCache(tenantId);

            return Response.ok().setPayload("Template cache cleared successfully");

        } catch (Exception e) {
            logger.error("Error clearing template cache: {}", e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to clear template cache", e);
        }
    }

    // ===== CONFIGURATION VALIDATION =====

    @GetMapping("/config/validation")
    @ApiOperation(value = "Validate email configuration for templates", authorizations = {@Authorization(value = "apiKey")})
    public Response validateEmailConfiguration(Principal principal) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            TenantConfigService.TenantConfigStatus status = tenantConfigService.getTenantConfigStatus(tenantId);

            Map<String, Object> result = new HashMap<>();
            result.put("emailConfigured", status.isEmailConfigured());
            result.put("canCreateTemplates", status.isEmailConfigured());

            if (status.isEmailConfigured()) {
                result.put("emailProvider", status.getEmailProvider());
                result.put("fromEmail", status.getDefaultFromEmail());
                result.put("message", "Email configuration is valid - templates can be created and used");
            } else {
                result.put("message", "Email configuration required before creating templates");
                result.put("configurationUrl", "/api/v1/tenant-config/email");
            }

            return Response.ok().setPayload(result);

        } catch (Exception e) {
            logger.error("Error validating email configuration: {}", e.getMessage(), e);
            return Response.exception()
                    .addErrorMsgToResponse("Failed to validate email configuration", e);
        }
    }

    // ===== HELPER METHODS =====

    private EmailTemplate mapToTemplate(CreateTemplateRequest request) {
        EmailTemplate template = new EmailTemplate()
                .setTemplateName(request.getTemplateName())
                .setSubject(request.getSubject())
                .setHtmlContent(request.getHtmlContent())
                .setTextContent(request.getTextContent())
                .setDescription(request.getDescription())
                .setTemplateType(request.getTemplateType())
                .setCategory(request.getCategory())
                .setActive(request.isActive());

        if (request.getVariables() != null) {
            template.setVariables(request.getVariables());
        }

        if (request.getDefaultValues() != null) {
            template.setDefaultValues(request.getDefaultValues());
        }

        return template;
    }

    private EmailTemplate mapToTemplate(UpdateTemplateRequest request) {
        EmailTemplate template = new EmailTemplate()
                .setTemplateName(request.getTemplateName())
                .setSubject(request.getSubject())
                .setHtmlContent(request.getHtmlContent())
                .setTextContent(request.getTextContent())
                .setDescription(request.getDescription())
                .setTemplateType(request.getTemplateType())
                .setCategory(request.getCategory())
                .setActive(request.isActive());

        if (request.getVariables() != null) {
            template.setVariables(request.getVariables());
        }

        if (request.getDefaultValues() != null) {
            template.setDefaultValues(request.getDefaultValues());
        }

        return template;
    }

    private EmailTemplate maskSensitiveTemplateData(EmailTemplate template) {
        // Create a copy to avoid modifying the original
        EmailTemplate masked = new EmailTemplate();
        masked.setId(template.getId());
        masked.setTenantId(template.getTenantId());
        masked.setTemplateName(template.getTemplateName());
        masked.setSubject(template.getSubject());
        masked.setHtmlContent(template.getHtmlContent());
        masked.setTextContent(template.getTextContent());
        masked.setDescription(template.getDescription());
        masked.setTemplateType(template.getTemplateType());
        masked.setCategory(template.getCategory());
        masked.setVariables(template.getVariables());
        masked.setDefaultValues(template.getDefaultValues());
        masked.setMetadata(template.getMetadata());
        masked.setActive(template.isActive());
        masked.setSystem(template.isSystem());
        masked.setDateCreated(template.getDateCreated());
        masked.setDateModified(template.getDateModified());
        masked.setVersion(template.getVersion());
        masked.setUsageCount(template.getUsageCount());
        masked.setLastUsed(template.getLastUsed());

        return masked;
    }

    private Map<String, Object> generateSampleData() {
        Map<String, Object> sampleData = new HashMap<>();
        sampleData.put("tenantName", "Sample Company");
        sampleData.put("logoUrl", "https://via.placeholder.com/200x60/4299E1/FFFFFF?text=Logo");
        sampleData.put("userName", "John Doe");
        sampleData.put("userEmail", "john.doe@example.com");
        sampleData.put("userRole", "Recruiter");
        sampleData.put("candidateName", "Jane Smith");
        sampleData.put("jobTitle", "Software Engineer");
        sampleData.put("applicationId", "APP-2024-001");
        sampleData.put("resetLink", "https://example.com/reset-password");
        sampleData.put("loginUrl", "https://example.com/login");
        sampleData.put("tempPassword", "TempPass123!");
        sampleData.put("interviewDate", "2024-03-15");
        sampleData.put("interviewTime", "2:00 PM EST");
        sampleData.put("duration", "45 minutes");
        sampleData.put("interviewType", "Video Interview");
        sampleData.put("location", "Zoom Meeting");
        sampleData.put("interviewerName", "Mike Johnson");
        sampleData.put("instructions", "Please join the meeting 5 minutes early and ensure you have a stable internet connection.");
        sampleData.put("confirmationLink", "https://example.com/confirm-interview");
        sampleData.put("rescheduleLink", "https://example.com/reschedule-interview");

        return sampleData;
    }

    @ModelAttribute
    public void setTenantContext(HttpServletRequest request) {
        String tenantId = request.getHeader("X-Tenant");
        logger.debug("Setting tenant context in template controller: {}", tenantId);
        TenantContext.setCurrentTenant(tenantId);
    }

    // ===== REQUEST/RESPONSE DTOs =====

    @Data
    @NoArgsConstructor
    public static class CreateTemplateRequest {
        @NotBlank(message = "Template name is required")
        private String templateName;

        @NotBlank(message = "Subject is required")
        private String subject;

        @NotBlank(message = "HTML content is required")
        private String htmlContent;

        private String textContent;
        private String description;

        @NotNull(message = "Template type is required")
        private EmailTemplate.TemplateType templateType = EmailTemplate.TemplateType.HTML;

        private EmailTemplate.TemplateCategory category = EmailTemplate.TemplateCategory.CUSTOM;
        private Map<String, EmailTemplate.TemplateVariable> variables;
        private Map<String, Object> defaultValues;
        private boolean active = true;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateTemplateRequest {
        @NotBlank(message = "Template name is required")
        private String templateName;

        @NotBlank(message = "Subject is required")
        private String subject;

        @NotBlank(message = "HTML content is required")
        private String htmlContent;

        private String textContent;
        private String description;

        @NotNull(message = "Template type is required")
        private EmailTemplate.TemplateType templateType;

        private EmailTemplate.TemplateCategory category;
        private Map<String, EmailTemplate.TemplateVariable> variables;
        private Map<String, Object> defaultValues;
        private boolean active;
    }
}