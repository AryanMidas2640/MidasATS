package com.midas.consulting.config.database;

import com.mongodb.MongoException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.function.Supplier;

public class RetryableMongoTemplate {

    private static final Logger logger = LoggerFactory.getLogger(RetryableMongoTemplate.class);
    
    private final MongoTemplate mongoTemplate;
    private final int maxRetries;
    private final int retryDelayMs;
    
    public RetryableMongoTemplate(MongoTemplate mongoTemplate, int maxRetries, int retryDelayMs) {
        this.mongoTemplate = mongoTemplate;
        this.maxRetries = maxRetries;
        this.retryDelayMs = retryDelayMs;
    }
    
    public <T> T findById(String id, Class<T> entityClass) {
        return executeWithRetry(() -> mongoTemplate.findById(id, entityClass));
    }
    
    public <T> List<T> find(Query query, Class<T> entityClass) {
        return executeWithRetry(() -> mongoTemplate.find(query, entityClass));
    }
    
    public <T> T findOne(Query query, Class<T> entityClass) {
        return executeWithRetry(() -> mongoTemplate.findOne(query, entityClass));
    }
    
    public <T> T save(T entity) {
        return executeWithRetry(() -> mongoTemplate.save(entity));
    }
    
    public <T> void remove(T entity) {
        executeWithRetry(() -> {
            mongoTemplate.remove(entity);
            return null;
        });
    }
    
    private <T> T executeWithRetry(Supplier<T> operation) {
        int attempts = 0;
        while (true) {
            try {
                attempts++;
                return operation.get();
            } catch (MongoException e) {
                if (isRetryable(e) && attempts <= maxRetries) {
                    logger.warn("MongoDB operation failed (attempt {}/{}), retrying in {}ms: {}", 
                            attempts, maxRetries, retryDelayMs, e.getMessage());
                    try {
                        Thread.sleep(retryDelayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Thread interrupted during retry delay", ie);
                    }
                } else {
                    logger.error("MongoDB operation failed after {} attempts: {}", 
                            attempts, e.getMessage());
                    throw e;
                }
            }
        }
    }
    
    private boolean isRetryable(MongoException e) {
        // Only retry on connection/timeout issues, not on data validation errors
        return e instanceof MongoSocketException 
                || e instanceof MongoTimeoutException
                || e.getCode() == 6  // HostUnreachable
                || e.getCode() == 7  // HostNotFound
                || e.getCode() == 89 // NetworkTimeout
                || e.getCode() == 91 // ShutdownInProgress
                || e.getCode() == 189 // PrimarySteppedDown
                || e.getCode() == 10107 // NotPrimary
                || e.getMessage().contains("Connection refused");
    }
}