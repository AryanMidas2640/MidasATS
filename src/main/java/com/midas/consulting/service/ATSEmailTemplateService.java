package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.service.storage.TenantConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@Component
public class ATSEmailTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(ATSEmailTemplateService.class);

    @Autowired
    private EnhancedTenantEmailService tenantEmailService;

    @Autowired
    private TenantConfigService tenantConfigService;

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    // ===== AUTHENTICATION & USER MANAGEMENT EMAILS =====

    /**
     * Send password reset email to user
     */
    public boolean sendPasswordResetEmail(String tenantId, String userEmail,
                                          String userName, String resetLink) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("resetLink", resetLink);
            variables.put("expirationHours", "24");

            return tenantEmailService.sendTemplatedEmail(
                    tenantId, userEmail,
                    "Password Reset Request",
                    "password-reset",
                    variables
            );
        } catch (Exception e) {
            logger.error("Failed to send password reset email for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send welcome email to new user
     */
    public boolean sendWelcomeEmail(String tenantId, String userEmail,
                                    String userName, String userRole,
                                    String tempPassword, String loginUrl) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("userRole", userRole);
            variables.put("userEmail", userEmail);
            variables.put("tempPassword", tempPassword);
            variables.put("loginUrl", loginUrl);

            String tenantName = getTenantName(tenantId);
            String subject = String.format("Welcome to %s - ATS Account Created", tenantName);

            return tenantEmailService.sendTemplatedEmail(
                    tenantId, userEmail,
                    subject,
                    "welcome-user",
                    variables
            );
        } catch (Exception e) {
            logger.error("Failed to send welcome email for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send account activation email
     */
    public boolean sendAccountActivationEmail(String tenantId, String userEmail,
                                              String userName, String activationLink) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", userName);
            variables.put("activationLink", activationLink);
            variables.put("expirationHours", "48");

            return tenantEmailService.sendTemplatedEmail(
                    tenantId, userEmail,
                    "Account Activation Required",
                    "account-activation",
                    variables
            );
        } catch (Exception e) {
            logger.error("Failed to send activation email for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }

    // ===== CANDIDATE COMMUNICATION EMAILS =====

    /**
     * Send application received confirmation
     */
    public boolean sendApplicationReceivedEmail(String tenantId, String candidateEmail,
                                                String candidateName, String jobTitle,
                                                String applicationId) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("candidateName", candidateName);
            variables.put("jobTitle", jobTitle);
            variables.put("applicationId", applicationId);
            variables.put("submissionDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            String subject = String.format("Application Received - %s", jobTitle);

            return tenantEmailService.sendTemplatedEmail(
                    tenantId, candidateEmail,
                    subject,
                    "application-received",
                    variables
            );
        } catch (Exception e) {
            logger.error("Failed to send application received email for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send application status update
     */
    public boolean sendApplicationStatusUpdate(String tenantId, String candidateEmail,
                                               String candidateName, String jobTitle,
                                               String currentStatus, String statusMessage) {
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("candidateName", candidateName);
            variables.put("jobTitle", jobTitle);
            variables.put("currentStatus", currentStatus);
            variables.put("statusMessage", statusMessage);
            variables.put("updateDate", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

            String subject = String.format("Application Status Update - %s", jobTitle);

            return tenantEmailService.sendTemplatedEmail(
                    tenantId, candidateEmail,
                    subject,
                    "application-status-update",
                    variables
            );
        } catch (Exception e) {
            logger.error("Failed to send application status update for tenant {}: {}", tenantId, e.getMessage(), e);
            return false;
        }
    }

    // ===== HELPER METHODS =====

    /**
     * Get tenant name from database
     */
    private String getTenantName(String tenantId) {
        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = Query.query(Criteria.where("id").is(tenantId));
            Tenant tenant = mongoTemplate.findOne(query, Tenant.class);
            return tenant != null ? tenant.getTenantName() : "Your Organization";
        } catch (Exception e) {
            logger.warn("Failed to get tenant name for {}: {}", tenantId, e.getMessage());
            return "Your Organization";
        }
    }
}