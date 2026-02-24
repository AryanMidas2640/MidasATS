package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.response.ServiceResultDTOs;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.model.user.Role;
import com.midas.consulting.model.user.User;
import com.midas.consulting.model.user.UserRoles;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DatabaseSetupService {


    private static final Logger logger = LoggerFactory.getLogger(DatabaseSetupService.class);

    // Add these autowired dependencies (add after existing @Autowired fields)
    @Autowired
    private DefaultStorageConfigService defaultStorageConfigService;

    // Update the initializeTenantDatabase method to include storage setup
    public ServiceResultDTOs.DatabaseSetupResult initializeTenantDatabase(String subdomain, String email,
                                                                          String hrmsConnectionString,
                                                                          String nodeConnectionString,
                                                                          String jobSyncConnectionString) {
        logger.info("Starting comprehensive database initialization for subdomain: {}", subdomain);

        try {
            // ... existing validation and tenant creation code ...

            // Create tenant record first
            Tenant newTenant = createTenantRecord(subdomain, hrmsConnectionString, nodeConnectionString, jobSyncConnectionString);
            logger.info("Created tenant record for subdomain: {}", subdomain);

            // Initialize all three databases
            DatabaseResult hrmsResult = initializeHRMSDatabase(hrmsConnectionString, email);
            DatabaseResult nodeResult = initializeNodeDatabase(nodeConnectionString, subdomain);
            DatabaseResult jobSyncResult = initializeJobSyncDatabase(jobSyncConnectionString, subdomain);

            // *** NEW: Create default email configuration and templates ***
            setupDefaultEmailSystem(newTenant.getId(), email);

            // *** ADD THIS: Create default storage configuration ***
            setupDefaultStorageSystem(newTenant.getId());

            // Calculate totals
            int totalCollections = hrmsResult.collectionsCreated + nodeResult.collectionsCreated + jobSyncResult.collectionsCreated;
            int totalIndexes = hrmsResult.indexesCreated + nodeResult.indexesCreated + jobSyncResult.indexesCreated;

            logger.info("Database initialization completed for subdomain: {} - Total Collections: {}, Total Indexes: {}",
                    subdomain, totalCollections, totalIndexes);

            return new ServiceResultDTOs.DatabaseSetupResult()
                    .setSuccess(true)
                    .setMessage(String.format("All databases initialized successfully with email and storage systems. HRMS: %d collections, Node: %d collections, JobSync: %d collections",
                            hrmsResult.collectionsCreated, nodeResult.collectionsCreated, jobSyncResult.collectionsCreated))
                    .setCollectionsCreated(totalCollections)
                    .setIndexesCreated(totalIndexes);

        } catch (Exception e) {
            logger.error("Failed to initialize databases for subdomain {}: {}", subdomain, e.getMessage(), e);
            throw e;
        }
    }

    // Add this new method
    private void setupDefaultStorageSystem(String tenantId) {
        try {
            logger.info("Setting up default storage system for tenant: {}", tenantId);

            // Set tenant context for storage service
            TenantContext.setCurrentTenant(tenantId);

            // Create default storage configuration
            defaultStorageConfigService.createDefaultStorageConfig(tenantId);

            logger.info("Default storage system setup completed for tenant: {}", tenantId);

        } catch (Exception e) {
            logger.error("Failed to setup default storage system for tenant {}: {}", tenantId, e.getMessage(), e);
            // Don't throw exception - storage setup is not critical for tenant creation
        } finally {
            TenantContext.clear();
        }
    }



    @Autowired
    private TenantService tenantService;

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    // Collections for HRMS database (main application)
    private static final List<String> HRMS_COLLECTIONS = Arrays.asList(
            "user", "role", "candidateMidas", "activities", "notes", "loginStats",
            "employee", "project", "client", "vms", "facility", "organisation",
            "timeSheet", "employeeDocs", "submissionDoc", "changeLog", "team",
            "division", "routes", "communicationLog", "candidateJobTags",
            "interviews", "locations", "storage_config", "tenant", "email_config",
            "email_templates", "email_template_mapping", "email_audit"
    );

    // Collections for Node/Checklist database
    private static final List<String> CHECKLIST_COLLECTIONS = Arrays.asList(
            "newchecklists", "checklists"
    );

    // Collections for Job Sync database
    private static final List<String> JOB_SYNC_COLLECTIONS = Arrays.asList(
            "jobs", "jobFeeds", "jobSources", "jobMappings", "jobSync",
            "jobCategories", "jobStatuses", "jobAssignments", "jobMetrics"
    );

//    public ServiceResultDTOs.DatabaseSetupResult initializeTenantDatabase(String subdomain,String email,
//                                                                          String hrmsConnectionString,
//                                                                          String nodeConnectionString,
//                                                                          String jobSyncConnectionString) {
//        logger.info("Starting comprehensive database initialization for subdomain: {}", subdomain);
//
//        try {
//            // Check if tenant already exists by subdomain (not by connection string)
//            Tenant existingTenant = tenantService.getTenantBySubdomain(subdomain);
//            if (existingTenant != null) {
//                logger.warn("Tenant already exists for subdomain: {}", subdomain);
//                throw new MidasCustomException.DuplicateEntityException(
//                        "Tenant already exists for subdomain: " + subdomain);
//            }
//
//            // Test all connection strings
//            validateConnectionStrings(hrmsConnectionString, nodeConnectionString, jobSyncConnectionString);
//
//            // Create tenant record first
//            Tenant newTenant = createTenantRecord(subdomain, hrmsConnectionString, nodeConnectionString, jobSyncConnectionString);
//            logger.info("Created tenant record for subdomain: {}", subdomain);
//
//            // Initialize all three databases
//            DatabaseResult hrmsResult = initializeHRMSDatabase(hrmsConnectionString, email);
//            DatabaseResult nodeResult = initializeNodeDatabase(nodeConnectionString, subdomain);
//            DatabaseResult jobSyncResult = initializeJobSyncDatabase(jobSyncConnectionString, subdomain);
//
//            // Calculate totals
//            int totalCollections = hrmsResult.collectionsCreated + nodeResult.collectionsCreated + jobSyncResult.collectionsCreated;
//            int totalIndexes = hrmsResult.indexesCreated + nodeResult.indexesCreated + jobSyncResult.indexesCreated;
//
//            logger.info("Database initialization completed for subdomain: {} - Total Collections: {}, Total Indexes: {}",
//                    subdomain, totalCollections, totalIndexes);
//
//            return new ServiceResultDTOs.DatabaseSetupResult()
//                    .setSuccess(true)
//                    .setMessage(String.format("All databases initialized successfully. HRMS: %d collections, Node: %d collections, JobSync: %d collections",
//                            hrmsResult.collectionsCreated, nodeResult.collectionsCreated, jobSyncResult.collectionsCreated))
//                    .setCollectionsCreated(totalCollections)
//                    .setIndexesCreated(totalIndexes);
//
//        } catch (Exception e) {
//            logger.error("Failed to initialize databases for subdomain {}: {}", subdomain, e.getMessage(), e);
//            throw e;
//        }
//    }

    private void validateConnectionStrings(String hrmsConnectionString, String nodeConnectionString, String jobSyncConnectionString) {
        if (!testConnection(hrmsConnectionString)) {
            throw new IllegalArgumentException("Cannot connect to HRMS database with provided connection string");
        }
        if (!testConnection(nodeConnectionString)) {
            throw new IllegalArgumentException("Cannot connect to Node database with provided connection string");
        }
        if (!testConnection(jobSyncConnectionString)) {
            throw new IllegalArgumentException("Cannot connect to JobSync database with provided connection string");
        }
        logger.info("All connection strings validated successfully");
    }

    private Tenant createTenantRecord(String subdomain, String hrmsConnectionString, String nodeConnectionString, String jobSyncConnectionString) {
        Tenant newTenant = new Tenant()
                .setSubdomain(subdomain)
                .setConnectionString(hrmsConnectionString)
                .setConnectionStringNode(nodeConnectionString)
                .setConnectionStringJobSync(jobSyncConnectionString)
                .setTenantName(subdomain);

        return tenantService.createTenant(newTenant);
    }

    private DatabaseResult initializeHRMSDatabase(String connectionString, String subdomain) {
        logger.info("Initializing HRMS database for subdomain: {}", subdomain);

        int collectionsCreated = createStandardCollections(connectionString, HRMS_COLLECTIONS, "HRMS");
        int indexesCreated = createHRMSIndexes(connectionString);

        // Create default roles and admin user only in HRMS database
        createDefaultRolesAndUser(connectionString, subdomain);

        return new DatabaseResult(collectionsCreated, indexesCreated);
    }

    private DatabaseResult initializeNodeDatabase(String connectionString, String subdomain) {
        logger.info("Initializing Node/Checklist database for subdomain: {}", subdomain);

        int collectionsCreated = createStandardCollections(connectionString, CHECKLIST_COLLECTIONS, "Checklist");
        int indexesCreated = createChecklistIndexes(connectionString);

        return new DatabaseResult(collectionsCreated, indexesCreated);
    }

    private DatabaseResult initializeJobSyncDatabase(String connectionString, String subdomain) {
        logger.info("Initializing JobSync database for subdomain: {}", subdomain);

        int collectionsCreated = createStandardCollections(connectionString, JOB_SYNC_COLLECTIONS, "JobSync");
        int indexesCreated = createJobSyncIndexes(connectionString);

        return new DatabaseResult(collectionsCreated, indexesCreated);
    }

    private boolean testConnection(String connectionString) {
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            // Test connection by listing databases
            mongoClient.listDatabaseNames().first();
            logger.debug("Database connection test successful for: {}", maskConnectionString(connectionString));
            return true;
        } catch (Exception e) {
            logger.error("Database connection test failed for {}: {}", maskConnectionString(connectionString), e.getMessage());
            return false;
        }
    }

    private int createStandardCollections(String connectionString, List<String> collections, String dbType) {
        int collectionsCreated = 0;

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            String databaseName = extractDatabaseName(connectionString);
            MongoDatabase database = mongoClient.getDatabase(databaseName);

            for (String collectionName : collections) {
                try {
                    database.createCollection(collectionName);
                    collectionsCreated++;
                    logger.debug("Created {} collection: {}", dbType, collectionName);
                } catch (Exception e) {
                    logger.debug("{} collection {} may already exist: {}", dbType, collectionName, e.getMessage());
                }
            }
        }

        logger.info("Created {} collections in {} database", collectionsCreated, dbType);
        return collectionsCreated;
    }

    private int createHRMSIndexes(String connectionString) {
        int indexesCreated = 0;

        try {
            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);

            // User collection indexes
            mongoTemplate.indexOps("user").ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
            indexesCreated++;

            // CandidateMidas collection indexes
            mongoTemplate.indexOps("candidateMidas").ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
            mongoTemplate.indexOps("candidateMidas").ensureIndex(new Index().on("phone", Sort.Direction.ASC));
            mongoTemplate.indexOps("candidateMidas").ensureIndex(new Index().on("skills", Sort.Direction.ASC));
            mongoTemplate.indexOps("candidateMidas").ensureIndex(new Index().on("fullText", Sort.Direction.ASC));
            indexesCreated += 4;

            // Activities collection indexes
            mongoTemplate.indexOps("activities").ensureIndex(new Index().on("candidateID", Sort.Direction.ASC));
            mongoTemplate.indexOps("activities").ensureIndex(new Index().on("dateCreated", Sort.Direction.DESC));
            indexesCreated += 2;

            // Employee collection indexes
            mongoTemplate.indexOps("employee").ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
            indexesCreated++;

            // Project collection indexes
            mongoTemplate.indexOps("project").ensureIndex(new Index().on("name", Sort.Direction.ASC).unique());
            indexesCreated++;

            // Role collection indexes
            mongoTemplate.indexOps("role").ensureIndex(new Index().on("role", Sort.Direction.ASC).unique());
            indexesCreated++;

            logger.info("Created {} indexes in HRMS database", indexesCreated);

        } catch (Exception e) {
            logger.error("Error creating HRMS indexes: {}", e.getMessage(), e);
        }

        return indexesCreated;
    }

    private int createChecklistIndexes(String connectionString) {
        int indexesCreated = 0;

        try {
            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);

            // Checklist indexes
            mongoTemplate.indexOps("checklists").ensureIndex(new Index().on("templateId", Sort.Direction.ASC));
            mongoTemplate.indexOps("checklists").ensureIndex(new Index().on("status", Sort.Direction.ASC));
            mongoTemplate.indexOps("checklists").ensureIndex(new Index().on("createdDate", Sort.Direction.DESC));
            indexesCreated += 3;

            // Submissions indexes
            mongoTemplate.indexOps("submissions").ensureIndex(new Index().on("checklistId", Sort.Direction.ASC));
            mongoTemplate.indexOps("submissions").ensureIndex(new Index().on("submittedDate", Sort.Direction.DESC));
            indexesCreated += 2;

            logger.info("Created {} indexes in Checklist database", indexesCreated);

        } catch (Exception e) {
            logger.error("Error creating Checklist indexes: {}", e.getMessage(), e);
        }

        return indexesCreated;
    }

    private int createJobSyncIndexes(String connectionString) {
        int indexesCreated = 0;

        try {
            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);

            // Jobs indexes
            mongoTemplate.indexOps("jobs").ensureIndex(new Index().on("jobId", Sort.Direction.ASC).unique());
            mongoTemplate.indexOps("jobs").ensureIndex(new Index().on("status", Sort.Direction.ASC));
            mongoTemplate.indexOps("jobs").ensureIndex(new Index().on("createdDate", Sort.Direction.DESC));
            indexesCreated += 3;

            // JobFeeds indexes
            mongoTemplate.indexOps("jobFeeds").ensureIndex(new Index().on("sourceId", Sort.Direction.ASC));
            mongoTemplate.indexOps("jobFeeds").ensureIndex(new Index().on("syncDate", Sort.Direction.DESC));
            indexesCreated += 2;

            logger.info("Created {} indexes in JobSync database", indexesCreated);

        } catch (Exception e) {
            logger.error("Error creating JobSync indexes: {}", e.getMessage(), e);
        }

        return indexesCreated;
    }

    private void createDefaultRolesAndUser(String connectionString, String email) {
        try {
            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);

            // Create default roles
            createDefaultRoles(mongoTemplate);

            // Create default admin user
            createDefaultAdminUser(mongoTemplate, email);

            logger.info("Created default roles and admin user for subdomain: {}", email);

        } catch (Exception e) {
            logger.error("Error creating default roles and user: {}", e.getMessage(), e);
        }
    }
//
//    private void createDefaultRoles(MongoTemplate mongoTemplate) {
//        // Define all standard roles
//        String[] roleNames = {
//                UserRoles.SUPERADMIN.name(),
//                UserRoles.ADMIN.name(),
//                UserRoles.ACCOUNTMANAGER.name(),
//                UserRoles.TEAMLEAD.name(),
//                UserRoles.RECRUITER.name(),
//                UserRoles.ONBOARD.name(),
//                UserRoles.MODERATOR.name(),
//                UserRoles.GENERALMANAGER.name(),
//                UserRoles.PARTNER.name()
//        };
//
//
//        for (String roleName : roleNames) {
//            try {
//                // Check if role already exists
//                Role existingRole = mongoTemplate.findOne(
//                        org.springframework.data.mongodb.core.query.Query.query(
//                                org.springframework.data.mongodb.core.query.Criteria.where("role").is(roleName)
//                        ),
//                        Role.class
//                );
//
//                if (existingRole == null) {
//                    Role role = new Role().setRole(roleName);
//                    mongoTemplate.save(role);
//                    logger.debug("Created role: {}", roleName);
//                }
//            } catch (Exception e) {
//                logger.warn("Error creating role {}: {}", roleName, e.getMessage());
//            }
//        }
//
//        logger.info("Default roles creation completed");
//    }

    private void createDefaultRoles(MongoTemplate mongoTemplate) {
        // Define all standard roles with their specific IDs from the JSON file
        Map<String, String> roleIdMap = new HashMap<>();
        roleIdMap.put("ADMIN", "658472f84b18126ca69a4921");
        roleIdMap.put("RECRUITER", "658472f94b18126ca69a4922");
        roleIdMap.put("SUPERADMIN", "658472f94b18126ca69a4923");
        roleIdMap.put("ONBOARD", "658472f94b18126ca69a4924");
        roleIdMap.put("TEAMLEAD", "658472f94b18126ca69a4925");
        roleIdMap.put("MODERATOR", "658472f94b18126ca69a4926");
        roleIdMap.put("ACCOUNTMANAGER", "658472f94b18126ca69a4927");
        roleIdMap.put("GENERALMANAGER", "658df0cfdbeb2926ec0fd60e");
        roleIdMap.put("PARTNER", "66d21d0bf589f3f2b596ece8");

        for (Map.Entry<String, String> entry : roleIdMap.entrySet()) {
            String roleName = entry.getKey();
            String roleId = entry.getValue();

            try {
                // Check if role already exists by ID
                Role existingRoleById = mongoTemplate.findById(roleId, Role.class);

                if (existingRoleById == null) {
                    // Check if role exists by name
                    Role existingRoleByName = mongoTemplate.findOne(
                            org.springframework.data.mongodb.core.query.Query.query(
                                    org.springframework.data.mongodb.core.query.Criteria.where("role").is(roleName)
                            ),
                            Role.class
                    );

                    if (existingRoleByName == null) {
                        // Create new role with specific ID
                        Role role = new Role();
                        role.setId(roleId);
                        role.setRole(roleName);
                        mongoTemplate.save(role);
                        logger.debug("Created role: {} with ID: {}", roleName, roleId);
                    } else {
                        // Role exists with different ID - log warning
                        logger.warn("Role {} already exists with different ID. Expected: {}, Found: {}",
                                roleName, roleId, existingRoleByName.getId());
                    }
                } else {
                    // Role exists with correct ID - verify the name matches
                    if (!roleName.equals(existingRoleById.getRole())) {
                        logger.warn("Role ID {} exists with different name. Expected: {}, Found: {}",
                                roleId, roleName, existingRoleById.getRole());
                    }
                }
            } catch (Exception e) {
                logger.warn("Error creating role {}: {}", roleName, e.getMessage());
            }
        }

        logger.info("Default roles creation completed");
    }
    private void createDefaultAdminUser(MongoTemplate mongoTemplate, String email) {
        try {
//            String adminEmail = "admin@" + subdomain + ".com";

            // Check if admin user already exists
            User existingUser = mongoTemplate.findOne(
                    org.springframework.data.mongodb.core.query.Query.query(
                            org.springframework.data.mongodb.core.query.Criteria.where("email").is(email)
                    ),
                    User.class
            );

            if (existingUser == null) {
                // Get SUPERADMIN role
                Role superAdminRole = mongoTemplate.findOne(
                        org.springframework.data.mongodb.core.query.Query.query(
                                org.springframework.data.mongodb.core.query.Criteria.where("role").is(UserRoles.SUPERADMIN.name())
                        ),
                        Role.class
                );

                if (superAdminRole != null) {
                    Set<Role> roles = new HashSet<>();
                    roles.add(superAdminRole);

                    User adminUser = new User()
                            .setEmail(email)
                            .setPassword(bCryptPasswordEncoder.encode("Admin@123"))
                            .setFirstName("Super")
                            .setLastName("Admin")
                            .setRoles(roles)
                            .setActive(true)
                            .setDateCreated(new Date())
                            .setDateModified(new Date());

                    mongoTemplate.save(adminUser);
                    logger.info("Created default admin user: {}", email);
                }
            }
        } catch (Exception e) {
            logger.warn("Error creating default admin user: {}", e.getMessage());
        }
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

    private String maskConnectionString(String connectionString) {
        return connectionString.replaceAll("://([^:]+):([^@]+)@", "://*****:*****@");
    }

    // Helper class to hold database initialization results
    private static class DatabaseResult {
        final int collectionsCreated;
        final int indexesCreated;

        DatabaseResult(int collectionsCreated, int indexesCreated) {
            this.collectionsCreated = collectionsCreated;
            this.indexesCreated = indexesCreated;
        }
    }




    // Add these autowired dependencies
    @Autowired
    private DefaultEmailConfigService defaultEmailConfigService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    // Update the initializeTenantDatabase method
//    public ServiceResultDTOs.DatabaseSetupResult initializeTenantDatabase(String subdomain, String email,
//                                                                          String hrmsConnectionString,
//                                                                          String nodeConnectionString,
//                                                                          String jobSyncConnectionString) {
//        logger.info("Starting comprehensive database initialization for subdomain: {}", subdomain);
//
//        try {
//            // ... existing validation and tenant creation code ...
//
//            // Create tenant record first
//            Tenant newTenant = createTenantRecord(subdomain, hrmsConnectionString, nodeConnectionString, jobSyncConnectionString);
//            logger.info("Created tenant record for subdomain: {}", subdomain);
//
//            // Initialize all three databases
//            DatabaseResult hrmsResult = initializeHRMSDatabase(hrmsConnectionString, email);
//            DatabaseResult nodeResult = initializeNodeDatabase(nodeConnectionString, subdomain);
//            DatabaseResult jobSyncResult = initializeJobSyncDatabase(jobSyncConnectionString, subdomain);
//
//            // *** NEW: Create default email configuration and templates ***
//            setupDefaultEmailSystem(newTenant.getId(), email);
//
//            // Calculate totals
//            int totalCollections = hrmsResult.collectionsCreated + nodeResult.collectionsCreated + jobSyncResult.collectionsCreated;
//            int totalIndexes = hrmsResult.indexesCreated + nodeResult.indexesCreated + jobSyncResult.indexesCreated;
//
//            logger.info("Database initialization completed for subdomain: {} - Total Collections: {}, Total Indexes: {}",
//                    subdomain, totalCollections, totalIndexes);
//
//            return new ServiceResultDTOs.DatabaseSetupResult()
//                    .setSuccess(true)
//                    .setMessage(String.format("All databases initialized successfully with email system. HRMS: %d collections, Node: %d collections, JobSync: %d collections",
//                            hrmsResult.collectionsCreated, nodeResult.collectionsCreated, jobSyncResult.collectionsCreated))
//                    .setCollectionsCreated(totalCollections)
//                    .setIndexesCreated(totalIndexes);
//
//        } catch (Exception e) {
//            logger.error("Failed to initialize databases for subdomain {}: {}", subdomain, e.getMessage(), e);
//            throw e;
//        }
//    }

    // Add this new method
    private void setupDefaultEmailSystem(String tenantId, String adminEmail) {
        try {
            logger.info("Setting up default email system for tenant: {}", tenantId);

            // Set tenant context for template service
            TenantContext.setCurrentTenant(tenantId);

            // Create default email configuration
            defaultEmailConfigService.createDefaultEmailConfig(tenantId, adminEmail);

            // Create default email templates
            emailTemplateService.createDefaultTemplates(tenantId);

            logger.info("Default email system setup completed for tenant: {}", tenantId);

        } catch (Exception e) {
            logger.error("Failed to setup default email system for tenant {}: {}", tenantId, e.getMessage(), e);
            // Don't throw exception - email setup is not critical for tenant creation
        } finally {
            TenantContext.clear();
        }
    }
}

//package com.midas.consulting.service;
//
//import com.midas.consulting.config.database.MongoTemplateProvider;
//import com.midas.consulting.controller.v1.response.ServiceResultDTOs;
//import com.midas.consulting.exception.MidasCustomException;
//import com.midas.consulting.model.Tenant;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoDatabase;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.index.Index;
//import org.springframework.stereotype.Service;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Service
//public class DatabaseSetupService {
//
//    private static final Logger logger = LoggerFactory.getLogger(DatabaseSetupService.class);
//
//    @Autowired
//    private TenantService tenantService;
//
//    @Autowired
//    private MongoTemplateProvider mongoTemplateProvider;
//
//    // Standard collections for all tenants
//    private static final List<String> STANDARD_COLLECTIONS = Arrays.asList(
//            "user", "role", "candidateMidas", "activities", "notes", "loginStats",
//            "employee", "project", "client", "vms", "facility", "organisation",
//            "timeSheet", "employeeDocs", "submissionDoc", "changeLog", "team",
//            "division", "routes", "communicationLog", "candidateJobTags",
//            "interdefaults", "locations", "storage_config"
//    );
//
//    public ServiceResultDTOs.DatabaseSetupResult initializeTenantDatabase(String subdomain, String connectionString) {
//        logger.info("Starting database initialization for subdomain: {}", subdomain);
//
//        try {
//            // Check if tenant already exists
//            Tenant existingTenant = tenantService.getTenantBySubdomain(subdomain);
//            if (existingTenant != null) {
//                throw new MidasCustomException.DuplicateEntityException(
//                        "Database already exists for subdomain: " + subdomain);
//            }
//
//            // Test connection string
//            if (!testConnection(connectionString)) {
//                throw new IllegalArgumentException("Cannot connect to database with provided connection string");
//            }
//
//            // Create tenant record
//            Tenant newTenant = new Tenant()
//                    .setSubdomain(subdomain)
//                    .setConnectionString(connectionString)
//                    .setTenantName(subdomain);
//
//            Tenant savedTenant = tenantService.createTenant(newTenant);
//            logger.info("Created tenant record for subdomain: {}", subdomain);
//
//            // Initialize database collections and indexes
//            int collectionsCreated = createStandardCollections(connectionString);
//            int indexesCreated = createStandardIndexes(connectionString);
//
//            // Create default admin user and roles
//            createDefaultRolesAndUser(connectionString, subdomain);
//
//            logger.info("Database initialization completed for subdomain: {} - Collections: {}, Indexes: {}",
//                    subdomain, collectionsCreated, indexesCreated);
//
//            return new ServiceResultDTOs.DatabaseSetupResult()
//                    .setSuccess(true)
//                    .setMessage("Database initialized successfully")
//                    .setCollectionsCreated(collectionsCreated)
//                    .setIndexesCreated(indexesCreated);
//
//        } catch (Exception e) {
//            logger.error("Failed to initialize database for subdomain {}: {}", subdomain, e.getMessage(), e);
//            throw e;
//        }
//    }
//
//    private boolean testConnection(String connectionString) {
//        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//            // Test connection by listing databases
//            mongoClient.listDatabaseNames().first();
//            logger.info("Database connection test successful");
//            return true;
//        } catch (Exception e) {
//            logger.error("Database connection test failed: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    private int createStandardCollections(String connectionString) {
//        int collectionsCreated = 0;
//
//        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//            String databaseName = extractDatabaseName(connectionString);
//            MongoDatabase database = mongoClient.getDatabase(databaseName);
//
//            for (String collectionName : STANDARD_COLLECTIONS) {
//                try {
//                    database.createCollection(collectionName);
//                    collectionsCreated++;
//                    logger.debug("Created collection: {}", collectionName);
//                } catch (Exception e) {
//                    logger.warn("Collection {} may already exist: {}", collectionName, e.getMessage());
//                }
//            }
//        }
//
//        return collectionsCreated;
//    }
//
//    private int createStandardIndexes(String connectionString) {
//        int indexesCreated = 0;
//
//        try {
//            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);
//
//            // Create indexes for user collection
//            mongoTemplate.indexOps("user").ensureIndex(
//                    new Index().on("email", Sort.Direction.ASC).unique());
//            indexesCreated++;
//
//            // Create indexes for candidateMidas collection
//            mongoTemplate.indexOps("candidateMidas").ensureIndex(
//                    new Index().on("email", Sort.Direction.ASC).unique());
//            mongoTemplate.indexOps("candidateMidas").ensureIndex(
//                    new Index().on("phone", Sort.Direction.ASC));
//            mongoTemplate.indexOps("candidateMidas").ensureIndex(
//                    new Index().on("skills", Sort.Direction.ASC));
//            indexesCreated += 3;
//
//            // Create indexes for activities collection
//            mongoTemplate.indexOps("activities").ensureIndex(
//                    new Index().on("candidateID", Sort.Direction.ASC));
//            mongoTemplate.indexOps("activities").ensureIndex(
//                    new Index().on("dateCreated", Sort.Direction.DESC));
//            indexesCreated += 2;
//
//            // Create indexes for employee collection
//            mongoTemplate.indexOps("employee").ensureIndex(
//                    new Index().on("email", Sort.Direction.ASC).unique());
//            indexesCreated++;
//
//            // Create indexes for project collection
//            mongoTemplate.indexOps("project").ensureIndex(
//                    new Index().on("name", Sort.Direction.ASC).unique());
//            indexesCreated++;
//
//            logger.info("Created {} indexes successfully", indexesCreated);
//
//        } catch (Exception e) {
//            logger.error("Error creating indexes: {}", e.getMessage(), e);
//        }
//
//        return indexesCreated;
//    }
//
//    private void createDefaultRolesAndUser(String connectionString, String subdomain) {
//        try {
//            MongoTemplate mongoTemplate = createTemporaryMongoTemplate(connectionString);
//
//            // Create default roles
//            createDefaultRoles(mongoTemplate);
//
//            // Create default admin user
//            createDefaultAdminUser(mongoTemplate, subdomain);
//
//            logger.info("Created default roles and admin user for subdomain: {}", subdomain);
//
//        } catch (Exception e) {
//            logger.error("Error creating default roles and user: {}", e.getMessage(), e);
//        }
//    }
//
//    private void createDefaultRoles(MongoTemplate mongoTemplate) {
//        // Implementation would create default roles like ADMIN, RECRUITER, etc.
//        // This is a placeholder - you'd implement based on your Role model
//        logger.info("Default roles creation completed");
//    }
//
//    private void createDefaultAdminUser(MongoTemplate mongoTemplate, String subdomain) {
//        // Implementation would create a default admin user
//        // This is a placeholder - you'd implement based on your User model
//        logger.info("Default admin user creation completed for: {}", subdomain);
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