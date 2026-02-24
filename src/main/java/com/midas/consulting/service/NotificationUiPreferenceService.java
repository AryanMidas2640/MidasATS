package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.user.NotificationUiPreference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificationUiPreferenceService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationUiPreferenceService.class);

    private final MongoTemplateProvider mongoTemplateProvider;

    @Autowired
    public NotificationUiPreferenceService(MongoTemplateProvider mongoTemplateProvider) {
        this.mongoTemplateProvider = mongoTemplateProvider;
    }

    public List<NotificationUiPreference> getAllPreferences() {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        return mongoTemplate.findAll(NotificationUiPreference.class);
    }

    public Optional<NotificationUiPreference> getById(String id) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        return Optional.ofNullable(mongoTemplate.findById(id, NotificationUiPreference.class));
    }

    public Optional<NotificationUiPreference> getByUserId(String userId) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = new Query(Criteria.where("userId").is(userId));
        return Optional.ofNullable(mongoTemplate.findOne(query, NotificationUiPreference.class));
    }

    public NotificationUiPreference save(NotificationUiPreference preference) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        return mongoTemplate.save(preference);
    }

    public void delete(String id) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, NotificationUiPreference.class);
    }
}
