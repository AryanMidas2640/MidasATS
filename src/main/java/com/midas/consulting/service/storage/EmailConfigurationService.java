package com.midas.consulting.service.storage;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.EmailConfig;
import com.midas.consulting.model.Tenant;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 4. Email Configuration Service using MongoTemplate
@Service
@Transactional
public class EmailConfigurationService {
    
    private final MongoTemplateProvider mongoTemplateProvider;
    private final EmailTemplateProvider emailTemplateProvider;
    
    public EmailConfigurationService(MongoTemplateProvider mongoTemplateProvider,
                                     EmailTemplateProvider emailTemplateProvider) {
        this.mongoTemplateProvider = mongoTemplateProvider;
        this.emailTemplateProvider = emailTemplateProvider;
    }
    
    public void updateEmailConfiguration(String tenantId, EmailConfig config) {
        MongoTemplate mainTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Query query = new Query(Criteria.where("id").is(tenantId));
        Update update = new Update().set("emailConfig", config);
        
        UpdateResult result = mainTemplate.updateFirst(query, update, Tenant.class);
        
        if (result.getMatchedCount() == 0) {
            throw new RuntimeException("Tenant not found: " + tenantId);
        }
        
        // Evict cache to force recreation with new config
        emailTemplateProvider.evictCache(tenantId);
    }
    
    public EmailConfig getEmailConfiguration(String tenantId) {
        MongoTemplate mainTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Query query = new Query(Criteria.where("tenantId").is(tenantId));
//        query.fields().include("emailConfig");
        
        EmailConfig tenant = mainTemplate.findOne(query, EmailConfig.class);
        
        return tenant != null ? tenant : null;
    }
    
    public boolean hasEmailConfiguration(String tenantId) {
        MongoTemplate mainTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Query query = new Query(Criteria.where("id").is(tenantId)
            .and("emailConfig").exists(true));
        
        return mainTemplate.exists(query, Tenant.class);
    }
}
