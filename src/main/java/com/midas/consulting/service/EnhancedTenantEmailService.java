package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.EmailAttachment;
import com.midas.consulting.controller.v1.request.EmailRequest;
import com.midas.consulting.model.EmailAudit;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.model.EmailTemplateMapping;
import com.midas.consulting.model.tenant.EmailTemplate;
import com.midas.consulting.service.storage.EmailSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class EnhancedTenantEmailService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedTenantEmailService.class);

    @Autowired
    private EnhancedEmailConfigService emailConfigService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    // Cache for mail senders per config
    private final Map<String, JavaMailSender> mailSenderCache = new ConcurrentHashMap<>();

    // Pattern for {{variable}} replacement
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([^}]+)\\s*\\}\\}");

    /**
     * Send email with template-based configuration selection
     */
    public void sendEmail(EmailRequest emailRequest) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available for email sending");
        }

        EmailConfig emailConfig = null;
        String configSource = "unknown";

        try {
            // Determine which email configuration to use
            if (emailRequest.getTemplateName() != null) {
                // Get config based on template
                emailConfig = emailConfigService.getEmailConfigForTemplate(tenantId, emailRequest.getTemplateName());
                configSource = "template: " + emailRequest.getTemplateName();
            } else if (emailRequest.getConfigName() != null) {
                // Get config by name
                emailConfig = emailConfigService.getEmailConfigByName(tenantId, emailRequest.getConfigName());
                configSource = "config name: " + emailRequest.getConfigName();
            } else {
                // Get default config
                emailConfig = emailConfigService.getDefaultEmailConfig(tenantId);
                configSource = "default config";
            }

            if (emailConfig == null) {
                throw new RuntimeException("No email configuration found for tenant: " + tenantId + " (source: " + configSource + ")");
            }

            // Get mail sender for this config
            JavaMailSender mailSender = getMailSender(emailConfig);

            // Create message
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Apply configuration settings
            applyEmailConfiguration(helper, emailConfig, emailRequest);

            // Set basic email properties
            helper.setTo(emailRequest.getTo());

            // Process subject and content
            String processedSubject = processEmailSubject(emailRequest, emailConfig);
            String processedContent = processEmailContent(emailRequest, emailConfig);

            helper.setSubject(processedSubject);
            helper.setText(processedContent, emailRequest.isHtml());

            // Add attachments if any
            if (emailRequest.getAttachments() != null) {
                for (EmailAttachment attachment : emailRequest.getAttachments()) {
                    helper.addAttachment(attachment.getFilename(), attachment.getResource());
                }
            }

            // Send email
            mailSender.send(message);

            // Audit success
            auditEmail(tenantId, emailRequest, "SENT", null, emailConfig.getId());

            logger.info("Email sent successfully to {} for tenant {} using config: {}",
                    emailRequest.getTo(), tenantId, configSource);

        } catch (Exception e) {
            logger.error("Failed to send email for tenant: {} using config: {}", tenantId, configSource, e);
            auditEmail(tenantId, emailRequest, "FAILED", e.getMessage(),
                    emailConfig != null ? emailConfig.getId() : null);
            throw new EmailSendException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Process email subject with template variables
     */
    private String processEmailSubject(EmailRequest emailRequest, EmailConfig emailConfig) {
        if (emailRequest.getTemplateName() != null) {
            try {
                EmailTemplate template = emailTemplateService.getTemplateByName(emailRequest.getTemplateName());
                if (template != null && template.getSubject() != null) {
                    Map<String, Object> variables = prepareTemplateVariables(emailRequest.getTemplateVariables(), emailConfig);
                    return processTemplateVariables(template.getSubject(), variables);
                }
            } catch (Exception e) {
                logger.warn("Failed to process template subject for {}: {}", emailRequest.getTemplateName(), e.getMessage());
            }
        }

        // Fallback to provided subject
        return emailRequest.getSubject();
    }

    /**
     * Process email content with templates
     */
    private String processEmailContent(EmailRequest emailRequest, EmailConfig emailConfig) {
        if (emailRequest.getTemplateName() != null) {
            try {
                EmailTemplate template = emailTemplateService.getTemplateByName(emailRequest.getTemplateName());
                if (template != null) {
                    Map<String, Object> variables = prepareTemplateVariables(emailRequest.getTemplateVariables(), emailConfig);
                    return processTemplateVariables(template.getHtmlContent(), variables);
                }
            } catch (Exception e) {
                logger.warn("Failed to process template content for {}: {}", emailRequest.getTemplateName(), e.getMessage());
                return createFallbackContent(emailRequest.getSubject(), emailRequest.getTemplateVariables());
            }
        }

        // Use provided content if no template
        return emailRequest.getContent() != null ? emailRequest.getContent() : "";
    }

    /**
     * Prepare template variables with defaults and tenant-specific values
     */
    private Map<String, Object> prepareTemplateVariables(Map<String, Object> requestVariables, EmailConfig emailConfig) {
        Map<String, Object> variables = new HashMap<>();

        // Add tenant-specific defaults
//        variables.put("tenantName", emailConfig.getTenantId());
        variables.put("companyName", emailConfig.getCompanyName() != null ? emailConfig.getCompanyName() : "");
        variables.put("currentYear", String.valueOf(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)));
        variables.put("logoUrl", emailConfig.getCompanyLogo() != null ? emailConfig.getCompanyLogo() : "");
        variables.put("fromEmail", emailConfig.getDefaultFromEmail());
        variables.put("fromName", emailConfig.getDefaultFromName());

        // Add request variables (these override defaults)
        if (requestVariables != null) {
            variables.putAll(requestVariables);
        }

        return variables;
    }

    /**
     * Process template variables using {{variable}} syntax
     */
    private String processTemplateVariables(String content, Map<String, Object> variables) {
        if (content == null || variables == null) {
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

    /**
     * Apply email configuration to message helper
     */
    private void applyEmailConfiguration(MimeMessageHelper helper, EmailConfig config, EmailRequest request) throws Exception {
        // Get the MimeMessage from helper
        MimeMessage message = helper.getMimeMessage();

        // Check for template-specific overrides
        EmailTemplateMapping.TemplateOverrides overrides = null;
        if (request.getTemplateName() != null) {
            EmailTemplateMapping mapping = emailConfigService.getTemplateMapping(
                    TenantContext.getCurrentTenant(), request.getTemplateName());
            if (mapping != null && mapping.getOverrides() != null) {
                overrides = mapping.getOverrides();
            }
        }

        // Set from address
        String fromEmail = overrides != null && overrides.getFromEmail() != null ?
                overrides.getFromEmail() : config.getDefaultFromEmail();
        String fromName = overrides != null && overrides.getFromName() != null ?
                overrides.getFromName() : config.getDefaultFromName();

        if (fromName != null && !fromName.isEmpty()) {
            helper.setFrom(fromEmail, fromName);
        } else {
            helper.setFrom(fromEmail);
        }

        // Set reply-to
        String replyTo = overrides != null && overrides.getReplyTo() != null ?
                overrides.getReplyTo() : config.getDefaultReplyTo();
        if (replyTo != null && !replyTo.isEmpty()) {
            helper.setReplyTo(replyTo);
        }

        // Add custom headers
        if (config.getCustomHeaders() != null) {
            for (Map.Entry<String, String> header : config.getCustomHeaders().entrySet()) {
                message.addHeader(header.getKey(), header.getValue());
            }
        }

        // Add override headers
        if (overrides != null && overrides.getCustomHeaders() != null) {
            for (Map.Entry<String, String> header : overrides.getCustomHeaders().entrySet()) {
                message.addHeader(header.getKey(), header.getValue());
            }
        }
    }

    /**
     * Get or create mail sender for configuration
     */
    private JavaMailSender getMailSender(EmailConfig config) {
        String cacheKey = config.getId();
        return mailSenderCache.computeIfAbsent(cacheKey, k -> createMailSender(config));
    }

    /**
     * Create mail sender from configuration
     */
    private JavaMailSender createMailSender(EmailConfig config) {
        try {
            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

            switch (config.getProvider()) {
                case SMTP:
                    configureSMTPSender(mailSender, config.getSmtpConfig());
                    break;
                case SENDGRID:
                    configureSendGridSender(mailSender, config.getSendGridConfig());
                    break;
                case AWS_SES:
                    configureAwsSesSender(mailSender, config.getAwsSesConfig());
                    break;
                case OUTLOOK:
                    configureOutlookSender(mailSender, config.getOutlookConfig());
                    break;
                case GMAIL:
                    configureGmailSender(mailSender, config.getGmailConfig());
                    break;
                default:
                    throw new UnsupportedOperationException("Email provider not supported: " + config.getProvider());
            }

            return mailSender;

        } catch (Exception e) {
            logger.error("Failed to create mail sender for config: {}", config.getId(), e);
            throw new RuntimeException("Failed to create mail sender", e);
        }
    }

    /**
     * Configure SMTP sender
     */
    private void configureSMTPSender(JavaMailSenderImpl mailSender, EmailConfig.SmtpConfig smtpConfig) {
        mailSender.setHost(smtpConfig.getHost());
        mailSender.setPort(smtpConfig.getPort());
        mailSender.setUsername(smtpConfig.getUsername());
        mailSender.setPassword(smtpConfig.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", smtpConfig.isEnableTls());
        props.put("mail.smtp.ssl.enable", smtpConfig.isEnableSsl());
        props.put("mail.debug", "false");
        props.put("mail.smtp.connectiontimeout", smtpConfig.getConnectionTimeout());
        props.put("mail.smtp.timeout", smtpConfig.getSocketTimeout());

        // Add any additional properties
        if (smtpConfig.getAdditionalProperties() != null) {
            smtpConfig.getAdditionalProperties().forEach(props::put);
        }
    }

    /**
     * Configure SendGrid sender (using SMTP relay)
     */
    private void configureSendGridSender(JavaMailSenderImpl mailSender, EmailConfig.SendGridConfig sendGridConfig) {
        mailSender.setHost("smtp.sendgrid.net");
        mailSender.setPort(587);
        mailSender.setUsername("apikey");
        mailSender.setPassword(sendGridConfig.getApiKey());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false");
    }

    /**
     * Configure AWS SES sender
     */
    private void configureAwsSesSender(JavaMailSenderImpl mailSender, EmailConfig.AwsSesConfig awsSesConfig) {
        // AWS SES SMTP settings
        String region = awsSesConfig.getRegion();
        mailSender.setHost("email-smtp." + region + ".amazonaws.com");
        mailSender.setPort(587);
        mailSender.setUsername(awsSesConfig.getAccessKey());
        mailSender.setPassword(awsSesConfig.getSecretKey());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
    }

    /**
     * Configure Outlook/Office365 sender
     */
    private void configureOutlookSender(JavaMailSenderImpl mailSender, EmailConfig.OutlookConfig outlookConfig) {
        mailSender.setHost("smtp.office365.com");
        mailSender.setPort(587);
        mailSender.setUsername(outlookConfig.getFromEmail());
        // Use OAuth token if available, otherwise use app password
        mailSender.setPassword(outlookConfig.getAccessToken() != null ?
                outlookConfig.getAccessToken() : outlookConfig.getClientSecret());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }

    /**
     * Configure Gmail sender
     */
    private void configureGmailSender(JavaMailSenderImpl mailSender, EmailConfig.GmailConfig gmailConfig) {
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(gmailConfig.getFromEmail());
        // Use OAuth token if available, otherwise use app password
        mailSender.setPassword(gmailConfig.getAccessToken() != null ?
                gmailConfig.getAccessToken() : gmailConfig.getClientSecret());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
    }

    /**
     * Send email with specific configuration
     */
    public void sendEmailWithConfig(String configName, EmailRequest emailRequest) {
        emailRequest.setConfigName(configName);
        sendEmail(emailRequest);
    }

    /**
     * Send bulk emails with configuration selection
     */
    public void sendBulkEmail(List<EmailRequest> emailRequests, String configName) {
        String tenantId = TenantContext.getCurrentTenant();

        for (EmailRequest request : emailRequests) {
            try {
                if (configName != null) {
                    request.setConfigName(configName);
                }
                sendEmail(request);
            } catch (Exception e) {
                logger.error("Failed to send email to {} for tenant {}: {}",
                        request.getTo(), tenantId, e.getMessage());
            }
        }
    }

    /**
     * Test email configuration
     */
    public boolean testEmailConfiguration1(String configId) {
        try {
            EmailConfig config = emailConfigService.getEmailConfigById(configId);
            if (config == null) {
                return false;
            }

            JavaMailSender mailSender = getMailSender(config);
            // Create a test connection
            mailSender.createMimeMessage();

            logger.info("Email configuration test successful for config: {}", configId);
            return true;
        } catch (Exception e) {
            logger.error("Email configuration test failed for config {}: {}", configId, e.getMessage());
            return false;
        }
    }

    public boolean testEmailConfiguration(String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            EmailConfig config = emailConfigService.getDefaultEmailConfig(tenantId);
            if (config == null) {
                return false;
            }
            return testEmailConfiguration(config.getId());
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Clear mail sender cache for specific config
     */
    public void clearMailSenderCache(String configId) {
        mailSenderCache.remove(configId);
        logger.info("Cleared mail sender cache for config: {}", configId);
    }

    /**
     * Clear all mail sender caches
     */
    public void clearAllMailSenderCache() {
        mailSenderCache.clear();
        logger.info("Cleared all mail sender caches");
    }

    /**
     * Send simple email without template
     */
    public boolean sendSimpleEmail(String tenantId, String to, String subject, String content) {
        try {
            TenantContext.setCurrentTenant(tenantId);

            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(to);
            emailRequest.setSubject(subject);
            emailRequest.setContent(content);
            emailRequest.setHtml(true);

            sendEmail(emailRequest);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send simple email to {} for tenant {}: {}", to, tenantId, e.getMessage(), e);
            return false;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Send templated email
     */
    public boolean sendTemplatedEmail(String tenantId, String to, String subject, String templateName, Map<String, Object> templateVariables) {
        try {
            TenantContext.setCurrentTenant(tenantId);

            EmailRequest emailRequest = new EmailRequest();
            emailRequest.setTo(to);
            emailRequest.setSubject(subject);
            emailRequest.setTemplateName(templateName);
            emailRequest.setTemplateVariables(templateVariables);
            emailRequest.setHtml(true);
            sendEmail(emailRequest);
            return true;

        } catch (Exception e) {
            logger.error("Failed to send templated email to {} using template {} for tenant {}: {}",
                    to, templateName, tenantId, e.getMessage(), e);
            return false;
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Create fallback content when template processing fails
     */
    private String createFallbackContent(String subject, Map<String, Object> variables) {
        StringBuilder content = new StringBuilder();
        content.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head>");
        content.append("<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333;\">");
        content.append("<div style=\"max-width: 600px; margin: 0 auto; padding: 20px;\">");
        content.append("<h2>").append(subject != null ? subject : "Notification").append("</h2>");

        if (variables != null && !variables.isEmpty()) {
            content.append("<div style=\"background-color: #f8f9fa; padding: 15px; border-radius: 5px;\">");
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                content.append("<p><strong>").append(entry.getKey()).append(":</strong> ");
                content.append(entry.getValue() != null ? entry.getValue().toString() : "").append("</p>");
            }
            content.append("</div>");
        }

        content.append("</div></body></html>");
        return content.toString();
    }

    /**
     * Audit email
     */
    private void auditEmail(String tenantId, EmailRequest request, String status,
                            String errorMessage, String emailConfigId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            EmailAudit audit = EmailAudit.builder()
                    .tenantId(tenantId)
                    .recipient(request.getTo())
                    .subject(request.getSubject())
                    .templateName(request.getTemplateName())
                    .status(status)
                    .errorMessage(errorMessage)
                    .sentAt(Instant.now())
                    .build();

            // Add config information
            audit.setEmailConfigId(emailConfigId);
            audit.setConfigName(request.getConfigName());

            mongoTemplate.save(audit);
        } catch (Exception e) {
            logger.error("Failed to audit email: {}", e.getMessage());
        }
    }
}