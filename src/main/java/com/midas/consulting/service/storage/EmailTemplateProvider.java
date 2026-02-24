package com.midas.consulting.service.storage;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.service.TenantContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailTemplateProvider {
    
    private final Map<String, JavaMailSender> mailSenderCache = new ConcurrentHashMap<>();
    private final MongoTemplateProvider mongoTemplateProvider;
    private final TemplateEngine templateEngine;
    
    public EmailTemplateProvider(MongoTemplateProvider mongoTemplateProvider, 
                               TemplateEngine templateEngine) {
        this.mongoTemplateProvider = mongoTemplateProvider;
        this.templateEngine = templateEngine;
    }
    
    public JavaMailSender getMailSender() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new IllegalStateException("No tenant context available");
        }
        
        return mailSenderCache.computeIfAbsent(tenantId, this::createMailSender);
    }
    
    private JavaMailSender createMailSender(String tenantId) {
        // Use the main mongo template to get tenant info (tenant collection is in main DB)
        MongoTemplate mainTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Query query = new Query(Criteria.where("id").is(tenantId));
        EmailConfig tenant = mainTemplate.findOne(query, EmailConfig.class);
        
        if (tenant == null) {
            throw new RuntimeException("Tenant not found: " + tenantId);
        }
        
        EmailConfig config = tenant;
        if (config == null) {
            throw new RuntimeException("Email configuration not found for tenant: " + tenantId);
        }
        
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(config.getSmtpConfig().getHost());
        mailSender.setPort(config.getSmtpConfig().getPort());
        mailSender.setUsername(config.getSmtpConfig().getUsername());
        mailSender.setPassword(config.getSmtpConfig().getPassword());
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", config.getSmtpConfig().isEnableTls());
        props.put("mail.smtp.ssl.enable", config.getSmtpConfig().isEnableSsl());
        props.put("mail.debug", "false");
        
        // Add custom properties if any
//        if (config.getSmtpConfig().getAdditionalProperties() != null) {
//            config.getCustomProperties().forEach(props::put);
//        }
        
        return mailSender;
    }
    
    public String processTemplate(String templateName, Map<String, Object> variables) {
        String tenantId = TenantContext.getCurrentTenant();
        
        // Get tenant info using MongoTemplate
        MongoTemplate mainTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = new Query(Criteria.where("id").is(tenantId));
        EmailConfig emailConfig = mainTemplate.findOne(query, EmailConfig.class);
        
        if (emailConfig == null) {
            throw new RuntimeException("Tenant not found: " + tenantId);
        }
        
        // Add tenant-specific branding variables

        if (emailConfig.getSmtpConfig() != null) {
            variables.put("configName", emailConfig.getConfigName());
            variables.put("logoUrl", emailConfig.getCompanyLogo());
            variables.put("brandColor", emailConfig.getFooterText());
            variables.put("fromName", emailConfig.getDefaultFromName());
        }
        
        Context context = new Context();
        context.setVariables(variables);
        
        return templateEngine.process(templateName, context);
    }
    
    public void evictCache(String tenantId) {
        mailSenderCache.remove(tenantId);
    }
    
    public void clearCache() {
        mailSenderCache.clear();
    }
}