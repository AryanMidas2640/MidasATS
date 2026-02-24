package com.midas.consulting.service;

import com.midas.consulting.controller.v1.response.ServiceResultDTOs;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.RoutePermission;
import com.midas.consulting.model.Tenant;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceConfigurationService.class);

    @Autowired
    private TenantService tenantService;

    public ServiceResultDTOs.ServiceConfigurationResult configureServices(String subdomain, String checklistUrl,
                                                                          String jobSyncUrl, String webUrl,
                                                                          String logo, String email) {

        logger.info("Starting service configuration for subdomain: {}", subdomain);

        try {
            // Find existing tenant
            Tenant tenant = tenantService.getTenantBySubdomain(subdomain);
            if (tenant == null) {
                throw new MidasCustomException.EntityNotFoundException(
                        "Tenant not found for subdomain: " + subdomain);
            }

            // Update tenant with service configurations
            tenant.setChecklistUrl(checklistUrl)
                    .setWebUrl(webUrl)
                    .setLogo(logo)
                    .setEmail(email);

            // Save updated tenant
            Tenant updatedTenant = tenantService.updateTenant(tenant.getId(), tenant);

            // Setup default route permissions
            setupDefaultRoutePermissions(tenant.getConnectionString());

            // Setup database indexes for HRMS database
            int hrmsIndexesCreated = setupHRMSIndexes(tenant.getConnectionString());





            // Setup database indexes for Job Sync database if configured
            int jobSyncIndexesCreated = 0;
            if (tenant.getConnectionStringJobSync() != null) {
                jobSyncIndexesCreated = setupJobSyncIndexes(tenant.getConnectionStringJobSync());
            }

            // Count configured services
            int servicesConfigured = countConfiguredServices(checklistUrl, jobSyncUrl, webUrl, logo, email);

            logger.info("Service configuration completed for subdomain: {} with {} services and {} indexes created",
                    subdomain, servicesConfigured, hrmsIndexesCreated + jobSyncIndexesCreated);

            return new ServiceResultDTOs.ServiceConfigurationResult()
                    .setSuccess(true)
                    .setMessage(String.format("Services configured successfully with default permissions and %d indexes created",
                            hrmsIndexesCreated + jobSyncIndexesCreated))
                    .setTenantId(updatedTenant.getId())
                    .setServicesConfigured(servicesConfigured);

        } catch (Exception e) {
            logger.error("Failed to configure services for subdomain {}: {}", subdomain, e.getMessage(), e);

            return new ServiceResultDTOs.ServiceConfigurationResult()
                    .setSuccess(false)
                    .setMessage("Failed to configure services: " + e.getMessage())
                    .setServicesConfigured(0);
        }
    }

    private int setupHRMSIndexes(String connectionString) {
        int indexesCreated = 0;
        try {
            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);

            // Notes collection indexes
            IndexOperations notesOps = mongoTemplate.indexOps("notes");
            notesOps.ensureIndex(new Index().on("noteType", Sort.Direction.ASC));
            notesOps.ensureIndex(new Index().on("candidateId", Sort.Direction.DESC));
            indexesCreated += 2;

            // CandidateMidas collection indexes
            IndexOperations candidateOps = mongoTemplate.indexOps("candidateMidas");
            candidateOps.ensureIndex(new Index().on("changeLog", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("name", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("phone", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("state", Sort.Direction.ASC)
                    .on("city", Sort.Direction.ASC)
                    .on("fullText", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("email", Sort.Direction.DESC).unique());
            candidateOps.ensureIndex(new Index().on("skills", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("fullText", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("changeLog.id", Sort.Direction.DESC));
            candidateOps.ensureIndex(new Index().on("city", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("state", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("changeLog.user.id", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("city", Sort.Direction.DESC)
                    .on("state", Sort.Direction.DESC)
                    .on("_id", Sort.Direction.ASC));
            candidateOps.ensureIndex(new Index().on("changeLog", Sort.Direction.DESC));

            // Text index for candidateMidas
            TextIndexDefinition textIndexDef = new TextIndexDefinition.TextIndexDefinitionBuilder()
                    .onField("fullText")
                    .withDefaultLanguage("english")
                    .build();
            candidateOps.ensureIndex(textIndexDef);
            indexesCreated += 14;

            // Activities collection indexes
            IndexOperations activitiesOps = mongoTemplate.indexOps("activities");
            indexesCreated += ensureIndexSafely(activitiesOps, new Index().on("sourceID", Sort.Direction.DESC));
            indexesCreated += ensureIndexSafely(activitiesOps, new Index().on("providerJobID", Sort.Direction.DESC));
            indexesCreated += ensureIndexSafely(activitiesOps, new Index().on("candidateID", Sort.Direction.DESC));
            indexesCreated += ensureIndexSafely(activitiesOps, new Index().on("activityType", Sort.Direction.DESC));

            // ChangeLog collection indexes
            IndexOperations changeLogOps = mongoTemplate.indexOps("changeLog");
            changeLogOps.ensureIndex(new Index().on("user", Sort.Direction.ASC));
            changeLogOps.ensureIndex(new Index().on("userNotes", Sort.Direction.DESC));
            changeLogOps.ensureIndex(new Index().on("_class", Sort.Direction.ASC));
            changeLogOps.ensureIndex(new Index().on("user", Sort.Direction.DESC));
            indexesCreated += 4;

            // User collection indexes
            IndexOperations userOps = mongoTemplate.indexOps("user");
            userOps.ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
            userOps.ensureIndex(new Index().on("isActive", Sort.Direction.ASC));
            userOps.ensureIndex(new Index().on("userType", Sort.Direction.ASC));
            userOps.ensureIndex(new Index().on("manager", Sort.Direction.ASC));
            indexesCreated += 4;

            // Routes collection indexes
            IndexOperations routesOps = mongoTemplate.indexOps("routes");
            routesOps.ensureIndex(new Index().on("route", Sort.Direction.ASC));
            indexesCreated += 1;

            // Tenant collection index
            IndexOperations tenantOps = mongoTemplate.indexOps("tenant");
            indexesCreated += ensureIndexSafely(tenantOps, new Index().on("subdomain", Sort.Direction.ASC).unique());

            // Facility collection indexes
            IndexOperations facilityOps = mongoTemplate.indexOps("facility");
            indexesCreated += ensureIndexSafely(facilityOps, new Index().on("name", Sort.Direction.ASC).unique());

            // Organisation collection indexes
            IndexOperations orgOps = mongoTemplate.indexOps("organisation");
            indexesCreated += ensureIndexSafely(orgOps, new Index().on("name", Sort.Direction.DESC).unique());

            // Project collection indexes
            IndexOperations projectOps = mongoTemplate.indexOps("project");
            indexesCreated += ensureIndexSafely(projectOps, new Index().on("name", Sort.Direction.DESC).unique());

            // Client collection indexes (if exists)
            try {
                IndexOperations clientOps = mongoTemplate.indexOps("client");
                indexesCreated += ensureIndexSafely(clientOps, new Index().on("name", Sort.Direction.DESC).unique());
                indexesCreated += ensureIndexSafely(clientOps, new Index().on("phone", Sort.Direction.DESC).unique());
                indexesCreated += ensureIndexSafely(clientOps, new Index().on("email", Sort.Direction.DESC).unique());
            } catch (Exception e) {
                logger.debug("Client collection may not exist, skipping indexes");
            }

            // Employee collection indexes
            IndexOperations employeeOps = mongoTemplate.indexOps("employee");
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("name", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("address", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("city", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("contactDetails", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("createDate", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("dob", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("email", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("ssn", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeOps, new Index().on("state", Sort.Direction.DESC).unique());

            // EmployeeDocs collection indexes
            IndexOperations employeeDocsOps = mongoTemplate.indexOps("employeeDocs");
            indexesCreated += ensureIndexSafely(employeeDocsOps, new Index().on("type", Sort.Direction.DESC).unique());
            indexesCreated += ensureIndexSafely(employeeDocsOps, new Index().on("docName", Sort.Direction.DESC));

            // VMS collection indexes
            IndexOperations vmsOps = mongoTemplate.indexOps("vms");
            indexesCreated += ensureIndexSafely(vmsOps, new Index().on("name", Sort.Direction.DESC).unique());

            // Role collection indexes
            IndexOperations roleOps = mongoTemplate.indexOps("role");
            indexesCreated += ensureIndexSafely(roleOps, new Index().on("role", Sort.Direction.DESC).unique());

            // Division collection indexes
            IndexOperations divisionOps = mongoTemplate.indexOps("division");
            indexesCreated += ensureIndexSafely(divisionOps, new Index().on("name", Sort.Direction.ASC));

            // Team collection indexes
            IndexOperations teamOps = mongoTemplate.indexOps("team");
            indexesCreated += ensureIndexSafely(teamOps, new Index().on("name", Sort.Direction.ASC));

            logger.info("Successfully created {} indexes for HRMS database", indexesCreated);


            // Add this to the setupHRMSIndexes method in ServiceConfigurationService.java

// Add this section after the existing indexes in setupHRMSIndexes method:

// Storage Config collection indexes
            IndexOperations storageConfigOps = mongoTemplate.indexOps("storage_config");
            indexesCreated += ensureIndexSafely(storageConfigOps, new Index().on("tenantId", Sort.Direction.ASC).unique());
            indexesCreated += ensureIndexSafely(storageConfigOps, new Index().on("primaryProvider", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(storageConfigOps, new Index().on("active", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(storageConfigOps, new Index().on("dateCreated", Sort.Direction.DESC));

// Email Config collection indexes (if not already present)
            IndexOperations emailConfigOps = mongoTemplate.indexOps("email_config");
            indexesCreated += ensureIndexSafely(emailConfigOps, new Index().on("tenantId", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailConfigOps, new Index().on("configName", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailConfigOps, new Index().on("active", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailConfigOps, new Index().on("templateMappings", Sort.Direction.ASC));

// Email Template collection indexes
            IndexOperations emailTemplateOps = mongoTemplate.indexOps("email_templates");
            indexesCreated += ensureIndexSafely(emailTemplateOps, new Index().on("tenantId", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailTemplateOps, new Index().on("templateName", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailTemplateOps, new Index().on("active", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailTemplateOps, new Index().on("category", Sort.Direction.ASC));

// Email Template Mapping collection indexes
            IndexOperations emailTemplateMappingOps = mongoTemplate.indexOps("email_template_mapping");
            indexesCreated += ensureIndexSafely(emailTemplateMappingOps, new Index().on("tenantId", Sort.Direction.ASC).on("templateName", Sort.Direction.ASC).unique());
            indexesCreated += ensureIndexSafely(emailTemplateMappingOps, new Index().on("emailConfigId", Sort.Direction.ASC));

// Email Audit collection indexes
            IndexOperations emailAuditOps = mongoTemplate.indexOps("email_audit");
            indexesCreated += ensureIndexSafely(emailAuditOps, new Index().on("tenantId", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailAuditOps, new Index().on("recipient", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailAuditOps, new Index().on("status", Sort.Direction.ASC));
            indexesCreated += ensureIndexSafely(emailAuditOps, new Index().on("sentAt", Sort.Direction.DESC));
            indexesCreated += ensureIndexSafely(emailAuditOps, new Index().on("createdAt", Sort.Direction.ASC)); // For TTL index

        } catch (Exception e) {
            logger.error("Error creating HRMS indexes: {}", e.getMessage(), e);
        }

        return indexesCreated;
    }

    private int setupJobSyncIndexes(String connectionString) {
        int indexesCreated = 0;
        try {
            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);

            // JobRequisitions collection indexes
            IndexOperations jobReqOps = mongoTemplate.indexOps("jobRequisitions");
            jobReqOps.ensureIndex(new Index().on("formattedEndDate", Sort.Direction.ASC));
            jobReqOps.ensureIndex(new Index().on("formattedStartDate", Sort.Direction.ASC));
            jobReqOps.ensureIndex(new Index().on("city", Sort.Direction.ASC));
            jobReqOps.ensureIndex(new Index().on("state", Sort.Direction.ASC));
            jobReqOps.ensureIndex(new Index().on("providerJobID", Sort.Direction.ASC).unique());
            jobReqOps.ensureIndex(new Index().on("sourceID", Sort.Direction.ASC).unique());
            indexesCreated += 6;

            // Activities collection indexes for job sync
            IndexOperations activitiesOps = mongoTemplate.indexOps("activities");
            activitiesOps.ensureIndex(new Index().on("activityNote", Sort.Direction.ASC));
            activitiesOps.ensureIndex(new Index().on("providerJobID", Sort.Direction.ASC));
            activitiesOps.ensureIndex(new Index().on("candidateID", Sort.Direction.ASC));
            activitiesOps.ensureIndex(new Index().on("userID", Sort.Direction.ASC));
            activitiesOps.ensureIndex(new Index().on("sourceID", Sort.Direction.ASC));
            indexesCreated += 5;

            // SmsSessions collection indexes
            IndexOperations smsSessionsOps = mongoTemplate.indexOps("smsSessions");
            smsSessionsOps.ensureIndex(new Index().on("participants.isSessionOwner", Sort.Direction.ASC));
            smsSessionsOps.ensureIndex(new Index().on("latestMessage.sender.phoneNumber", Sort.Direction.ASC));
            smsSessionsOps.ensureIndex(new Index().on("latestMessage.sender.displayName", Sort.Direction.ASC));
            smsSessionsOps.ensureIndex(new Index().on("participants.phoneNumber", Sort.Direction.ASC));
            smsSessionsOps.ensureIndex(new Index().on("latestMessage.direction", Sort.Direction.ASC));
            smsSessionsOps.ensureIndex(new Index().on("latestMessage.sender", Sort.Direction.ASC));
            indexesCreated += 6;

            // User collection indexes for job sync
            IndexOperations userOps = mongoTemplate.indexOps("user");
            userOps.ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
            indexesCreated += 1;

            // Role collection indexes
            IndexOperations roleOps = mongoTemplate.indexOps("role");
            roleOps.ensureIndex(new Index().on("role", Sort.Direction.DESC).unique());
            indexesCreated += 1;

            // ZoomSmsHistory collection indexes
            IndexOperations zoomSmsOps = mongoTemplate.indexOps("zoomSmsHistory");
            zoomSmsOps.ensureIndex(new Index().on("messageId", Sort.Direction.ASC));
            indexesCreated += 1;

            // FeedUpdate collection indexes
            IndexOperations feedUpdateOps = mongoTemplate.indexOps("feedUpdate");
            feedUpdateOps.ensureIndex(new Index().on("updateTime", Sort.Direction.DESC).unique());
            feedUpdateOps.ensureIndex(new Index().on("updateTime", Sort.Direction.ASC).named("updateTimeIndex"));
            indexesCreated += 2;

            // VmsConfig collection indexes
            IndexOperations vmsConfigOps = mongoTemplate.indexOps("vmsConfig");
            vmsConfigOps.ensureIndex(new Index().on("vmsAM", Sort.Direction.ASC).unique());
            indexesCreated += 1;

            // VmsEnableConfig collection indexes
            IndexOperations vmsEnableConfigOps = mongoTemplate.indexOps("vmsEnableConfig");
            vmsEnableConfigOps.ensureIndex(new Index().on("vmsName", Sort.Direction.ASC).unique());
            indexesCreated += 1;

            // CommunicationRules collection indexes
            IndexOperations commRulesOps = mongoTemplate.indexOps("communicationRules");
            commRulesOps.ensureIndex(new Index().on("serviceName", Sort.Direction.ASC).unique());
            indexesCreated += 1;

            // JobPipeline collection indexes
            IndexOperations jobPipelineOps = mongoTemplate.indexOps("jobPipeline");
            jobPipelineOps.ensureIndex(new Index().on("email", Sort.Direction.ASC));
            jobPipelineOps.ensureIndex(new Index().on("firstName", Sort.Direction.ASC));
            jobPipelineOps.ensureIndex(new Index().on("lastName", Sort.Direction.ASC));
            indexesCreated += 3;

            // SkillChecklistMails collection indexes
            IndexOperations skillChecklistOps = mongoTemplate.indexOps("skillChecklistMails");
            skillChecklistOps.ensureIndex(new Index().on("email", Sort.Direction.ASC));
            indexesCreated += 1;

            // Skills_training collection indexes
            IndexOperations skillsTrainingOps = mongoTemplate.indexOps("skills_training");
            skillsTrainingOps.ensureIndex(new Index().on("skill", Sort.Direction.ASC));
            indexesCreated += 1;

            // UserSessions collection indexes
            IndexOperations userSessionsOps = mongoTemplate.indexOps("userSessions");
            userSessionsOps.ensureIndex(new Index().on("smsSessions.participants.phoneNumber", Sort.Direction.ASC));
            userSessionsOps.ensureIndex(new Index().on("smsSessions.latestMessage.sender.displayName", Sort.Direction.ASC));
            userSessionsOps.ensureIndex(new Index().on("smsSessions.latestMessage.direction", Sort.Direction.ASC));
            userSessionsOps.ensureIndex(new Index().on("smsSessions.latestMessage.sender", Sort.Direction.ASC));
            userSessionsOps.ensureIndex(new Index().on("smsSessions.participants.isSessionOwner", Sort.Direction.ASC));
            userSessionsOps.ensureIndex(new Index().on("smsSessions.latestMessage.sender.phoneNumber", Sort.Direction.ASC));
            indexesCreated += 6;

            logger.info("Successfully created {} indexes for Job Sync database", indexesCreated);

        } catch (Exception e) {
            logger.error("Error creating Job Sync indexes: {}", e.getMessage(), e);
        }

        return indexesCreated;
    }

    private int ensureIndexSafely(IndexOperations indexOps, Index index) {
        try {
            indexOps.ensureIndex(index);
            return 1;
        } catch (Exception e) {
            logger.debug("Index may already exist or collection doesn't exist: {}", e.getMessage());
            return 0;
        }
    }

    private void setupDefaultRoutePermissions(String connectionString) {
        try {
            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);
            // Define default route permissions based on your routes.json data
            List<RoutePermission> defaultRoutes = createDefaultRoutePermissions();

            for (RoutePermission route : defaultRoutes) {
                try {
                    // Check if route already exists
                    RoutePermission existingRoute = mongoTemplate.findOne(
                            org.springframework.data.mongodb.core.query.Query.query(
                                    org.springframework.data.mongodb.core.query.Criteria.where("route").is(route.getRoute())
                            ),
                            RoutePermission.class
                    );

                    if (existingRoute == null) {
                        mongoTemplate.save(route);
                        logger.debug("Created route permission: {}", route.getRoute());
                    }
                } catch (Exception e) {
                    logger.warn("Error creating route permission {}: {}", route.getRoute(), e.getMessage());
                }
            }

            logger.info("Default route permissions setup completed");

        } catch (Exception e) {
            logger.error("Error setting up default route permissions: {}", e.getMessage(), e);
        }
    }

    private List<RoutePermission> createDefaultRoutePermissions() {
        List<RoutePermission> routes = new ArrayList<>();

        // Dashboard - accessible to all roles
        routes.add(createRoutePermission("dashboard",
                createPermissionMap(true, true, true, true, true, true, true, true)));

        // User management
        routes.add(createRoutePermission("adduser",
                createPermissionMap(true, true, true, false, false, false, false, false)));

        routes.add(createRoutePermission("viewuser",
                createPermissionMap(true, false, true, false, false, false, false, false)));

        // Candidate management
        routes.add(createRoutePermission("Applicant",
                createPermissionMap(true, true, false, true, true, true, true, true)));

        routes.add(createRoutePermission("ApplicantSearch",
                createPermissionMap(true, true, false, true, true, true, true, true)));

        // Job management
        routes.add(createRoutePermission("allJobs",
                createPermissionMap(true, true, false, true, true, true, true, true)));

        routes.add(createRoutePermission("hotjobs",
                createPermissionMap(true, true, false, true, true, false, true, true)));

        routes.add(createRoutePermission("manualJobs",
                createPermissionMap(true, true, false, true, true, true, true, true)));

        routes.add(createRoutePermission("editJobs",
                createPermissionMap(true, true, false, false, false, false, false, false)));

        routes.add(createRoutePermission("assignedJobs",
                createPermissionMap(false, false, false, false, false, true, true, false)));

        routes.add(createRoutePermission("assignedJobsByManager",
                createPermissionMap(true, false, false, false, false, true, false, false)));

        // Client management
        routes.add(createRoutePermission("client",
                createPermissionMap(false, false, false, false, true, false, true, false)));

        routes.add(createRoutePermission("addClient",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("viewClient",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        // Submissions and checklist
        routes.add(createRoutePermission("submission",
                createPermissionMap(true, false, false, true, true, true, true, false)));

        routes.add(createRoutePermission("checklist",
                createPermissionMap(true, true, true, true, true, true, true, true)));

        routes.add(createRoutePermission("addchecklist",
                createPermissionMap(true, false, true, false, false, true, false, false)));

        // Administrative functions
        routes.add(createRoutePermission("permission",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("permissionRoles",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("tenant",
                createPermissionMap(true, true, false, false, false, false, false, false)));

        // Reports
        routes.add(createRoutePermission("candidatereport",
                createPermissionMap(true, true, false, true, true, false, true, true)));

        // Organization management
        routes.add(createRoutePermission("add-organisation",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("addOrganisation",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("viewOrganisation",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        // Division management
        routes.add(createRoutePermission("division",
                createPermissionMap(false, false, false, false, true, false, true, false)));

        // Facility management
        routes.add(createRoutePermission("addFacility",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        // Pipeline management
        routes.add(createRoutePermission("pipeline",
                createPermissionMap(true, false, false, false, false, true, false, false)));

        routes.add(createRoutePermission("viewpipeline",
                createPermissionMap(true, false, false, false, false, true, false, false)));

        // VMS and Stats
        routes.add(createRoutePermission("assignVms",
                createPermissionMap(true, false, false, false, false, true, false, false)));

        routes.add(createRoutePermission("feedStats",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        // Messaging (all disabled)
        routes.add(createRoutePermission("zoomMessage",
                createPermissionMap(false, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("unifiedMessage",
                createPermissionMap(false, false, false, false, false, false, false, false)));

        // Issue tracking (all disabled)
        routes.add(createRoutePermission("CreateIssue",
                createPermissionMap(false, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("ViewIssue",
                createPermissionMap(false, false, false, false, false, false, false, false)));

        // System management
        routes.add(createRoutePermission("control",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        routes.add(createRoutePermission("loginActivity",
                createPermissionMap(true, false, false, false, false, false, false, false)));

        // Health monitoring
        routes.add(createRoutePermission("healthMonitor",
                createPermissionMap(true, true, false, true, true, false, true, true)));


        routes.add(createRoutePermission("integration",
                createPermissionMap(true, true, false, true, true, false, true, true)));


        return routes;
    }

    /**
     * Helper method to create permission map for all roles
     * Order: SUPERADMIN, ADMIN, MODERATOR, RECRUITER, TEAMLEAD, GENERALMANAGER, ACCOUNTMANAGER, ONBOARD
     */
    private Map<String, Boolean> createPermissionMap(boolean superAdmin, boolean admin,
                                                     boolean moderator, boolean recruiter, boolean teamLead, boolean generalManager,
                                                     boolean accountManager, boolean onboard) {
        Map<String, Boolean> permissions = new HashMap<>();
        permissions.put("SUPERADMIN", superAdmin);
        permissions.put("ADMIN", admin);
        permissions.put("MODERATOR", moderator);
        permissions.put("RECRUITER", recruiter);
        permissions.put("TEAMLEAD", teamLead);
        permissions.put("GENERALMANAGER", generalManager);
        permissions.put("ACCOUNTMANAGER", accountManager);
        permissions.put("ONBOARD", onboard);
        return permissions;
    }

    private RoutePermission createRoutePermission(String route, Map<String, Boolean> permissions) {
        RoutePermission routePermission = new RoutePermission();
        routePermission.setRoute(route);
        routePermission.setPermissions(permissions);
        return routePermission;
    }

    private int countConfiguredServices(String checklistUrl, String jobSyncUrl,
                                        String webUrl, String logo, String email) {
        int count = 0;

        if (isValidUrl(checklistUrl)) count++;
        if (isValidUrl(jobSyncUrl)) count++;
        if (isValidUrl(webUrl)) count++;
        if (isValidUrl(logo)) count++;
        if (isValidEmail(email)) count++;

        return count;
    }

    private boolean isValidUrl(String url) {
        return url != null && !url.trim().isEmpty() && url.startsWith("http");
    }

    private boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() && email.contains("@");
    }

    private MongoTemplate createTemporaryMongoTemplate(String connectionString) {
        MongoClient mongoClient = MongoClients.create(connectionString);
        String databaseName = extractDatabaseName(connectionString);
        return new MongoTemplate(mongoClient, databaseName);
    }

    private String extractDatabaseName(String connectionString) {
        String regex = "mongodb://.*?/(.*?)(\\?|$)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(connectionString);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new IllegalArgumentException("Cannot extract database name from connection string");
    }
}

//package com.midas.consulting.service;
//
//import com.midas.consulting.controller.v1.response.ServiceResultDTOs;
//import com.midas.consulting.exception.MidasCustomException;
//import com.midas.consulting.model.RoutePermission;
//import com.midas.consulting.model.Tenant;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class ServiceConfigurationService {
//
//    private static final Logger logger = LoggerFactory.getLogger(ServiceConfigurationService.class);
//
//    @Autowired
//    private TenantService tenantService;
//
//    public ServiceResultDTOs.ServiceConfigurationResult configureServices(String subdomain, String checklistUrl,
//                                                                          String jobSyncUrl, String webUrl,
//                                                                          String logo, String email) {
//
//        logger.info("Starting service configuration for subdomain: {}", subdomain);
//
//        try {
//            // Find existing tenant
//            Tenant tenant = tenantService.getTenantBySubdomain(subdomain);
//            if (tenant == null) {
//                throw new MidasCustomException.EntityNotFoundException(
//                        "Tenant not found for subdomain: " + subdomain);
//            }
//
//            // Update tenant with service configurations
//            tenant.setChecklistUrl(checklistUrl)
//                    .setWebUrl(webUrl)
//                    .setLogo(logo)
//                    .setEmail(email);
//
//            // Save updated tenant
//            Tenant updatedTenant = tenantService.updateTenant(tenant.getId(), tenant);
//
//            // Setup default route permissions
//            setupDefaultRoutePermissions(tenant.getConnectionString());
//
//            // Count configured services
//            int servicesConfigured = countConfiguredServices(checklistUrl, jobSyncUrl, webUrl, logo, email);
//
//            logger.info("Service configuration completed for subdomain: {} with {} services",
//                    subdomain, servicesConfigured);
//
//            return new ServiceResultDTOs.ServiceConfigurationResult()
//                    .setSuccess(true)
//                    .setMessage("Services configured successfully with default permissions")
//                    .setTenantId(updatedTenant.getId())
//                    .setServicesConfigured(servicesConfigured);
//
//        } catch (Exception e) {
//            logger.error("Failed to configure services for subdomain {}: {}", subdomain, e.getMessage(), e);
//
//            return new ServiceResultDTOs.ServiceConfigurationResult()
//                    .setSuccess(false)
//                    .setMessage("Failed to configure services: " + e.getMessage())
//                    .setServicesConfigured(0);
//        }
//    }
//
//    private void setupDefaultRoutePermissions(String connectionString) {
//        try {
//            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);
//            // Define default route permissions based on your routes.json data
//            List<RoutePermission> defaultRoutes = createDefaultRoutePermissions();
//
//            for (RoutePermission route : defaultRoutes) {
//                try {
//                    // Check if route already exists
//                    RoutePermission existingRoute = mongoTemplate.findOne(
//                            org.springframework.data.mongodb.core.query.Query.query(
//                                    org.springframework.data.mongodb.core.query.Criteria.where("route").is(route.getRoute())
//                            ),
//                            RoutePermission.class
//                    );
//
//                    if (existingRoute == null) {
//                        mongoTemplate.save(route);
//                        logger.debug("Created route permission: {}", route.getRoute());
//                    }
//                } catch (Exception e) {
//                    logger.warn("Error creating route permission {}: {}", route.getRoute(), e.getMessage());
//                }
//            }
//
//            logger.info("Default route permissions setup completed");
//
//        } catch (Exception e) {
//            logger.error("Error setting up default route permissions: {}", e.getMessage(), e);
//        }
//    }
//
//    private List<RoutePermission> createDefaultRoutePermissions() {
//        List<RoutePermission> routes = new ArrayList<>();
//
//        // Dashboard - accessible to all roles
//        routes.add(createRoutePermission("dashboard",
//                createPermissionMap(true, true, true, true, true, true, true, true)));
//
//        // User management
//        routes.add(createRoutePermission("adduser",
//                createPermissionMap(true, true, true, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("viewuser",
//                createPermissionMap(true, false, true, false, false, false, false, false)));
//
//        // Candidate management
//        routes.add(createRoutePermission("Applicant",
//                createPermissionMap(true, true, false, true, true, true, true, true)));
//
//        routes.add(createRoutePermission("ApplicantSearch",
//                createPermissionMap(true, true, false, true, true, true, true, true)));
//
//        // Job management
//        routes.add(createRoutePermission("allJobs",
//                createPermissionMap(true, true, false, true, true, true, true, true)));
//
//        routes.add(createRoutePermission("hotjobs",
//                createPermissionMap(true, true, false, true, true, false, true, true)));
//
//        routes.add(createRoutePermission("manualJobs",
//                createPermissionMap(true, true, false, true, true, true, true, true)));
//
//        routes.add(createRoutePermission("editJobs",
//                createPermissionMap(true, true, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("assignedJobs",
//                createPermissionMap(false, false, false, false, false, true, true, false)));
//
//        routes.add(createRoutePermission("assignedJobsByManager",
//                createPermissionMap(true, false, false, false, false, true, false, false)));
//
//        // Client management
//        routes.add(createRoutePermission("client",
//                createPermissionMap(false, false, false, false, true, false, true, false)));
//
//        routes.add(createRoutePermission("addClient",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("viewClient",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        // Submissions and checklist
//        routes.add(createRoutePermission("submission",
//                createPermissionMap(true, false, false, true, true, true, true, false)));
//
//        routes.add(createRoutePermission("checklist",
//                createPermissionMap(true, true, true, true, true, true, true, true)));
//
//        routes.add(createRoutePermission("addchecklist",
//                createPermissionMap(true, false, true, false, false, true, false, false)));
//
//        // Administrative functions
//        routes.add(createRoutePermission("permission",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("permissionRoles",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("tenant",
//                createPermissionMap(true, true, false, false, false, false, false, false)));
//
//        // Reports
//        routes.add(createRoutePermission("candidatereport",
//                createPermissionMap(true, true, false, true, true, false, true, true)));
//
//        // Organization management
//        routes.add(createRoutePermission("add-organisation",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("addOrganisation",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("viewOrganisation",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        // Division management
//        routes.add(createRoutePermission("division",
//                createPermissionMap(true, false, false, false, true, false, true, false)));
//
//        // Facility management
//        routes.add(createRoutePermission("addFacility",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        // Pipeline management
//        routes.add(createRoutePermission("pipeline",
//                createPermissionMap(true, false, false, false, false, true, false, false)));
//
//        routes.add(createRoutePermission("viewpipeline",
//                createPermissionMap(true, false, false, false, false, true, false, false)));
//
//        // VMS and Stats
//        routes.add(createRoutePermission("assignVms",
//                createPermissionMap(true, false, false, false, false, true, false, false)));
//
//        routes.add(createRoutePermission("feedStats",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        // Messaging (all disabled)
//        routes.add(createRoutePermission("zoomMessage",
//                createPermissionMap(false, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("unifiedMessage",
//                createPermissionMap(false, false, false, false, false, false, false, false)));
//
//        // Issue tracking (all disabled)
//        routes.add(createRoutePermission("CreateIssue",
//                createPermissionMap(false, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("ViewIssue",
//                createPermissionMap(false, false, false, false, false, false, false, false)));
//
//        // System management
//        routes.add(createRoutePermission("control",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        routes.add(createRoutePermission("loginActivity",
//                createPermissionMap(true, false, false, false, false, false, false, false)));
//
//        // Health monitoring
//        routes.add(createRoutePermission("healthMonitor",
//                createPermissionMap(true, true, false, true, true, false, true, true)));
//
//        return routes;
//    }
//
//    /**
//     * Helper method to create permission map for all roles
//     * Order: SUPERADMIN, ADMIN, MODERATOR, RECRUITER, TEAMLEAD, GENERALMANAGER, ACCOUNTMANAGER, ONBOARD
//     */
//    private Map<String, Boolean> createPermissionMap(boolean superAdmin, boolean admin,
//                                                     boolean moderator, boolean recruiter, boolean teamLead, boolean generalManager,
//                                                     boolean accountManager, boolean onboard)
//
//    {
//        Map<String, Boolean> permissions = new HashMap<>();
//        permissions.put("SUPERADMIN", superAdmin);
//        permissions.put("ADMIN", admin);
//        permissions.put("MODERATOR", moderator);
//        permissions.put("RECRUITER", recruiter);
//        permissions.put("TEAMLEAD", teamLead);
//        permissions.put("GENERALMANAGER", generalManager);
//        permissions.put("ACCOUNTMANAGER", accountManager);
//        permissions.put("ONBOARD", onboard);
//        return permissions;
//    }
//
////    private List<RoutePermission> createDefaultRoutePermissions() {
////        List<RoutePermission> routes = new ArrayList<>();
////
////        // Dashboard - accessible to all roles
////        routes.add(createRoutePermission("dashboard",
////                createPermissionMap(true, true, true, true, true, true, true, true)));
////
////
////        routes.add(createRoutePermission("ApplicantSearch",
////                createPermissionMap(true, true, false, true, true, true, true, true)));
////        // User management
////        routes.add(createRoutePermission("adduser",
////                createPermissionMap(true, true, true, false, false, false, false, false)));
////
////        routes.add(createRoutePermission("viewuser",
////                createPermissionMap(true, false, true, false, false, false, false, false)));
////
////        // Candidate management
////        routes.add(createRoutePermission("Applicant",
////                createPermissionMap(true, true, false, true, true, true, true, true)));
////
////        routes.add(createRoutePermission("ApplicantSearch",
////                createPermissionMap(true, true, false, true, true, true, true, true)));
////
////        // Job management
////        routes.add(createRoutePermission("allJobs",
////                createPermissionMap(true, true, false, true, true, true, true, true)));
////
////        routes.add(createRoutePermission("hotjobs",
////                createPermissionMap(true, true, false, true, true, false, true, true)));
////
////        routes.add(createRoutePermission("manualJobs",
////                createPermissionMap(true, true, false, true, true, true, true, true)));
////
////        // Client management
////        routes.add(createRoutePermission("client",
////                createPermissionMap(false, false, false, false, true, false, true, false)));
////
////        // Submissions and checklist
////        routes.add(createRoutePermission("submission",
////                createPermissionMap(true, false, false, true, true, true, true, false)));
////
////        routes.add(createRoutePermission("checklist",
////                createPermissionMap(true, true, true, true, true, true, true, true)));
////
////        // Administrative functions
////        routes.add(createRoutePermission("permission",
////                createPermissionMap(true, false, false, false, false, false, false, false)));
////
////        routes.add(createRoutePermission("tenant",
////                createPermissionMap(true, true, false, false, false, false, false, false)));
////
////        // Reports
////        routes.add(createRoutePermission("candidatereport",
////                createPermissionMap(true, true, false, true, true, false, true, true)));
////
////        // Organization management
////        routes.add(createRoutePermission("add-organisation",
////                createPermissionMap(true, false, false, false, false, false, false, false)));
////
////        return routes;
////    }
////
////    /**
////     * Helper method to create permission map for all roles
////     * Order: SUPERADMIN, ADMIN, MODERATOR, RECRUITER, TEAMLEAD, GENERALMANAGER, ACCOUNTMANAGER, ONBOARD
////     */
////    private Map<String, Boolean> createPermissionMap(boolean superAdmin, boolean admin,
////                                                     boolean moderator, boolean recruiter, boolean teamLead, boolean generalManager,
////                                                     boolean accountManager, boolean onboard) {
////        Map<String, Boolean> permissions = new HashMap<>();
////        permissions.put("SUPERADMIN", superAdmin);
////        permissions.put("ADMIN", admin);
////        permissions.put("MODERATOR", moderator);
////        permissions.put("RECRUITER", recruiter);
////        permissions.put("TEAMLEAD", teamLead);
////        permissions.put("GENERALMANAGER", generalManager);
////        permissions.put("ACCOUNTMANAGER", accountManager);
////        permissions.put("ONBOARD", onboard);
////        permissions.put("PARTNER", onboard);
////        return permissions;
////    }
//    private RoutePermission createRoutePermission(String route, Map<String, Boolean> permissions) {
//        RoutePermission routePermission = new RoutePermission();
//        routePermission.setRoute(route);
//        routePermission.setPermissions(permissions);
//        return routePermission;
//    }
//
//    private int countConfiguredServices(String checklistUrl, String jobSyncUrl,
//                                        String webUrl, String logo, String email) {
//        int count = 0;
//
//        if (isValidUrl(checklistUrl)) count++;
//        if (isValidUrl(jobSyncUrl)) count++;
//        if (isValidUrl(webUrl)) count++;
//        if (isValidUrl(logo)) count++;
//        if (isValidEmail(email)) count++;
//
//        return count;
//    }
//
//    private boolean isValidUrl(String url) {
//        return url != null && !url.trim().isEmpty() && url.startsWith("http");
//    }
//
//    private boolean isValidEmail(String email) {
//        return email != null && !email.trim().isEmpty() && email.contains("@");
//    }
//
//    private MongoTemplate createTemporaryMongoTemplate(String connectionString) {
//        MongoClient mongoClient = MongoClients.create(connectionString);
//        String databaseName = extractDatabaseName(connectionString);
//        return new MongoTemplate(mongoClient, databaseName);
//    }
//
//    private String extractDatabaseName(String connectionString) {
//        String regex = "mongodb://.*?/(.*?)(\\?|$)";
//        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
//        java.util.regex.Matcher matcher = pattern.matcher(connectionString);
//
//        if (matcher.find()) {
//            return matcher.group(1);
//        }
//
//        throw new IllegalArgumentException("Cannot extract database name from connection string");
//    }
//}
