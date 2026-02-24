package com.midas.consulting.util;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.tenant.EmailTemplate;
import com.midas.consulting.service.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TemplateVerificationUtility {
    
    private static final Logger logger = LoggerFactory.getLogger(TemplateVerificationUtility.class);
    
    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;
    
    public void verifyTemplatesForTenant(String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            
            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            List<EmailTemplate> templates = mongoTemplate.find(query, EmailTemplate.class);
            
            logger.info("=== Template Verification for Tenant: {} ===", tenantId);
            logger.info("Total templates found: {}", templates.size());
            
            if (templates.isEmpty()) {
                logger.warn("NO TEMPLATES FOUND for tenant: {}", tenantId);
            } else {
                for (EmailTemplate template : templates) {
                    logger.info("Template: {} | System: {} | Active: {} | Created: {}", 
                               template.getTemplateName(), 
                               template.isSystem(), 
                               template.isActive(),
                               template.getDateCreated());
                }
            }
            
            // Check for specific system templates
            String[] expectedTemplates = {"password-reset", "welcome-user", "application-received", "interview-invitation"};
            for (String templateName : expectedTemplates) {
                Query templateQuery = new Query(Criteria.where("tenantId").is(tenantId)
                        .and("templateName").is(templateName));
                boolean exists = mongoTemplate.exists(templateQuery, EmailTemplate.class);
                logger.info("System template '{}': {}", templateName, exists ? "EXISTS" : "MISSING");
            }
            
        } catch (Exception e) {
            logger.error("Error verifying templates for tenant {}: {}", tenantId, e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }
    
    public boolean hasTemplates(String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            
            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            return mongoTemplate.exists(query, EmailTemplate.class);
            
        } catch (Exception e) {
            logger.error("Error checking templates for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        } finally {
            TenantContext.clear();
        }
    }
}