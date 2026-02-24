package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.tenant.EmailTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class EmailTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(EmailTemplateService.class);

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    @Autowired
    private EnhancedEmailConfigService tenantConfigService;

    // Cache for compiled templates
    private final Map<String, EmailTemplate> templateCache = new ConcurrentHashMap<>();

    // Pattern to find template variables like {{variableName}}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+)\\s*\\}\\}");

    private void validateEmailConfigExists(String tenantId) {
        if (!tenantConfigService.isEmailConfigured(tenantId)) {
            throw new RuntimeException("No email configuration found for tenant: " + tenantId +
                    ". Please configure email settings before using templates.");
        }
    }

    // ===== CRUD OPERATIONS =====

    public EmailTemplate createTemplate(EmailTemplate template) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            if (tenantId == null) {
                throw new IllegalStateException("No tenant context available");
            }

            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Check for duplicate template names within tenant
            if (templateExistsByName(template.getTemplateName())) {
                throw new RuntimeException("Template with name '" + template.getTemplateName() + "' already exists for this tenant");
            }

            // Set tenant and audit fields
            template.setTenantId(tenantId);
            template.setDateCreated(new Date());
            template.setDateModified(new Date());

            // Auto-detect and set template variables
            template.setVariables(extractTemplateVariables(template.getHtmlContent()));

            // Validate template syntax
            validateTemplateSyntax(template);

            EmailTemplate savedTemplate = mongoTemplate.save(template);

            // Update cache
            updateTemplateCache(savedTemplate);

            logger.info("Created email template: {} for tenant: {}",
                    savedTemplate.getTemplateName(), tenantId);

            return savedTemplate;

        } catch (Exception e) {
            logger.error("Error creating email template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create email template: " + e.getMessage(), e);
        }
    }

    public EmailTemplate updateTemplate(String templateId, EmailTemplate template) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Find existing template
            EmailTemplate existingTemplate = getTemplateById(templateId);
            if (existingTemplate == null) {
                throw new RuntimeException("Template not found with ID: " + templateId);
            }

            // Check if it's a system template
            if (existingTemplate.isSystem()) {
                throw new RuntimeException("Cannot modify system template");
            }

            // Update fields
            template.setId(templateId);
            template.setTenantId(tenantId);
            template.setDateCreated(existingTemplate.getDateCreated());
            template.setDateModified(new Date());
            template.setCreatedBy(existingTemplate.getCreatedBy());
            template.setUsageCount(existingTemplate.getUsageCount());
            template.setLastUsed(existingTemplate.getLastUsed());

            // Auto-detect and set template variables
            template.setVariables(extractTemplateVariables(template.getHtmlContent()));

            // Validate template syntax
            validateTemplateSyntax(template);

            // Increment version if content changed
            if (!existingTemplate.getHtmlContent().equals(template.getHtmlContent()) ||
                    !existingTemplate.getSubject().equals(template.getSubject())) {
                template.setVersion(existingTemplate.getVersion() + 1);
            } else {
                template.setVersion(existingTemplate.getVersion());
            }

            EmailTemplate savedTemplate = mongoTemplate.save(template);

            // Update cache
            updateTemplateCache(savedTemplate);

            logger.info("Updated email template: {} for tenant: {}",
                    savedTemplate.getTemplateName(), tenantId);

            return savedTemplate;

        } catch (Exception e) {
            logger.error("Error updating email template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update email template: " + e.getMessage(), e);
        }
    }

    public EmailTemplate getTemplateById(String templateId) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("id").is(templateId)
                    .and("tenantId").is(tenantId));

            return mongoTemplate.findOne(query, EmailTemplate.class);

        } catch (Exception e) {
            logger.error("Error getting template by ID: {}", e.getMessage(), e);
            return null;
        }
    }

    public EmailTemplate getTemplateByName(String templateName) {
        try {
            String tenantId = TenantContext.getCurrentTenant();

            // Check cache first
            String cacheKey = tenantId + ":" + templateName;
            if (templateCache.containsKey(cacheKey)) {
                return templateCache.get(cacheKey);
            }

            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("templateName").is(templateName)
                    .and("tenantId").is(tenantId)
                    .and("active").is(true));

            EmailTemplate template = mongoTemplate.findOne(query, EmailTemplate.class);

            // Cache the result
            if (template != null) {
                templateCache.put(cacheKey, template);
            }

            return template;

        } catch (Exception e) {
            logger.error("Error getting template by name: {}", e.getMessage(), e);
            return null;
        }
    }

    public Page<EmailTemplate> getTemplates(Pageable pageable, String category, String search, Boolean active) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Criteria criteria = Criteria.where("tenantId").is(tenantId);

            if (category != null && !category.isEmpty()) {
                criteria.and("category").is(EmailTemplate.TemplateCategory.valueOf(category.toUpperCase()));
            }

            if (active != null) {
                criteria.and("active").is(active);
            }

            if (search != null && !search.trim().isEmpty()) {
                criteria.orOperator(
                        Criteria.where("templateName").regex(search, "i"),
                        Criteria.where("description").regex(search, "i"),
                        Criteria.where("subject").regex(search, "i")
                );
            }

            Query query = new Query(criteria)
                    .with(pageable)
                    .with(Sort.by(Sort.Direction.DESC, "dateModified"));

            List<EmailTemplate> templates = mongoTemplate.find(query, EmailTemplate.class);
            long total = mongoTemplate.count(new Query(criteria), EmailTemplate.class);

            return new PageImpl<>(templates, pageable, total);

        } catch (Exception e) {
            logger.error("Error getting templates: {}", e.getMessage(), e);
            return Page.empty();
        }
    }

    public boolean deleteTemplate(String templateId) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            EmailTemplate template = getTemplateById(templateId);
            if (template == null) {
                return false;
            }

            if (template.isSystem()) {
                throw new RuntimeException("Cannot delete system template");
            }

            Query query = new Query(Criteria.where("id").is(templateId)
                    .and("tenantId").is(tenantId));

            mongoTemplate.remove(query, EmailTemplate.class);

            // Remove from cache
            String cacheKey = tenantId + ":" + template.getTemplateName();
            templateCache.remove(cacheKey);

            logger.info("Deleted email template: {} for tenant: {}",
                    template.getTemplateName(), tenantId);

            return true;

        } catch (Exception e) {
            logger.error("Error deleting email template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete email template: " + e.getMessage(), e);
        }
    }

    // ===== TEMPLATE PROCESSING =====

    public String processTemplate(String templateName, Map<String, Object> variables) {
        String tenantId = TenantContext.getCurrentTenant();
        validateEmailConfigExists(tenantId);
        try {
            EmailTemplate template = getTemplateByName(templateName);
            if (template == null) {
                throw new RuntimeException("Template not found: " + templateName);
            }

            return processTemplate(template, variables);

        } catch (Exception e) {
            logger.error("Error processing template {}: {}", templateName, e.getMessage(), e);
            throw new RuntimeException("Failed to process template: " + e.getMessage(), e);
        }
    }

    public String processTemplate(EmailTemplate template, Map<String, Object> variables) {
        try {
            // Update usage statistics
            updateTemplateUsage(template.getId());

            // Merge with default values
            Map<String, Object> mergedVariables = new HashMap<>();
            if (template.getDefaultValues() != null) {
                mergedVariables.putAll(template.getDefaultValues());
            }
            if (variables != null) {
                mergedVariables.putAll(variables);
            }

            // Add tenant-specific variables
            addTenantVariables(mergedVariables);

            // Validate required variables
            validateRequiredVariables(template, mergedVariables);

            // Process the template content using simple variable replacement
            return processTemplateContent(template.getHtmlContent(), mergedVariables);

        } catch (Exception e) {
            logger.error("Error processing template content: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process template content: " + e.getMessage(), e);
        }
    }

    public String processSubject(String templateName, Map<String, Object> variables) {
        try {
            EmailTemplate template = getTemplateByName(templateName);
            if (template == null) {
                throw new RuntimeException("Template not found: " + templateName);
            }

            // Merge with default values
            Map<String, Object> mergedVariables = new HashMap<>();
            if (template.getDefaultValues() != null) {
                mergedVariables.putAll(template.getDefaultValues());
            }
            if (variables != null) {
                mergedVariables.putAll(variables);
            }

            addTenantVariables(mergedVariables);

            return processTemplateContent(template.getSubject(), mergedVariables);

        } catch (Exception e) {
            logger.error("Error processing template subject: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process template subject: " + e.getMessage(), e);
        }
    }

    // ===== TEMPLATE VALIDATION =====

    public List<String> validateTemplate(EmailTemplate template) {
        List<String> errors = new ArrayList<>();

        try {
            // Validate template syntax
            validateTemplateSyntax(template);

            // Check for required variables
            Map<String, EmailTemplate.TemplateVariable> variables = template.getVariables();
            if (variables != null) {
                for (EmailTemplate.TemplateVariable variable : variables.values()) {
                    if (variable.isRequired() &&
                            (template.getDefaultValues() == null ||
                                    !template.getDefaultValues().containsKey(variable.getName()))) {

                        // This is OK - required variables can be provided at runtime
                        // We're just checking the template structure
                    }
                }
            }

        } catch (Exception e) {
            errors.add("Template validation failed: " + e.getMessage());
        }

        return errors;
    }

    public EmailTemplate previewTemplate(String templateName, Map<String, Object> sampleData) {
        try {
            EmailTemplate template = getTemplateByName(templateName);
            if (template == null) {
                throw new RuntimeException("Template not found: " + templateName);
            }

            // Create a copy for preview
            EmailTemplate preview = new EmailTemplate();
            preview.setTemplateName(template.getTemplateName());
            preview.setDescription(template.getDescription());
            preview.setTemplateType(template.getTemplateType());
            preview.setCategory(template.getCategory());

            // Process with sample data
            preview.setSubject(processTemplateContent(template.getSubject(), sampleData));
            preview.setHtmlContent(processTemplateContent(template.getHtmlContent(), sampleData));

            if (template.getTextContent() != null) {
                preview.setTextContent(processTemplateContent(template.getTextContent(), sampleData));
            }

            return preview;

        } catch (Exception e) {
            logger.error("Error previewing template: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to preview template: " + e.getMessage(), e);
        }
    }

    // ===== HELPER METHODS =====

    private boolean templateExistsByName(String templateName) {
        String tenantId = TenantContext.getCurrentTenant();
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = new Query(Criteria.where("templateName").is(templateName)
                .and("tenantId").is(tenantId));

        return mongoTemplate.exists(query, EmailTemplate.class);
    }

    private Map<String, EmailTemplate.TemplateVariable> extractTemplateVariables(String content) {
        Map<String, EmailTemplate.TemplateVariable> variables = new HashMap<>();

        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            if (!variables.containsKey(variableName)) {
                EmailTemplate.TemplateVariable variable = new EmailTemplate.TemplateVariable()
                        .setName(variableName)
                        .setDisplayName(variableName)
                        .setType(EmailTemplate.VariableType.STRING)
                        .setRequired(false);
                variables.put(variableName, variable);
            }
        }

        return variables;
    }

    private void validateTemplateSyntax(EmailTemplate template) {
        try {
            // Basic validation - check for balanced braces
            String content = template.getHtmlContent();
            long openBraces = content.chars().filter(ch -> ch == '{').count();
            long closeBraces = content.chars().filter(ch -> ch == '}').count();

            if (openBraces != closeBraces) {
                throw new RuntimeException("Unbalanced template braces in content");
            }

            // Check subject as well
            if (template.getSubject() != null) {
                String subject = template.getSubject();
                openBraces = subject.chars().filter(ch -> ch == '{').count();
                closeBraces = subject.chars().filter(ch -> ch == '}').count();

                if (openBraces != closeBraces) {
                    throw new RuntimeException("Unbalanced template braces in subject");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid template syntax: " + e.getMessage());
        }
    }

    /**
     * Process template content using {{variable}} syntax
     */
    private String processTemplateContent(String content, Map<String, Object> variables) {
        if (content == null) {
            return "";
        }

        if (variables == null || variables.isEmpty()) {
            return content;
        }

        String result = content;

        // Replace {{variable}} patterns
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            String placeholder = matcher.group(0); // Full {{variableName}}

            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";

            result = result.replace(placeholder, replacement);
        }

        return result;
    }

    private void addTenantVariables(Map<String, Object> variables) {
        String tenantId = TenantContext.getCurrentTenant();
        variables.put("tenantId", tenantId);
        variables.put("currentDate", new Date());
        variables.put("currentYear", Calendar.getInstance().get(Calendar.YEAR));

        // Add other tenant-specific variables as needed
        // You could fetch tenant info and add logo, company name, etc.
    }

    private void validateRequiredVariables(EmailTemplate template, Map<String, Object> variables) {
        if (template.getVariables() == null) return;

        List<String> missingVariables = new ArrayList<>();
        for (EmailTemplate.TemplateVariable variable : template.getVariables().values()) {
            if (variable.isRequired() && !variables.containsKey(variable.getName())) {
                missingVariables.add(variable.getName());
            }
        }

        if (!missingVariables.isEmpty()) {
            throw new RuntimeException("Missing required variables: " + String.join(", ", missingVariables));
        }
    }

    private void updateTemplateUsage(String templateId) {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("id").is(templateId)
                    .and("tenantId").is(tenantId));

            Update update = new Update()
                    .inc("usageCount", 1)
                    .set("lastUsed", new Date());

            mongoTemplate.updateFirst(query, update, EmailTemplate.class);

        } catch (Exception e) {
            logger.warn("Failed to update template usage statistics: {}", e.getMessage());
        }
    }

    private void updateTemplateCache(EmailTemplate template) {
        String cacheKey = template.getTenantId() + ":" + template.getTemplateName();
        templateCache.put(cacheKey, template);
    }

    public void clearTemplateCache(String tenantId) {
        templateCache.entrySet().removeIf(entry -> entry.getKey().startsWith(tenantId + ":"));
    }

    public void clearAllTemplateCache() {
        templateCache.clear();
    }

    // ===== SYSTEM TEMPLATE MANAGEMENT =====

    public void createDefaultTemplates(String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);

            createSystemTemplate("password-reset", "Password Reset Request",
                    "Reset Your Password - {{tenantName}}",
                    getPasswordResetTemplate(),
                    EmailTemplate.TemplateCategory.AUTHENTICATION);

            createSystemTemplate("welcome-user", "Welcome New User",
                    "Welcome to {{tenantName}} - Account Created",
                    getWelcomeUserTemplate(),
                    EmailTemplate.TemplateCategory.AUTHENTICATION);

            createSystemTemplate("application-received", "Application Received",
                    "Application Received - {{jobTitle}}",
                    getApplicationReceivedTemplate(),
                    EmailTemplate.TemplateCategory.RECRUITMENT);

            createSystemTemplate("interview-invitation", "Interview Invitation",
                    "Interview Invitation - {{jobTitle}}",
                    getInterviewInvitationTemplate(),
                    EmailTemplate.TemplateCategory.RECRUITMENT);

            logger.info("Created default email templates for tenant: {}", tenantId);

        } catch (Exception e) {
            logger.error("Error creating default templates for tenant {}: {}", tenantId, e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }

    private void createSystemTemplate(String name, String description, String subject,
                                      String content, EmailTemplate.TemplateCategory category) {
        try {
            if (!templateExistsByName(name)) {
                EmailTemplate template = new EmailTemplate()
                        .setTemplateName(name)
                        .setDescription(description)
                        .setSubject(subject)
                        .setHtmlContent(content)
                        .setTemplateType(EmailTemplate.TemplateType.HTML)
                        .setCategory(category)
                        .setActive(true)
                        .setSystem(true);

                createTemplate(template);
            }
        } catch (Exception e) {
            logger.warn("Failed to create system template {}: {}", name, e.getMessage());
        }
    }

    // ===== DEFAULT TEMPLATE CONTENT =====

    private String getWelcomeUserTemplate() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <title>Welcome</title>\n"
                + "</head>\n"
                + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">\n"
                + "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">\n"
                + "        <div style=\"text-align: center; margin-bottom: 30px;\">\n"
                + "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">\n"
                + "        </div>\n"
                + "        <h2 style=\"color: #27ae60;\">Welcome to {{tenantName}}!</h2>\n"
                + "        <p>Hello {{userName}},</p>\n"
                + "        <p>We're excited to have you on board. Your account has been created successfully.</p>\n"
                + "        <div style=\"background-color: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;\">\n"
                + "            <p><strong>Role:</strong> {{userRole}}</p>\n"
                + "            <p><strong>Temporary Password:</strong> {{tempPassword}}</p>\n"
                + "            <p><strong>Login URL:</strong> <a href=\"{{loginUrl}}\">{{loginUrl}}</a></p>\n"
                + "        </div>\n"
                + "        <p>Please log in and change your password on first access.</p>\n"
                + "        <div style=\"text-align: center; margin: 30px 0;\">\n"
                + "            <a href=\"{{loginUrl}}\" style=\"background-color: #27ae60; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;\">Login Now</a>\n"
                + "        </div>\n"
                + "        <p>Let's get started!</p>\n"
                + "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">\n"
                + "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>\n"
                + "    </div>\n"
                + "</body>\n"
                + "</html>";
    }


    private String getApplicationReceivedTemplate() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <title>Application Received</title>\n"
                + "</head>\n"
                + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">\n"
                + "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">\n"
                + "        <div style=\"text-align: center; margin-bottom: 30px;\">\n"
                + "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">\n"
                + "        </div>\n"
                + "        <h2 style=\"color: #2980b9;\">Application Received</h2>\n"
                + "        <p>Dear {{candidateName}},</p>\n"
                + "        <p>Thank you for your interest in the <strong>{{jobTitle}}</strong> position at {{tenantName}}.</p>\n"
                + "        <p>We have successfully received your application (ID: {{applicationId}}) and our team will review it carefully.</p>\n"
                + "        <div style=\"background-color: #e8f4f8; padding: 20px; border-radius: 5px; margin: 20px 0;\">\n"
                + "            <h3 style=\"color: #2980b9; margin-top: 0;\">What's Next?</h3>\n"
                + "            <p>• Our hiring team will review your application</p>\n"
                + "            <p>• We'll contact you within 5-7 business days</p>\n"
                + "            <p>• Keep an eye on your email for updates</p>\n"
                + "        </div>\n"
                + "        <p>If you have any questions, please don't hesitate to contact us.</p>\n"
                + "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">\n"
                + "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>\n"
                + "    </div>\n"
                + "</body>\n"
                + "</html>";
    }

    private String getInterviewInvitationTemplate() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <title>Interview Invitation</title>\n"
                + "</head>\n"
                + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">\n"
                + "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">\n"
                + "        <div style=\"text-align: center; margin-bottom: 30px;\">\n"
                + "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">\n"
                + "        </div>\n"
                + "        <h2 style=\"color: #e74c3c;\">Interview Invitation</h2>\n"
                + "        <p>Dear {{candidateName}},</p>\n"
                + "        <p>Congratulations! We would like to invite you for an interview for the <strong>{{jobTitle}}</strong> position.</p>\n"
                + "        <div style=\"background-color: #fdf2e9; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #e74c3c;\">\n"
                + "            <h3 style=\"color: #e74c3c; margin-top: 0;\">Interview Details</h3>\n"
                + "            <p><strong>Date:</strong> {{interviewDate}}</p>\n"
                + "            <p><strong>Time:</strong> {{interviewTime}}</p>\n"
                + "            <p><strong>Duration:</strong> {{duration}}</p>\n"
                + "            <p><strong>Type:</strong> {{interviewType}}</p>\n"
                + "            <p><strong>Location:</strong> {{location}}</p>\n"
                + "            <p><strong>Interviewer:</strong> {{interviewerName}}</p>\n"
                + "        </div>\n"
                + "        <div style=\"text-align: center; margin: 30px 0;\">\n"
                + "            <a href=\"{{confirmationLink}}\" style=\"background-color: #27ae60; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin-right: 10px;\">Confirm Interview</a>\n"
                + "            <a href=\"{{rescheduleLink}}\" style=\"background-color: #f39c12; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;\">Reschedule</a>\n"
                + "        </div>\n"
                + "        <p><strong>Instructions:</strong></p>\n"
                + "        <p>{{instructions}}</p>\n"
                + "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">\n"
                + "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>\n"
                + "    </div>\n"
                + "</body>\n"
                + "</html>";
    }



    private String getPasswordResetTemplate() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "    <meta charset=\"UTF-8\">\n"
                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    <title>Password Reset</title>\n"
                + "</head>\n"
                + "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">\n"
                + "    <div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">\n"
                + "        <div style=\"text-align: center; margin-bottom: 30px;\">\n"
                + "            <img src=\"{{logoUrl}}\" alt=\"{{tenantName}}\" style=\"max-height: 60px;\">\n"
                + "        </div>\n"
                + "        <h2 style=\"color: #2c3e50;\">Password Reset Request</h2>\n"
                + "        <p>Hello {{userName}},</p>\n"
                + "        <p>We received a request to reset your password. Click the button below to reset it:</p>\n"
                + "        <div style=\"text-align: center; margin: 30px 0;\">\n"
                + "            <a href=\"{{resetLink}}\" style=\"background-color: #3498db; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;\">Reset Password</a>\n"
                + "        </div>\n"
                + "        <p>If you didn't request this password reset, please ignore this email.</p>\n"
                + "        <p>This link will expire in 24 hours for security reasons.</p>\n"
                + "        <hr style=\"margin: 30px 0; border: none; border-top: 1px solid #eee;\">\n"
                + "        <p style=\"font-size: 12px; color: #666;\">{{tenantName}} - {{currentYear}}</p>\n"
                + "    </div>\n"
                + "</body>\n"
                + "</html>";
    }



}