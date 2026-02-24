package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.EmailRequest;
import com.midas.consulting.model.EmailAudit;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.service.storage.TenantConfigService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Component
public class EmailMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailMonitoringService.class);
    
    @Autowired
    private EnhancedTenantEmailService tenantEmailService;
    
    @Autowired
    private TenantConfigService tenantConfigService;
    
    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;
    
    // Circuit breaker pattern for email sending
    private final Map<String, CircuitBreaker> tenantCircuitBreakers = new ConcurrentHashMap<>();
    
    // Email health metrics
    private final Map<String, EmailHealthMetrics> healthMetrics = new ConcurrentHashMap<>();
    
    // ===== HEALTH CHECK METHODS =====
    
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void performHealthChecks() {
        List<String> activeTenants = getActiveTenants();
        
        for (String tenantId : activeTenants) {
            try {
                performTenantHealthCheck(tenantId);
            } catch (Exception e) {
                logger.error("Health check failed for tenant {}: {}", tenantId, e.getMessage());
            }
        }
    }
    
    private void performTenantHealthCheck(String tenantId) {
        EmailHealthMetrics metrics = healthMetrics.computeIfAbsent(tenantId, 
            k -> new EmailHealthMetrics(tenantId));
        
        try {
            // Test email configuration
            boolean configValid = tenantEmailService.testEmailConfiguration(tenantId);
            
            // Get recent email statistics
            updateEmailStatistics(tenantId, metrics);
            
            // Update health status
            metrics.setLastHealthCheck(Instant.now());
            metrics.setConfigurationValid(configValid);
            metrics.setOverallHealth(calculateOverallHealth(metrics));
            
            // Alert if health is poor
            if (metrics.getOverallHealth() == HealthStatus.CRITICAL) {
                sendHealthAlert(tenantId, metrics);
            }
            
        } catch (Exception e) {
            metrics.setLastError(e.getMessage());
            metrics.setOverallHealth(HealthStatus.CRITICAL);
            logger.error("Health check failed for tenant {}: {}", tenantId, e.getMessage());
        }
    }
    
    private void updateEmailStatistics(String tenantId, EmailHealthMetrics metrics) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            
            // Get email counts for the last hour
            Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
            
            long recentTotal = getEmailCountSince(tenantId, oneHourAgo, null);
            long recentSent = getEmailCountSince(tenantId, oneHourAgo, "SENT");
            long recentFailed = getEmailCountSince(tenantId, oneHourAgo, "FAILED");
            
            metrics.setRecentEmailCount(recentTotal);
            metrics.setRecentSuccessCount(recentSent);
            metrics.setRecentFailureCount(recentFailed);
            
            // Calculate success rate
            if (recentTotal > 0) {
                metrics.setRecentSuccessRate((double) recentSent / recentTotal * 100);
            }
            
        } finally {
            TenantContext.clear();
        }
    }
    
    private long getEmailCountSince(String tenantId, Instant since, String status) {
        MongoTemplate template = mongoTemplateProvider.getMongoTemplate();
        
        Criteria criteria = Criteria.where("tenantId").is(tenantId)
                .and("sentAt").gte(since);
        
        if (status != null) {
            criteria.and("status").is(status);
        }
        
        Query query = new Query(criteria);
        return template.count(query, EmailAudit.class);
    }
    
    private HealthStatus calculateOverallHealth(EmailHealthMetrics metrics) {
        if (!metrics.isConfigurationValid()) {
            return HealthStatus.CRITICAL;
        }
        
        if (metrics.getRecentSuccessRate() < 50) {
            return HealthStatus.CRITICAL;
        } else if (metrics.getRecentSuccessRate() < 80) {
            return HealthStatus.WARNING;
        } else {
            return HealthStatus.HEALTHY;
        }
    }
    
    // ===== CIRCUIT BREAKER IMPLEMENTATION =====
    
    public boolean sendEmailWithCircuitBreaker(String tenantId, EmailRequest emailRequest) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(tenantId);
        
        if (circuitBreaker.isOpen()) {
            logger.warn("Circuit breaker is OPEN for tenant {}. Email sending blocked.", tenantId);
            throw new EmailCircuitBreakerException("Email service temporarily unavailable for tenant: " + tenantId);
        }
        
        try {
            tenantEmailService.sendEmail(emailRequest);
            circuitBreaker.recordSuccess();
            return true;
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }
    
    private CircuitBreaker getCircuitBreaker(String tenantId) {
        return tenantCircuitBreakers.computeIfAbsent(tenantId, 
            k -> new CircuitBreaker(tenantId, 5, 60000)); // 5 failures, 1 minute timeout
    }
    
    // ===== MONITORING ENDPOINTS =====
    
    public Map<String, Object> getSystemHealthStatus() {
        Map<String, Object> systemHealth = new HashMap<>();
        
        List<EmailHealthMetrics> allMetrics = new ArrayList<>(healthMetrics.values());
        
        long healthyTenants = allMetrics.stream()
                .mapToLong(m -> m.getOverallHealth() == HealthStatus.HEALTHY ? 1 : 0)
                .sum();
        
        long warningTenants = allMetrics.stream()
                .mapToLong(m -> m.getOverallHealth() == HealthStatus.WARNING ? 1 : 0)
                .sum();
        
        long criticalTenants = allMetrics.stream()
                .mapToLong(m -> m.getOverallHealth() == HealthStatus.CRITICAL ? 1 : 0)
                .sum();
        
        systemHealth.put("totalTenants", allMetrics.size());
        systemHealth.put("healthyTenants", healthyTenants);
        systemHealth.put("warningTenants", warningTenants);
        systemHealth.put("criticalTenants", criticalTenants);
        systemHealth.put("lastUpdated", Instant.now());
        
        // Overall system status
        if (criticalTenants > allMetrics.size() / 2) {
            systemHealth.put("systemStatus", "CRITICAL");
        } else if (warningTenants + criticalTenants > allMetrics.size() / 3) {
            systemHealth.put("systemStatus", "WARNING");
        } else {
            systemHealth.put("systemStatus", "HEALTHY");
        }
        
        return systemHealth;
    }
    
    public EmailHealthMetrics getTenantHealthMetrics(String tenantId) {
        return healthMetrics.get(tenantId);
    }
    
    public List<EmailHealthMetrics> getAllTenantMetrics() {
        return new ArrayList<>(healthMetrics.values());
    }
    
    // ===== ALERT METHODS =====
    
    private void sendHealthAlert(String tenantId, EmailHealthMetrics metrics) {
        try {
            // Send alert to system administrators
            String alertSubject = String.format("ALERT: Email Health Critical for Tenant %s", tenantId);
            String alertContent = buildHealthAlertContent(tenantId, metrics);
            
            // You could send this to a system admin email or integrate with external alerting
            logger.error("HEALTH ALERT for tenant {}: {}", tenantId, alertContent);
            
        } catch (Exception e) {
            logger.error("Failed to send health alert for tenant {}: {}", tenantId, e.getMessage());
        }
    }
    
    private String buildHealthAlertContent(String tenantId, EmailHealthMetrics metrics) {
        StringBuilder content = new StringBuilder();
        content.append("Email Health Alert for Tenant: ").append(tenantId).append("\n\n");
        content.append("Health Status: ").append(metrics.getOverallHealth()).append("\n");
        content.append("Configuration Valid: ").append(metrics.isConfigurationValid()).append("\n");
        content.append("Recent Success Rate: ").append(String.format("%.2f%%", metrics.getRecentSuccessRate())).append("\n");
        content.append("Recent Emails: ").append(metrics.getRecentEmailCount()).append("\n");
        content.append("Recent Failures: ").append(metrics.getRecentFailureCount()).append("\n");
        
        if (metrics.getLastError() != null) {
            content.append("Last Error: ").append(metrics.getLastError()).append("\n");
        }
        
        content.append("Last Health Check: ").append(metrics.getLastHealthCheck()).append("\n");
        
        return content.toString();
    }
    
    // ===== UTILITY METHODS =====
    
    private List<String> getActiveTenants() {
        MongoTemplate mainTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Query query = new Query();
        query.fields().include("id");
        
        List<Tenant> tenants = mainTemplate.find(query, Tenant.class);
        return tenants.stream().map(Tenant::getId).collect(Collectors.toList());
    }
    
    // ===== INNER CLASSES =====
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EmailHealthMetrics {
        private String tenantId;
        private HealthStatus overallHealth = HealthStatus.UNKNOWN;
        private boolean configurationValid = false;
        private Instant lastHealthCheck;
        private String lastError;
        
        // Email statistics
        private long recentEmailCount = 0;
        private long recentSuccessCount = 0;
        private long recentFailureCount = 0;
        private double recentSuccessRate = 0.0;
        
        // Historical data
        private long totalEmailsToday = 0;
        private long totalEmailsThisWeek = 0;
        private double averageResponseTime = 0.0;
        
        public EmailHealthMetrics(String tenantId) {
            this.tenantId = tenantId;
            this.lastHealthCheck = Instant.now();
        }
    }
    
    public enum HealthStatus {
        HEALTHY, WARNING, CRITICAL, UNKNOWN
    }
    
    // Circuit Breaker Implementation
    private static class CircuitBreaker {
        private final String tenantId;
        private final int failureThreshold;
        private final long timeoutMs;
        
        private int failureCount = 0;
        private Instant lastFailureTime;
        private CircuitState state = CircuitState.CLOSED;
        
        public CircuitBreaker(String tenantId, int failureThreshold, long timeoutMs) {
            this.tenantId = tenantId;
            this.failureThreshold = failureThreshold;
            this.timeoutMs = timeoutMs;
        }
        
        public boolean isOpen() {
            if (state == CircuitState.OPEN) {
                if (Instant.now().isAfter(lastFailureTime.plusMillis(timeoutMs))) {
                    state = CircuitState.HALF_OPEN;
                    return false;
                }
                return true;
            }
            return false;
        }
        
        public void recordSuccess() {
            failureCount = 0;
            state = CircuitState.CLOSED;
        }
        
        public void recordFailure() {
            failureCount++;
            lastFailureTime = Instant.now();
            
            if (failureCount >= failureThreshold) {
                state = CircuitState.OPEN;
            }
        }
        
        private enum CircuitState {
            CLOSED, OPEN, HALF_OPEN
        }
    }
    
    // Custom Exception
    public static class EmailCircuitBreakerException extends RuntimeException {
        public EmailCircuitBreakerException(String message) {
            super(message);
        }
    }
}