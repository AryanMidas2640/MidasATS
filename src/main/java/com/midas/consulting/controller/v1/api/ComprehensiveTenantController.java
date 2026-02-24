
package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.CreateTenantRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.controller.v1.response.ServiceResultDTOs;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.security.api.JwtUtils;
import com.midas.consulting.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/tenants")
@Api(value = "tenant-management", description = "Comprehensive tenant management operations")
public class ComprehensiveTenantController {

    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveTenantController.class);

    @Autowired
    private TenantService tenantService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private JwtUtils jwtUtils;


    @Autowired private DatabaseSetupService databaseSetupService;
    @Autowired private ServiceConfigurationService serviceConfigurationService;
    @Autowired private ComprehensiveEmailSetupService comprehensiveEmailSetupService;
    @Autowired private ComprehensiveStorageSetupService comprehensiveStorageSetupService;
    @Autowired private EnhancedTenantEmailService tenantEmailService;



    // ==================== RETRIEVAL OPERATIONS ====================

    @ApiOperation(value = "Retrieve all tenants (SuperAdmin only)",
            authorizations = {@Authorization(value = "apiKey")})
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Response<List<Tenant>>> getAllTenants() {
        try {
            List<Tenant> tenants = tenantService.getAllTenants();
            Response<List<Tenant>> response = Response.ok();
            response.setPayload(tenants);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving all tenants: {}", e.getMessage(), e);
            Response<List<Tenant>> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to retrieve tenants", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ApiOperation(value = "Get tenant by ID",
            authorizations = {@Authorization(value = "apiKey")})
    @GetMapping("/{id}")
    public ResponseEntity<Response<Tenant>> getTenantById(
            @ApiParam(value = "Tenant ID", required = true)
            @PathVariable @NotBlank String id) {
        try {
            Tenant tenant = tenantService.getTenantById(id);
            Response<Tenant> response = Response.ok();
            response.setPayload(tenant);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving tenant {}: {}", id, e.getMessage(), e);
            Response<Tenant> errorResponse = Response.notFound();
            errorResponse.addErrorMsgToResponse("Tenant not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @ApiOperation(value = "Get tenant by subdomain",
            authorizations = {@Authorization(value = "apiKey")})
    @GetMapping("/subdomain/{subDomain}")
    public ResponseEntity<Response<Tenant>> getTenantBySubdomain(
            @ApiParam(value = "Subdomain", required = true)
            @PathVariable @NotBlank @Pattern(regexp = "^[a-z0-9-]+$", message = "Invalid subdomain format")
            String subDomain) {
        try {
            Tenant tenant = tenantService.getTenantBySubdomain(subDomain);
            if (tenant == null) {
                Response<Tenant> response = Response.notFound();
                response.setErrors("Tenant was not found: " + subDomain);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            tenant.setConnectionStringNode(bCryptPasswordEncoder.encode(tenant.getConnectionStringNode()));
            tenant.setConnectionStringJobSync(bCryptPasswordEncoder.encode(tenant.getConnectionStringJobSync()));
            tenant.setConnectionString(bCryptPasswordEncoder.encode(tenant.getConnectionString()));
            Response<Tenant> response = Response.ok();
            response.setPayload(tenant);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving tenant by subdomain {}: {}", subDomain, e.getMessage(), e);
            Response<Tenant> errorResponse = Response.notFound();
            errorResponse.addErrorMsgToResponse("Tenant not found for subdomain", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // ==================== CONTEXT-AWARE RETRIEVAL ====================

    @ApiOperation(value = "Get tenant configuration by context",
            authorizations = {@Authorization(value = "apiKey")})
    @GetMapping("/context/{callType}")
    public ResponseEntity<Response<Tenant>> getTenantByContext(
            @ApiParam(value = "Call type: hrms, checklist, or jobs", required = true)
            @PathVariable @Pattern(regexp = "^(hrms|checklist|jobs)$", message = "Invalid call type")
            String callType,
            @RequestHeader(value = "X-Tenant", required = true) String tenantId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = true) String authToken) {

        try {
            // Validate inputs
            validateContextRequest(tenantId, callType, authToken);

            // Validate JWT token
            if (!jwtUtils.validateJwtToken(authToken.replace("Bearer ", ""))) {
                Response<Tenant> errorResponse = Response.unauthorized();
                errorResponse.addErrorMsgToResponse("Invalid token", new RuntimeException());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            Tenant tenant = tenantService.getTenantById(tenantId);
            if (tenant == null) {
                Response<Tenant> errorResponse = Response.notFound();
                errorResponse.addErrorMsgToResponse("Tenant not found", new RuntimeException());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Filter sensitive connection strings based on call type
            Tenant filteredTenant = filterTenantByCallType(tenant, callType);

            Response<Tenant> response = Response.ok();
            response.setPayload(filteredTenant);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            Response<Tenant> errorResponse = Response.badRequest();
            errorResponse.addErrorMsgToResponse("Invalid parameters", e);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Error retrieving tenant context: {}", e.getMessage(), e);
            Response<Tenant> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to retrieve tenant context", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== COMPREHENSIVE TENANT CREATION ====================

    @ApiOperation(value = "Create a new tenant with comprehensive database setup",
            authorizations = {@Authorization(value = "apiKey")})
    @PostMapping("/comprehensive")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> createComprehensiveTenant(
            @ApiParam(value = "Comprehensive tenant configuration", required = true)
            @Valid @RequestBody CreateTenantRequest tenantRequest) {

        logger.info("Starting comprehensive tenant creation for subdomain: {}", tenantRequest.getSubdomain());

        try {
            // Validate subdomain availability
            if (tenantService.getTenantBySubdomain(tenantRequest.getSubdomain()) != null) {
                throw new MidasCustomException.DuplicateEntityException(
                        "Subdomain already exists: " + tenantRequest.getSubdomain());
            }

            // Validate subdomain format
            if (!tenantService.isValidSubdomain(tenantRequest.getSubdomain())) {
                throw new IllegalArgumentException("Invalid subdomain format: " + tenantRequest.getSubdomain());
            }

            // Initialize all three databases with their respective collections
            ServiceResultDTOs.DatabaseSetupResult setupResult = databaseSetupService.initializeTenantDatabase(
                    tenantRequest.getSubdomain(),
                    tenantRequest.getEmail(),
                    tenantRequest.getConnectionString(),
                    tenantRequest.getConnectionStringNode(),
                    tenantRequest.getConnectionStringJobSync());

            if (!setupResult.isSuccess()) {
                Response<Map<String, Object>> errorResponse = Response.exception();
                errorResponse.addErrorMsgToResponse("Database setup failed",
                        new RuntimeException(setupResult.getMessage()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }

            // Configure additional services
            ServiceResultDTOs.ServiceConfigurationResult configResult = serviceConfigurationService.configureServices(
                    tenantRequest.getSubdomain(),
                    tenantRequest.getChecklistUrl(),
                    tenantRequest.getConnectionStringJobSync(),
                    tenantRequest.getWebUrl(),
                    tenantRequest.getLogo(),
                    tenantRequest.getEmail());

            // Prepare comprehensive response
            Map<String, Object> result = new HashMap<>();
            result.put("tenantId", configResult.getTenantId());
            result.put("subdomain", tenantRequest.getSubdomain());
            Map<String, Object> databaseSetup = new HashMap<>();
            databaseSetup.put("success", setupResult.isSuccess());
            databaseSetup.put("message", setupResult.getMessage());
            databaseSetup.put("collectionsCreated", setupResult.getCollectionsCreated());
            databaseSetup.put("indexesCreated", setupResult.getIndexesCreated());
            result.put("databaseSetup", databaseSetup);

            Map<String, Object> serviceConfiguration = new HashMap<>();
            serviceConfiguration.put("success", configResult.isSuccess());
            serviceConfiguration.put("message", configResult.getMessage());
            serviceConfiguration.put("servicesConfigured", configResult.getServicesConfigured());
            result.put("serviceConfiguration", serviceConfiguration);

            Map<String, String> databases = new HashMap<>();
            databases.put("hrms", "Collections created for user management, candidates, and core HRMS functionality");
            databases.put("checklist", "Collections created for checklist templates, submissions, and document management");
            databases.put("jobSync", "Collections created for job feeds, synchronization, and job management");
            result.put("databases", databases);
            sendWelcomeEmail(tenantRequest.getTenantName(), tenantRequest.getEmail(), tenantRequest.getSubdomain());
//            sendWelcomeEmail(tenant.getTenantName(), request.getAdminEmail(), request.getSubdomain());

            Response<Map<String, Object>> response = Response.ok();
            response.setPayload(result);

            logger.info("Comprehensive tenant creation completed successfully for subdomain: {}", tenantRequest.getSubdomain());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (MidasCustomException.DuplicateEntityException e) {
            logger.warn("Duplicate tenant creation attempt: {}", e.getMessage());
            Response<Map<String, Object>> errorResponse = Response.duplicateEntity();
            errorResponse.addErrorMsgToResponse("Tenant already exists", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid tenant creation request: {}", e.getMessage());
            Response<Map<String, Object>> errorResponse = Response.badRequest();
            errorResponse.addErrorMsgToResponse("Invalid request parameters", e);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Error creating comprehensive tenant: {}", e.getMessage(), e);
            Response<Map<String, Object>> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to create tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }






    //  DHEERAJ
    /**
     * Comprehensive tenant onboarding with database, email, and storage setup
     */
    @PostMapping("/comprehensivev2")
    @ApiOperation(value = "Comprehensive tenant onboarding with all services",
            authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Response> comprehensiveOnboarding(
            @Valid @RequestBody ComprehensiveTenantRequest request) {

        logger.info("=== Starting comprehensive onboarding for tenant: {} ===", request.getSubdomain());

        try {
            // 1. Validate subdomain availability
            validateTenantRequest(request);

            // 2. Initialize tenant database and default services
            ServiceResultDTOs.DatabaseSetupResult dbResult = initializeTenantDatabase(request);

            // 3. Configure basic services (routes, permissions, etc.)
            ServiceResultDTOs.ServiceConfigurationResult serviceResult = configureBasicServices(request);

            // 4. Get the created tenant
            Tenant tenant = tenantService.getTenantBySubdomain(request.getSubdomain());
            if (tenant == null) {
                throw new RuntimeException("Tenant creation failed - tenant not found after setup");
            }

            // 5. Setup comprehensive email system
            ComprehensiveEmailSetupResult emailResult = setupEmailSystem(tenant.getId(), request);

            // 6. Setup storage system if provided
            ComprehensiveStorageSetupResult storageResult = setupStorageSystem(tenant.getId(), request);

            // 7. Send welcome email to admin
            sendWelcomeEmail(tenant.getTenantName(), request.getAdminEmail(), request.getSubdomain());

            // 8. Create comprehensive result
            Map<String, Object> result = buildComprehensiveResult(
                    tenant, request, dbResult, serviceResult, emailResult, storageResult);

            logger.info("=== Comprehensive onboarding completed successfully for tenant: {} ===",
                    request.getSubdomain());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.ok().setPayload(result));

        } catch (Exception e) {
            logger.error("=== Comprehensive onboarding FAILED for {}: {} ===",
                    request.getSubdomain(), e.getMessage(), e);

            // Cleanup on failure
            cleanupFailedOnboarding(request.getSubdomain());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.exception().addErrorMsgToResponse("Comprehensive onboarding failed", e));
        }
    }

    /**
     * Update email configuration (switches between providers)
     */
    @PutMapping("/{tenantId}/email-config")
    @ApiOperation(value = "Update tenant email configuration",
            authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Response> updateEmailConfiguration(
            @PathVariable String tenantId,
            @Valid @RequestBody ComprehensiveEmailConfig emailConfig) {

        try {
            logger.info("Updating email configuration for tenant: {} to provider: {}",
                    tenantId, emailConfig.getProvider());

            // Deactivate existing email configurations
            comprehensiveEmailSetupService.deactivateAllEmailConfigs(tenantId);

            // Setup new email configuration
            ComprehensiveEmailSetupResult result = comprehensiveEmailSetupService
                    .setupComprehensiveEmailSystem(tenantId, emailConfig, emailConfig.getFromEmail());

            if (!result.isSuccess()) {
                throw new RuntimeException("Email configuration update failed: " + result.getMessage());
            }

            logger.info("Email configuration updated successfully for tenant: {}", tenantId);

            return ResponseEntity.ok(Response.ok().setPayload(result));

        } catch (Exception e) {
            logger.error("Failed to update email configuration for tenant {}: {}",
                    tenantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.exception().addErrorMsgToResponse("Email configuration update failed", e));
        }
    }

    /**
     * Update storage configuration (switches between providers)
     */
    @PutMapping("/{tenantId}/storage-config")
    @ApiOperation(value = "Update tenant storage configuration",
            authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Response> updateStorageConfiguration(
            @PathVariable String tenantId,
            @Valid @RequestBody ComprehensiveStorageConfig storageConfig) {

        try {
            logger.info("Updating storage configuration for tenant: {} to provider: {}",
                    tenantId, storageConfig.getProvider());

            // Deactivate existing storage configurations
            comprehensiveStorageSetupService.deactivateAllStorageConfigs(tenantId);

            // Setup new storage configuration
            ComprehensiveStorageSetupResult result = comprehensiveStorageSetupService
                    .setupComprehensiveStorageSystem(tenantId, storageConfig);

            if (!result.isSuccess()) {
                throw new RuntimeException("Storage configuration update failed: " + result.getMessage());
            }

            logger.info("Storage configuration updated successfully for tenant: {}", tenantId);

            return ResponseEntity.ok(Response.ok().setPayload(result));

        } catch (Exception e) {
            logger.error("Failed to update storage configuration for tenant {}: {}",
                    tenantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.exception().addErrorMsgToResponse("Storage configuration update failed", e));
        }
    }

    /**
     * Get comprehensive tenant status
     */
    @GetMapping("/{tenantId}/comprehensive-status")
    @ApiOperation(value = "Get comprehensive tenant configuration status",
            authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Response> getComprehensiveStatus(@PathVariable String tenantId) {

        try {
            Map<String, Object> status = new HashMap<>();

            // Get tenant basic info
            Tenant tenant = tenantService.getTenantById(tenantId);
            if (tenant == null) {
                throw new MidasCustomException.EntityNotFoundException("Tenant not found: " + tenantId);
            }

            // Get email configuration status
            ComprehensiveEmailStatusResult emailStatus = comprehensiveEmailSetupService
                    .getComprehensiveEmailStatus(tenantId);

            // Get storage configuration status
            ComprehensiveStorageStatusResult storageStatus = comprehensiveStorageSetupService
                    .getComprehensiveStorageStatus(tenantId);

            status.put("tenantInfo", createTenantInfo(tenant));
            status.put("emailConfiguration", emailStatus);
            status.put("storageConfiguration", storageStatus);
            status.put("lastUpdated", new Date());

            return ResponseEntity.ok(Response.ok().setPayload(status));

        } catch (Exception e) {
            logger.error("Failed to get comprehensive status for tenant {}: {}",
                    tenantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.exception().addErrorMsgToResponse("Failed to get tenant status", e));
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private void validateTenantRequest(ComprehensiveTenantRequest request) {
        logger.info("Validating tenant request for subdomain: {}", request.getSubdomain());

        if (!tenantService.isSubdomainAvailable(request.getSubdomain())) {
            throw new MidasCustomException.DuplicateEntityException(
                    "Subdomain already exists: " + request.getSubdomain());
        }

        if (!tenantService.isValidSubdomain(request.getSubdomain())) {
            throw new IllegalArgumentException(
                    "Invalid subdomain format: " + request.getSubdomain());
        }

        // Validate email configuration
        if (request.getEmailConfig() == null) {
            throw new IllegalArgumentException("Email configuration is required");
        }

        logger.info("Tenant request validation completed successfully");
    }

    private ServiceResultDTOs.DatabaseSetupResult initializeTenantDatabase(ComprehensiveTenantRequest request) {
        logger.info("Initializing database for tenant: {}", request.getSubdomain());

        ServiceResultDTOs.DatabaseSetupResult dbResult = databaseSetupService
                .initializeTenantDatabase(
                        request.getSubdomain(),
                        request.getAdminEmail(),
                        request.getConnectionString(),
                        request.getConnectionStringNode(),
                        request.getConnectionStringJobSync()
                );

        if (!dbResult.isSuccess()) {
            throw new RuntimeException("Database setup failed: " + dbResult.getMessage());
        }

        logger.info("Database initialization completed - Collections: {}, Indexes: {}",
                dbResult.getCollectionsCreated(), dbResult.getIndexesCreated());
        return dbResult;
    }

    private ServiceResultDTOs.ServiceConfigurationResult configureBasicServices(ComprehensiveTenantRequest request) {
        logger.info("Configuring basic services for tenant: {}", request.getSubdomain());

        ServiceResultDTOs.ServiceConfigurationResult serviceResult = serviceConfigurationService
                .configureServices(
                        request.getSubdomain(),
                        request.getChecklistUrl(),
                        request.getConnectionStringJobSync(),
                        request.getWebUrl(),
                        request.getLogo(),
                        request.getAdminEmail()
                );

        if (!serviceResult.isSuccess()) {
            logger.warn("Service configuration completed with warnings: {}", serviceResult.getMessage());
        } else {
            logger.info("Service configuration completed successfully");
        }

        return serviceResult;
    }

    private ComprehensiveEmailSetupResult setupEmailSystem(String tenantId, ComprehensiveTenantRequest request) {
        logger.info("Setting up email system for tenant: {}", tenantId);

        ComprehensiveEmailSetupResult emailResult = comprehensiveEmailSetupService
                .setupComprehensiveEmailSystem(tenantId, request.getEmailConfig(), request.getAdminEmail());

        if (!emailResult.isSuccess()) {
            logger.warn("Email setup completed with issues: {}", emailResult.getMessage());
        } else {
            logger.info("Email system setup completed successfully - Provider: {}",
                    emailResult.getProvider());
        }

        return emailResult;
    }

    private ComprehensiveStorageSetupResult setupStorageSystem(String tenantId, ComprehensiveTenantRequest request) {
        logger.info("Setting up storage system for tenant: {}", tenantId);

        ComprehensiveStorageSetupResult storageResult = null;
        if (request.getStorageConfig() != null) {
            storageResult = comprehensiveStorageSetupService
                    .setupComprehensiveStorageSystem(tenantId, request.getStorageConfig());

            if (storageResult.isSuccess()) {
                logger.info("Storage system setup completed successfully - Provider: {}",
                        storageResult.getProvider());
            } else {
                logger.warn("Storage setup completed with issues: {}", storageResult.getMessage());
            }
        } else {
            logger.info("No storage configuration provided - skipping storage setup");
            storageResult = new ComprehensiveStorageSetupResult()
                    .setSuccess(false)
                    .setMessage("No storage configuration provided");
        }

        return storageResult;
    }

    private void sendWelcomeEmail(String tenantId, String adminEmail, String subdomain) {
        try {
            logger.info("Sending welcome email to admin: {}", adminEmail);

            tenantId="670a48b168b0640a262870c4";
            TenantContext.setCurrentTenant(tenantId);

            Map<String, Object> variables = new HashMap<>();
            variables.put("adminEmail", adminEmail);
            variables.put("subdomain", subdomain);
            variables.put("loginUrl", "https://" + subdomain + ".theartemis.ai/auth/login");
            variables.put("tempPassword", "Admin@123");

            boolean sent = tenantEmailService.sendTemplatedEmail(
                    tenantId, adminEmail,
                    "Welcome to Your New ATS System",
                    "welcome-user",
                    variables
            );

            if (sent) {
                logger.info("Welcome email sent successfully");
            } else {
                logger.warn("Welcome email sending failed");
            }

        } catch (Exception e) {
            logger.warn("Failed to send welcome email: {}", e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    private Map<String, Object> buildComprehensiveResult(
            Tenant tenant, ComprehensiveTenantRequest request,
            ServiceResultDTOs.DatabaseSetupResult dbResult,
            ServiceResultDTOs.ServiceConfigurationResult serviceResult,
            ComprehensiveEmailSetupResult emailResult,
            ComprehensiveStorageSetupResult storageResult) {

        Map<String, Object> result = new HashMap<>();

        // Basic tenant info
        result.put("tenantId", tenant.getId());
        result.put("subdomain", request.getSubdomain());
        result.put("tenantName", request.getTenantName());
        result.put("adminEmail", request.getAdminEmail());

        // URLs and access info
        result.put("adminLoginUrl", request.getWebUrl() + "/login");
        result.put("webUrl", request.getWebUrl());
        result.put("checklistUrl", request.getChecklistUrl());
        result.put("defaultPassword", "Admin@123");
//        Map<String, Object> result = new HashMap<>();

// Setup results
        Map<String, Object> dbSetup = new HashMap<>();
        dbSetup.put("success", dbResult.isSuccess());
        dbSetup.put("message", dbResult.getMessage());
        dbSetup.put("collectionsCreated", dbResult.getCollectionsCreated());
        dbSetup.put("indexesCreated", dbResult.getIndexesCreated());
        result.put("databaseSetup", dbSetup);

        Map<String, Object> serviceSetup = new HashMap<>();
        serviceSetup.put("success", serviceResult.isSuccess());
        serviceSetup.put("message", serviceResult.getMessage());
        serviceSetup.put("servicesConfigured", serviceResult.getServicesConfigured());
        result.put("serviceConfiguration", serviceSetup);

        Map<String, Object> emailSetup = new HashMap<>();
        emailSetup.put("success", emailResult.isSuccess());
        emailSetup.put("provider", emailResult.getProvider());
        emailSetup.put("fromEmail", emailResult.getFromEmail());
        emailSetup.put("testPassed", emailResult.isTestPassed());
        emailSetup.put("message", emailResult.getMessage());
        result.put("emailSetup", emailSetup);

        if (storageResult != null) {
            Map<String, Object> storageSetup = new HashMap<>();
            storageSetup.put("success", storageResult.isSuccess());
            storageSetup.put("provider", storageResult.getProvider() != null ? storageResult.getProvider() : "none");
            storageSetup.put("message", storageResult.getMessage());
            result.put("storageSetup", storageSetup);
        }

//
//        // Setup results
//        result.put("databaseSetup", Map.of(
//                "success", dbResult.isSuccess(),
//                "message", dbResult.getMessage(),
//                "collectionsCreated", dbResult.getCollectionsCreated(),
//                "indexesCreated", dbResult.getIndexesCreated()
//        ));
//
//        result.put("serviceConfiguration", Map.of(
//                "success", serviceResult.isSuccess(),
//                "message", serviceResult.getMessage(),
//                "servicesConfigured", serviceResult.getServicesConfigured()
//        ));
//
//        result.put("emailSetup", Map.of(
//                "success", emailResult.isSuccess(),
//                "provider", emailResult.getProvider(),
//                "fromEmail", emailResult.getFromEmail(),
//                "testPassed", emailResult.isTestPassed(),
//                "message", emailResult.getMessage()
//        ));
//
//        if (storageResult != null) {
//            result.put("storageSetup", Map.of(
//                    "success", storageResult.isSuccess(),
//                    "provider", storageResult.getProvider() != null ? storageResult.getProvider() : "none",
//                    "message", storageResult.getMessage()
//            ));
//        }

        // Overall status
        boolean allSuccess = dbResult.isSuccess() && serviceResult.isSuccess() && emailResult.isSuccess();
        result.put("setupComplete", allSuccess);
        result.put("setupDate", new Date());

        return result;
    }

    private void cleanupFailedOnboarding(String subdomain) {
        try {
            logger.info("Cleaning up failed onboarding for subdomain: {}", subdomain);

            Tenant existingTenant = tenantService.getTenantBySubdomain(subdomain);
            if (existingTenant != null) {
                tenantService.deleteTenant(existingTenant.getId());
                logger.info("Cleaned up tenant record for: {}", subdomain);
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup after onboarding failure: {}", e.getMessage());
        }
    }

    private Map<String, Object> createTenantInfo(Tenant tenant) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", tenant.getId());
        info.put("subdomain", tenant.getSubdomain());
        info.put("tenantName", tenant.getTenantName());
        info.put("webUrl", tenant.getWebUrl());
        info.put("logo", tenant.getLogo());
        info.put("email", tenant.getEmail());
        return info;
    }

    // ===== REQUEST/RESPONSE DTOs =====

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ComprehensiveTenantRequest {
        @NotBlank(message = "Subdomain is required")
        private String subdomain;

        @NotBlank(message = "Tenant name is required")
        private String tenantName;

        @NotBlank(message = "Admin email is required")
        @Email(message = "Valid admin email is required")
        private String adminEmail;

        @NotBlank(message = "HRMS connection string is required")
        private String connectionString;

        @NotBlank(message = "Node connection string is required")
        private String connectionStringNode;

        @NotBlank(message = "Job sync connection string is required")
        private String connectionStringJobSync;

        private String checklistUrl;
        private String webUrl;
        private String logo;

        @NotNull(message = "Email configuration is required")
        @Valid
        private ComprehensiveEmailConfig emailConfig;

        @Valid
        private ComprehensiveStorageConfig storageConfig;
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ComprehensiveEmailConfig {
        @NotNull(message = "Email provider is required")
        private EmailProvider provider = EmailProvider.OUTLOOK;

        @NotBlank(message = "From email is required")
        @Email(message = "Valid from email is required")
        private String fromEmail;

        private String fromName = "ATS System";
        private String replyTo;

        // Outlook/Office365 specific (required for OUTLOOK provider)
        private String clientId;
        private String clientSecret;
        private String tenantId;

        // Gmail specific (required for GMAIL provider)
        private String gmailClientId;
        private String gmailClientSecret;
        private String credentialsPath;

        // SMTP fallback (required for SMTP provider)
        private String smtpHost = "smtp-mail.outlook.com";
        private Integer smtpPort = 587;
        private String smtpUsername;
        private String smtpPassword;
        private Boolean enableTls = true;
        private Boolean enableSsl = false;
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ComprehensiveStorageConfig {
        @NotNull(message = "Storage provider is required")
        private StorageProvider provider;

        private Boolean setPrimary = true;
        private Boolean enableBackup = false;

        // OneDrive specific (required for ONEDRIVE provider)
        private String oneDriveClientId;
        private String oneDriveClientSecret;
        private String oneDriveTenantId;
        private String driveId;

        // Google Drive specific (required for GOOGLE_DRIVE provider)
        private String googleServiceAccountKeyPath;
        private String googleCredentialsFilePath;
        private String googleFolderId;

        // AWS S3 specific (required for AWS_S3 provider)
        private String awsAccessKey;
        private String awsSecretKey;
        private String awsRegion;
        private String awsBucketName;
    }

    public enum EmailProvider {
        OUTLOOK, GMAIL, SMTP
    }

    public enum StorageProvider {
        ONEDRIVE, GOOGLE_DRIVE, AWS_S3
    }

    // ===== RESULT DTOs =====

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ComprehensiveEmailSetupResult {
        private boolean success;
        private String message;
        private String configId;
        private String provider;
        private String fromEmail;
        private boolean testPassed;
        private Date setupDate = new Date();
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ComprehensiveStorageSetupResult {
        private boolean success;
        private String message;
        private String configId;
        private String provider;
        private Date setupDate = new Date();
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ComprehensiveEmailStatusResult {
        private boolean configured;
        private String activeProvider;
        private String activeFromEmail;
        private boolean testPassed;
        private Date lastTested;
        private String configId;
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class ComprehensiveStorageStatusResult {
        private boolean configured;
        private String activeProvider;
        private boolean backupEnabled;
        private Date lastTested;
        private String configId;
    }
    //Dheeraj End



    // ==================== LEGACY TENANT CREATION ====================

    @ApiOperation(value = "Create a new tenant (legacy method)",
            authorizations = {@Authorization(value = "apiKey")})
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Response<Tenant>> createTenant(
            @ApiParam(value = "Basic tenant configuration", required = true)
            @Valid @RequestBody Tenant tenant) {
        try {
            Tenant createdTenant = tenantService.createTenant(tenant);
            Response<Tenant> response = Response.ok();
            response.setPayload(createdTenant);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            logger.error("Error creating tenant: {}", e.getMessage(), e);
            Response<Tenant> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to create tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== TENANT MANAGEMENT ====================

    @ApiOperation(value = "Update an existing tenant",
            authorizations = {@Authorization(value = "apiKey")})
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Response<Tenant>> updateTenant(
            @ApiParam(value = "Tenant ID", required = true)
            @PathVariable @NotBlank String id,
            @ApiParam(value = "Updated tenant data", required = true)
            @Valid @RequestBody Tenant tenant) {
        try {
            Tenant updatedTenant = tenantService.updateTenant(id, tenant);
            if (updatedTenant != null) {
                Response<Tenant> response = Response.ok();
                response.setPayload(updatedTenant);
                return ResponseEntity.ok(response);
            } else {
                Response<Tenant> errorResponse = Response.notFound();
                errorResponse.addErrorMsgToResponse("Tenant not found", new RuntimeException());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            logger.error("Error updating tenant {}: {}", id, e.getMessage(), e);
            Response<Tenant> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to update tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ApiOperation(value = "Delete a tenant by ID",
            authorizations = {@Authorization(value = "apiKey")})
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Response<String>> deleteTenant(
            @ApiParam(value = "Tenant ID", required = true)
            @PathVariable @NotBlank String id) {
        try {
            tenantService.deleteTenant(id);
            Response<String> response = Response.ok();
            response.setPayload("Tenant deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting tenant {}: {}", id, e.getMessage(), e);
            Response<String> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to delete tenant", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== VALIDATION ENDPOINTS ====================

    @ApiOperation(value = "Check subdomain availability",
            authorizations = {@Authorization(value = "apiKey")})
    @GetMapping("/validate/subdomain/{subdomain}")
    public ResponseEntity<Response<Map<String, Object>>> validateSubdomain(
            @ApiParam(value = "Subdomain to validate", required = true)
            @PathVariable @NotBlank String subdomain) {
        try {
            boolean isValid = tenantService.isValidSubdomain(subdomain);
            boolean isAvailable = tenantService.isSubdomainAvailable(subdomain);

            Map<String, Object> result = new HashMap<>();
            result.put("subdomain", subdomain);
            result.put("isValid", isValid);
            result.put("isAvailable", isAvailable);
            result.put("canUse", isValid && isAvailable);

            if (!isValid) {
                result.put("validationMessage", "Subdomain format is invalid. Must be 3-30 characters, lowercase letters, numbers, and hyphens (not at start/end)");
            } else if (!isAvailable) {
                result.put("validationMessage", "Subdomain is already taken");
            } else {
                result.put("validationMessage", "Subdomain is available");
            }

            Response<Map<String, Object>> response = Response.ok();
            response.setPayload(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error validating subdomain {}: {}", subdomain, e.getMessage(), e);
            Response<Map<String, Object>> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to validate subdomain", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ApiOperation(value = "Test database connection",
            authorizations = {@Authorization(value = "apiKey")})
    @PostMapping("/validate/connection")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Response<Map<String, Object>>> testDatabaseConnection(
            @ApiParam(value = "Connection string to test", required = true)
            @RequestBody Map<String, String> connectionRequest) {
        try {
            String connectionString = connectionRequest.get("connectionString");
            if (StringUtils.isEmpty(connectionString)) {
                throw new IllegalArgumentException("Connection string is required");
            }

            // Test the connection using the database setup service
            boolean isValid = testConnection(connectionString);

            Map<String, Object> result = new HashMap<>();
            result.put("connectionString", maskConnectionString(connectionString));
            result.put("isValid", isValid);
            result.put("message", isValid ? "Connection successful" : "Connection failed");

            Response<Map<String, Object>> response = Response.ok();
            response.setPayload(result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error testing database connection: {}", e.getMessage(), e);
            Response<Map<String, Object>> errorResponse = Response.exception();
            errorResponse.addErrorMsgToResponse("Failed to test connection", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ==================== UTILITY METHODS ====================

    private void validateContextRequest(String tenantId, String callType, String authToken) {
        if (StringUtils.isEmpty(tenantId) || StringUtils.isEmpty(callType) || StringUtils.isEmpty(authToken)) {
            throw new IllegalArgumentException("Required parameters are missing: tenantId, callType, or authToken");
        }
    }

    private Tenant filterTenantByCallType(Tenant tenant, String callType) {
        // Create a copy to avoid modifying the original
        Tenant filtered = new Tenant()
                .setId(tenant.getId())
                .setSubdomain(tenant.getSubdomain())
                .setTenantName(tenant.getTenantName())
                .setHost(tenant.getHost())
                .setEmail(tenant.getEmail())
                .setWebUrl(tenant.getWebUrl())
                .setLogo(tenant.getLogo())
                .setChecklistUrl(tenant.getChecklistUrl())
                .setConnectionPoolConfig(tenant.getConnectionPoolConfig());

        // Only include relevant connection strings based on call type
        switch (callType.toLowerCase()) {
            case "hrms":
                filtered.setConnectionString(tenant.getConnectionString());
                break;
            case "checklist":
                filtered.setConnectionStringNode(tenant.getConnectionStringNode());
                break;
            case "jobs":
                filtered.setConnectionStringJobSync(tenant.getConnectionStringJobSync());
                break;
            default:
                // No connection strings for unknown call types
                break;
        }

        return filtered;
    }

    private boolean testConnection(String connectionString) {
        try (com.mongodb.client.MongoClient mongoClient = com.mongodb.client.MongoClients.create(connectionString)) {
            // Test connection by listing databases
            mongoClient.listDatabaseNames().first();
            return true;
        } catch (Exception e) {
            logger.debug("Connection test failed: {}", e.getMessage());
            return false;
        }
    }

    private String maskConnectionString(String connectionString) {
        return connectionString.replaceAll("://([^:]+):([^@]+)@", "://*****:*****@");
    }
}