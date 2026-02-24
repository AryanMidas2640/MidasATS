package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.candidate.Activity;
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
public class ActivityService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityService.class);

    private final MongoTemplateProvider mongoTemplateProvider;

    @Autowired
    public ActivityService(MongoTemplateProvider mongoTemplateProvider) {
        this.mongoTemplateProvider = mongoTemplateProvider;
    }

    public List<Activity> getAllActivities() {
        logger.debug("Getting all activities for current tenant");
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        return mongoTemplate.findAll(Activity.class);
    }

    public Optional<Activity> getActivityById(String id) {
        logger.debug("Getting activity by id: {} for current tenant", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Activity activity = mongoTemplate.findById(id, Activity.class);
        return Optional.ofNullable(activity);
    }

    public Activity createOrUpdateActivity(Activity activity) {
        logger.debug("Creating/updating activity for current tenant");
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        return mongoTemplate.save(activity);
    }

    public void deleteActivity(String id) {
        logger.debug("Deleting activity with id: {} for current tenant", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, Activity.class);
    }

    public List<Activity> getActivityBySourceID(String sourceID) {
        logger.debug("Getting activities by source ID: {} for current tenant", sourceID);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = new Query(Criteria.where("sourceID").is(sourceID));
        return mongoTemplate.find(query, Activity.class);
    }

    public List<Activity> getActivityByProviderJobID(Integer providerJobID) {
        logger.debug("Getting activities by provider job ID: {} for current tenant", providerJobID);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = new Query(Criteria.where("providerJobID").is(providerJobID));
        return mongoTemplate.find(query, Activity.class);
    }

    public List<Activity> getActivityByCandidateId(String candidateId) {
        logger.debug("Getting activities by candidate ID: {} for current tenant", candidateId);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = new Query(Criteria.where("candidateId").is(candidateId));
        return mongoTemplate.find(query, Activity.class);
    }
}