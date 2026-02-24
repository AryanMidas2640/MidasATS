package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.response.BulkDeleteIpResponse;
import com.midas.consulting.controller.v1.response.BulkDeletePreview;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.dto.HybridIpRequest;
import com.midas.consulting.dto.IpWhitelistResponse;
import com.midas.consulting.dto.RoleSpecificIpRequest;
import com.midas.consulting.dto.UserSpecificIpRequest;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.model.IpWhitelist;
import com.midas.consulting.model.IpWhitelistScope;
import com.midas.consulting.security.SecurityConstants;
import com.midas.consulting.service.IpWhitelistService;
import com.midas.consulting.service.TenantContext;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.ValidationResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/ip-whitelist")
@Api(value = "IP Whitelist Management", description = "Comprehensive IP whitelist management with user-specific, role-specific, and bulk operations")
@Validated
public class IpWhitelistController {

    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistController.class);

    private final IpWhitelistService ipWhitelistService;
    private final UserService userService;

    @Autowired
    public IpWhitelistController(IpWhitelistService ipWhitelistService, UserService userService) {
        this.ipWhitelistService = ipWhitelistService;
        this.userService = userService;
    }

    // ============================================================================
    // BASIC CRUD OPERATIONS
    // ============================================================================

    @GetMapping
    @ApiOperation(value = "Get all IP whitelist entries with pagination", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Response> getAllEntries(
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            @ApiParam(value = "Page number", defaultValue = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @ApiParam(value = "Page size", defaultValue = "10") @RequestParam(defaultValue = "10") @Min(1) int size,
            @ApiParam(value = "Filter by active status") @RequestParam(required = false) Boolean active,
            @ApiParam(value = "Filter by scope") @RequestParam(required = false) IpWhitelistScope scope,
            @ApiParam(value = "Search by IP address or description") @RequestParam(required = false) String search,
            Principal principal) {

        try {
            logger.info("=== GET /api/v1/ip-whitelist ===");
            logger.info("Tenant: {}, Page: {}, Size: {}, Active: {}, Scope: {}, Search: {}",
                    tenantId, page, size, active, scope, search);

            // ✅ CRITICAL: Set tenant context
            TenantContext.setCurrentTenant(tenantId);

            Pageable pageable = PageRequest.of(page, size);
            Page<IpWhitelist> entries;

            if (scope != null) {
                logger.info("Fetching entries by scope: {}", scope);
                entries = ipWhitelistService.getEntriesByScope(tenantId, scope, pageable);
            } else {
                logger.info("Fetching all entries for tenant");
                entries = ipWhitelistService.getAllByTenant(tenantId, pageable);
            }

            logger.info("Service returned: {} entries, {} total",
                    entries.getNumberOfElements(), entries.getTotalElements());

            // Apply filters if provided
            if (active != null || StringUtils.hasText(search)) {
                logger.info("Applying filters: active={}, search={}", active, search);
                List<IpWhitelist> filteredContent = entries.getContent().stream()
                        .filter(entry -> active == null || entry.isActive() == active)
                        .filter(entry -> !StringUtils.hasText(search) ||
                                (entry.getIpAddress().contains(search) ||
                                        (entry.getDescription() != null && entry.getDescription().toLowerCase().contains(search.toLowerCase()))))
                        .collect(Collectors.toList());

                entries = new PageImpl<>(filteredContent, pageable, filteredContent.size());
                logger.info("After filtering: {} entries", filteredContent.size());
            }

            List<IpWhitelistResponse> responseList = entries.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", responseList);
            responseData.put("totalElements", entries.getTotalElements());
            responseData.put("totalPages", entries.getTotalPages());
            responseData.put("size", entries.getSize());
            responseData.put("number", entries.getNumber());

            logger.info("✅ Returning {} entries to client", responseList.size());

            return ResponseEntity.ok(Response.ok().setPayload(responseData).build());

        } catch (Exception e) {
            logger.error("❌ Error fetching IP whitelist entries: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error fetching entries: " + e.getMessage()).build());
        } finally {
            // Clean up tenant context
            TenantContext.clear();
        }
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Get IP whitelist entry by ID", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Response> getEntryById(
            @ApiParam(value = "Entry ID", required = true) @PathVariable String id,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            Optional<IpWhitelist> entry = ipWhitelistService.getById(id, tenantId);
            if (entry.isPresent()) {
                IpWhitelistResponse response = mapToResponse(entry.get());
                return ResponseEntity.ok(Response.ok().setPayload(response).build());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound().setMessage("Entry not found").build());
            }
        } catch (Exception e) {
            logger.error("Error fetching IP whitelist entry {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error fetching entry").build());
        }
    }

    @PostMapping
    @ApiOperation(value = "Create a generic IP whitelist entry", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> createEntry(
            @ApiParam(value = "IP whitelist entry", required = true) @Valid @RequestBody IpWhitelist ipWhitelist,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            logger.info("Creating IP whitelist entry for tenant: {} by user: {}", tenantId, principal.getName());

            // ✅ Validate IP address format
            if (!isValidIpAddress(ipWhitelist.getIpAddress())) {
                logger.warn("Invalid IP address format: {}", ipWhitelist.getIpAddress());
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("Invalid IP address format: " + ipWhitelist.getIpAddress()).build());
            }

            // ✅ Ensure new entry (not update)
            ipWhitelist.setId(null);

            // ✅ CRITICAL: Force active to true
            ipWhitelist.setActive(true);

            // ✅ Set default scope if not provided
            if (ipWhitelist.getScope() == null) {
                // Determine scope based on provided user/role IDs
                boolean hasUserIds = ipWhitelist.getAllowedUserIds() != null && !ipWhitelist.getAllowedUserIds().isEmpty();
                boolean hasRoleIds = ipWhitelist.getAllowedRoleIds() != null && !ipWhitelist.getAllowedRoleIds().isEmpty();

                if (hasUserIds && hasRoleIds) {
                    ipWhitelist.setScope(IpWhitelistScope.HYBRID);
                } else if (hasUserIds) {
                    ipWhitelist.setScope(IpWhitelistScope.USER_SPECIFIC);
                } else if (hasRoleIds) {
                    ipWhitelist.setScope(IpWhitelistScope.ROLE_SPECIFIC);
                } else {
                    ipWhitelist.setScope(IpWhitelistScope.TENANT);
                }

                logger.info("Auto-determined scope: {}", ipWhitelist.getScope());
            }

            // ✅ Set default priority if not provided
            if (ipWhitelist.getPriority() == null || ipWhitelist.getPriority() == 0) {
                ipWhitelist.setPriority(ipWhitelist.getScope().getPriorityOrder());
            }

            // ✅ Initialize accessCount if null
            if (ipWhitelist.getAccessCount() == null) {
                ipWhitelist.setAccessCount(0L);
            }

            // ✅ Initialize empty sets if null (to prevent NullPointerException)
            if (ipWhitelist.getAllowedUserIds() == null) {
                ipWhitelist.setAllowedUserIds(new HashSet<>());
            }
            if (ipWhitelist.getAllowedRoleIds() == null) {
                ipWhitelist.setAllowedRoleIds(new HashSet<>());
            }

            // ✅ Validate scope consistency
            boolean hasUserIds = !ipWhitelist.getAllowedUserIds().isEmpty();
            boolean hasRoleIds = !ipWhitelist.getAllowedRoleIds().isEmpty();

            switch (ipWhitelist.getScope()) {
                case USER_SPECIFIC:
                    if (!hasUserIds) {
                        return ResponseEntity.badRequest()
                                .body(Response.badRequest()
                                        .setMessage("USER_SPECIFIC scope requires at least one user ID")
                                        .build());
                    }
                    if (hasRoleIds) {
                        logger.warn("USER_SPECIFIC scope should not have role IDs. Clearing role IDs.");
                        ipWhitelist.setAllowedRoleIds(new HashSet<>());
                    }
                    break;

                case ROLE_SPECIFIC:
                    if (!hasRoleIds) {
                        return ResponseEntity.badRequest()
                                .body(Response.badRequest()
                                        .setMessage("ROLE_SPECIFIC scope requires at least one role ID")
                                        .build());
                    }
                    if (hasUserIds) {
                        logger.warn("ROLE_SPECIFIC scope should not have user IDs. Clearing user IDs.");
                        ipWhitelist.setAllowedUserIds(new HashSet<>());
                    }
                    break;

                case HYBRID:
                    if (!hasUserIds && !hasRoleIds) {
                        return ResponseEntity.badRequest()
                                .body(Response.badRequest()
                                        .setMessage("HYBRID scope requires at least one user ID or role ID")
                                        .build());
                    }
                    break;

                case TENANT:
                case GLOBAL:
                case ADMIN:
                    // These scopes can have or not have user/role restrictions
                    break;
            }

            // Call service to create
            IpWhitelist created = ipWhitelistService.create(ipWhitelist, tenantId, principal.getName());
            IpWhitelistResponse response = mapToResponse(created);

            logger.info("✅ Successfully created IP whitelist entry: id={}, ipAddress={}, scope={}, active={}",
                    created.getId(), created.getIpAddress(), created.getScope(), created.isActive());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.ok()
                            .setMessage("IP whitelist entry created successfully")
                            .setPayload(response)
                            .build());

        } catch (IllegalArgumentException e) {
            logger.error("Validation error creating IP whitelist entry: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Response.badRequest()
                            .setMessage("Validation error: " + e.getMessage())
                            .build());

        } catch (Exception e) {
            logger.error("Error creating IP whitelist entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest()
                            .setMessage("Error creating entry: " + e.getMessage())
                            .build());
        }
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update an existing IP whitelist entry", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> updateEntry(
            @ApiParam(value = "Entry ID", required = true) @PathVariable String id,
            @ApiParam(value = "Updated IP whitelist entry", required = true) @Valid @RequestBody IpWhitelist ipWhitelist,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            // Validate IP address format
            if (!isValidIpAddress(ipWhitelist.getIpAddress())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("Invalid IP address format").build());
            }

            IpWhitelist updated = ipWhitelistService.update(id, ipWhitelist, tenantId, principal.getName());
            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound().setMessage("Entry not found").build());
            }
            
            IpWhitelistResponse response = mapToResponse(updated);

            logger.info("Updated IP whitelist entry {} for tenant {} by user {}", 
                    id, tenantId, principal.getName());

            return ResponseEntity.ok(Response.ok().setPayload(response).build());
        } catch (Exception e) {
            logger.error("Error updating IP whitelist entry {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error updating entry: " + e.getMessage()).build());
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete an IP whitelist entry", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> deleteEntry(
            @ApiParam(value = "Entry ID", required = true) @PathVariable String id,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            @ApiParam(value = "Reason for deletion") @RequestParam(required = false) String reason,
            Principal principal) {

        try {
            Optional<IpWhitelist> existing = ipWhitelistService.getById(id, tenantId);
            if (!existing.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound().setMessage("Entry not found").build());
            }

            ipWhitelistService.delete(id, tenantId);

            logger.info("Deleted IP whitelist entry {} for tenant {} by user {}. Reason: {}", 
                    id, tenantId, principal.getName(), reason != null ? reason : "Not provided");

            return ResponseEntity.ok(Response.ok().setMessage("Entry deleted successfully").build());
        } catch (Exception e) {
            logger.error("Error deleting IP whitelist entry {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error deleting entry: " + e.getMessage()).build());
        }
    }

    // ============================================================================
    // SPECIALIZED CREATION ENDPOINTS
    // ============================================================================

    @PostMapping("/user-specific")
    @ApiOperation(
            value = "Create a user-specific IP whitelist entry",
            notes = "Creates an IP whitelist entry that applies only to specific users",
            authorizations = {@Authorization(value = "apiKey")}
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> createUserSpecificEntry(
            @ApiParam(value = "User-specific IP whitelist request", required = true)
            @Valid @RequestBody UserSpecificIpRequest request,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            logger.info("Creating user-specific IP whitelist entry for tenant: {} by user: {}", tenantId, principal.getName());

            // Validate IP address
            if (!isValidIpAddress(request.getIpAddress())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("Invalid IP address format").build());
            }

            // Validate user IDs
            if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("User IDs are required for user-specific entries").build());
            }

            // Validate users exist (optional - depends on your system)
            for (String userId : request.getUserIds()) {
                if (!StringUtils.hasText(userId)) {
                    return ResponseEntity.badRequest()
                            .body(Response.badRequest().setMessage("Invalid user ID provided").build());
                }
            }




            IpWhitelist created = ipWhitelistService.createUserSpecific(
                    tenantId,
                    request.getIpAddress(),
                    request.getDescription(),
                    request.getUserIds(),
                    principal.getName(),
                    request.getUserSpecString()
            );

            IpWhitelistResponse response = mapToResponse(created);

            logger.info("Created user-specific IP whitelist entry {} for {} users", 
                    created.getId(), request.getUserIds().size());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.ok().setPayload(response).build());

        } catch (Exception e) {
            logger.error("Error creating user-specific IP whitelist entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error creating user-specific entry: " + e.getMessage()).build());
        }
    }

    @PostMapping("/role-specific")
    @ApiOperation(
            value = "Create a role-specific IP whitelist entry",
            notes = "Creates an IP whitelist entry that applies to users with specific roles",
            authorizations = {@Authorization(value = "apiKey")}
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> createRoleSpecificEntry(
            @ApiParam(value = "Role-specific IP whitelist request", required = true)
            @Valid @RequestBody RoleSpecificIpRequest request,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            logger.info("Creating role-specific IP whitelist entry for tenant: {} by user: {}", tenantId, principal.getName());

            // Validate IP address
            if (!isValidIpAddress(request.getIpAddress())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("Invalid IP address format").build());
            }

            // Validate role IDs
            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("Role IDs are required for role-specific entries").build());
            }

            IpWhitelist created = ipWhitelistService.createRoleSpecific(
                    tenantId,
                    request.getIpAddress(),
                    request.getDescription(),
                    request.getRoleIds(),
                    principal.getName(),
                    request.getRoleSpecString()
            );

            IpWhitelistResponse response = mapToResponse(created);

            logger.info("Created role-specific IP whitelist entry {} for {} roles", 
                    created.getId(), request.getRoleIds().size());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.ok().setPayload(response).build());

        } catch (Exception e) {
            logger.error("Error creating role-specific IP whitelist entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error creating role-specific entry: " + e.getMessage()).build());
        }
    }

    @PostMapping("/hybrid")
    @ApiOperation(
            value = "Create a hybrid IP whitelist entry",
            notes = "Creates an IP whitelist entry that applies to both specific users and roles",
            authorizations = {@Authorization(value = "apiKey")}
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> createHybridEntry(
            @ApiParam(value = "Hybrid IP whitelist request", required = true)
            @Valid @RequestBody HybridIpRequest request,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            logger.info("Creating hybrid IP whitelist entry for tenant: {} by user: {}", tenantId, principal.getName());

            // Validate IP address
            if (!isValidIpAddress(request.getIpAddress())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("Invalid IP address format").build());
            }

            // Validate that at least one of userIds or roleIds is provided
            if ((request.getUserIds() == null || request.getUserIds().isEmpty()) &&
                (request.getRoleIds() == null || request.getRoleIds().isEmpty())) {
                return ResponseEntity.badRequest()
                        .body(Response.badRequest().setMessage("At least one user ID or role ID is required for hybrid entries").build());
            }

            IpWhitelist created = ipWhitelistService.createHybrid(
                    tenantId,
                    request.getIpAddress(),
                    request.getDescription(),
                    request.getUserIds(),
                    request.getRoleIds(),
                    principal.getName()
            );

            IpWhitelistResponse response = mapToResponse(created);

            logger.info("Created hybrid IP whitelist entry {} for {} users and {} roles", 
                    created.getId(), 
                    request.getUserIds() != null ? request.getUserIds().size() : 0,
                    request.getRoleIds() != null ? request.getRoleIds().size() : 0);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Response.ok().setPayload(response).build());

        } catch (Exception e) {
            logger.error("Error creating hybrid IP whitelist entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error creating hybrid entry: " + e.getMessage()).build());
        }
    }

    // ============================================================================
    // QUERY AND UTILITY ENDPOINTS
    // ============================================================================

    @GetMapping("/by-scope")
    @ApiOperation(value = "Get IP whitelist entries by scope", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Response> getEntriesByScope(
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            @ApiParam(value = "Scope filter", required = true) @RequestParam IpWhitelistScope scope,
            @ApiParam(value = "Page number", defaultValue = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @ApiParam(value = "Page size", defaultValue = "10") @RequestParam(defaultValue = "10") @Min(1) int size,
            Principal principal) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<IpWhitelist> entries = ipWhitelistService.getEntriesByScope(tenantId, scope, pageable);

            List<IpWhitelistResponse> responseList = entries.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("content", responseList);
            responseData.put("totalElements", entries.getTotalElements());
            responseData.put("totalPages", entries.getTotalPages());
            responseData.put("scope", scope);

            return ResponseEntity.ok(Response.ok().setPayload(responseData).build());
        } catch (Exception e) {
            logger.error("Error fetching IP whitelist entries by scope: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error fetching entries by scope: " + e.getMessage()).build());
        }
    }

    @GetMapping("/by-user/{userId}")
    @ApiOperation(value = "Get IP whitelist entries for a specific user", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Response> getEntriesForUser(
            @ApiParam(value = "User ID", required = true) @PathVariable String userId,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            @ApiParam(value = "Page number", defaultValue = "0") @RequestParam(defaultValue = "0") @Min(0) int page,
            @ApiParam(value = "Page size", defaultValue = "10") @RequestParam(defaultValue = "10") @Min(1) int size,
            Principal principal) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<IpWhitelist> entries = ipWhitelistService.getEntriesForUserId(tenantId, userId, pageable);

            List<IpWhitelistResponse> responseList = entries.getContent().stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Response.ok().setPayload(responseList).build());
        } catch (Exception e) {
            logger.error("Error fetching IP whitelist entries for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error fetching entries for user: " + e.getMessage()).build());
        }
    }



    @PostMapping("/validate-access")
    @ApiOperation(value = "Validate IP access for a user", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Response> validateIpAccess(
            @ApiParam(value = "Client IP address", required = true) @RequestParam String clientIp,
            @ApiParam(value = "User ID", required = true) @RequestParam String userId,
            @ApiParam(value = "User roles") @RequestParam(required = false) List<String> userRoles,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            Set<String> roles = userRoles != null ? new HashSet<>(userRoles) : new HashSet<>();
            UserDto userDto = userService.findUserById(userId);

            boolean isSuperAdmin = userDto.getRoles()
                    .stream()
                    .anyMatch(role -> "SUPERADMIN".equalsIgnoreCase(role.getRole()));
            ValidationResult result= null;
            if (isSuperAdmin) {
                // Allow access or perform super admin–specific logic

                Map<String, Object> responseData = new HashMap<>();
                responseData.put("allowed", true);
                responseData.put("message", "Since Super Admin allowed access by default");
                responseData.put("clientIp", clientIp);
                responseData.put("userId", userId);
                responseData.put("validatedAt", LocalDateTime.now());


                return ResponseEntity.ok(Response.ok().setPayload(responseData).build());
            }

        // Continue normal logic if not SUPERADMIN

            // tenantId should only be used for DB routing, not passed to service logic
            // Assuming you have a TenantContext or similar mechanism
     result      = ipWhitelistService.validateUserIpAccess(clientIp, userId,  tenantId,roles);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("allowed", result.isAllowed());
            responseData.put("message", result.getMessage());
            responseData.put("clientIp", clientIp);
            responseData.put("userId", userId);
            responseData.put("validatedAt", LocalDateTime.now());

            if (result.getMatchedEntry() != null) {
                responseData.put("matchedEntry", mapToResponse(result.getMatchedEntry()));
            }

            return ResponseEntity.ok(Response.ok().setPayload(responseData).build());
        } catch (Exception e) {
            logger.error("Error validating IP access for {}: {}", clientIp, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error validating access: " + e.getMessage()).build());
        }
    }

    @GetMapping("/statistics")
    @ApiOperation(value = "Get comprehensive IP whitelist statistics", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> getStatistics(
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId) {

        try {
            Map<String, Object> stats = ipWhitelistService.getStatistics(tenantId);
            return ResponseEntity.ok(Response.ok().setPayload(stats).build());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid tenant ID for statistics: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Response.badRequest().setMessage("Invalid request: " + e.getMessage()).build());
        } catch (Exception e) {
            logger.error("Error fetching IP whitelist statistics for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error fetching statistics: " + e.getMessage()).build());
        }
    }
//    @PatchMapping("/{id}/toggle-status")
//    @ApiOperation(
//            value = "Toggle active/inactive status of an IP whitelist entry",
//            notes = "Toggles the active status of an IP whitelist entry. If currently active, it becomes inactive and vice versa.",
//            authorizations = {@Authorization(value = "apiKey")}
//    )
//    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
//    public ResponseEntity<Response> toggleEntryStatus(
//            @ApiParam(value = "Entry ID", required = true) @PathVariable String id,
//            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
//            @ApiParam(value = "Reason for status change") @RequestParam(required = false) String reason,
//            Principal principal) {
//
//        try {
//            Optional<IpWhitelist> existingOpt = ipWhitelistService.getById(id, tenantId);
//            if (!existingOpt.isPresent()) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                        .body(Response.notFound().setMessage("Entry not found").build());
//            }
//
//            IpWhitelist existing = existingOpt.get();
//            existing.setActive(!existing.isActive());
//
//            IpWhitelist updated = ipWhitelistService.update(id, existing, tenantId, principal.getName());
//            IpWhitelistResponse response = mapToResponse(updated);
//
//            logger.info("Toggled status of IP whitelist entry {} to {} for tenant {} by user {}. Reason: {}",
//                    id, updated.isActive() ? "active" : "inactive", tenantId, principal.getName(),
//                    reason != null ? reason : "Not provided");
//
//            return ResponseEntity.ok(Response.ok().setPayload(response).build());
//        } catch (Exception e) {
//            logger.error("Error toggling status of IP whitelist entry {}: {}", id, e.getMessage(), e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Response.badRequest().setMessage("Error updating entry status: " + e.getMessage()).build());
//        }
//    }


    @PatchMapping("/{id}/toggle-status")
    @ApiOperation(
            value = "Toggle active/inactive status of an IP whitelist entry",
            notes = "Toggles the active status of an IP whitelist entry. If currently active, it becomes inactive and vice versa.",
            authorizations = {@Authorization(value = "apiKey")}
    )
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN') or hasRole('IP_MANAGER')")
    public ResponseEntity<Response> toggleEntryStatus(
            @ApiParam(value = "Entry ID", required = true) @PathVariable String id,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            @ApiParam(value = "Reason for status change") @RequestParam(required = false) String reason,
            Principal principal) {

        try {
            Optional<IpWhitelist> existingOpt = ipWhitelistService.getById(id, tenantId);
            if (!existingOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Response.notFound().setMessage("Entry not found").build());
            }

            IpWhitelist existing = existingOpt.get();
            existing.setActive(!existing.isActive());

            // Set updatedBy and updatedAt
            existing.setUpdatedBy(principal.getName());
            existing.setUpdatedAt(LocalDateTime.now());

            IpWhitelist updated = ipWhitelistService.update(id, existing, tenantId, principal.getName());
            IpWhitelistResponse response = mapToResponse(updated);

            logger.info("User '{}' toggled IP whitelist entry '{}' to '{}' for tenant '{}'. Reason: {}",
                    principal.getName(),
                    id,
                    updated.isActive() ? "active" : "inactive",
                    tenantId,
                    reason != null ? reason : "Not provided"
            );

            return ResponseEntity.ok(Response.ok().setPayload(response).build());
        } catch (Exception e) {
            logger.error("Error toggling status of IP whitelist entry {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error updating entry status: " + e.getMessage()).build());
        }
    }

    // ============================================================================
    // BULK OPERATIONS
    // ============================================================================

    @PostMapping("/bulk-delete/preview")
    @ApiOperation(value = "Preview bulk delete operation", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Response> previewBulkDelete(
            @ApiParam(value = "List of entry IDs to delete", required = true) @RequestBody @NotEmpty List<String> ids,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            BulkDeletePreview preview = ipWhitelistService.previewBulkDelete(ids, tenantId, principal.getName());
            return ResponseEntity.ok(Response.ok().setPayload(preview).build());

        } catch (Exception e) {
            logger.error("Error generating bulk delete preview: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error generating preview: " + e.getMessage()).build());
        }
    }

    @DeleteMapping("/bulk-delete")
    @ApiOperation(value = "Perform bulk delete operation", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Response> bulkDelete(
            @ApiParam(value = "List of entry IDs to delete", required = true) @RequestBody @NotEmpty List<String> ids,
            @ApiParam(value = "Force delete critical entries") @RequestParam(defaultValue = "false") boolean forceDelete,
            @ApiParam(value = "Skip validation checks") @RequestParam(defaultValue = "false") boolean skipValidation,
            @ApiParam(value = "Reason for bulk deletion") @RequestParam(required = false) String reason,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            BulkDeleteIpResponse result = ipWhitelistService.bulkDelete(
                    ids, tenantId, principal.getName(), forceDelete, skipValidation, reason);
            
            logger.info("Bulk delete operation completed by {}: {}/{} entries deleted successfully", 
                    principal.getName(), result.getSuccessfullyDeleted(), result.getTotalRequested());

            return ResponseEntity.ok(Response.ok().setPayload(result).build());

        } catch (Exception e) {
            logger.error("Error in bulk delete operation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Bulk delete failed: " + e.getMessage()).build());
        }
    }

    @PatchMapping("/bulk-soft-delete")
    @ApiOperation(value = "Perform bulk soft delete (deactivate entries)", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Response> bulkSoftDelete(
            @ApiParam(value = "List of entry IDs to deactivate", required = true) @RequestBody @NotEmpty List<String> ids,
            @ApiParam(value = "Reason for bulk deactivation") @RequestParam(required = false) String reason,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            BulkDeleteIpResponse result = ipWhitelistService.bulkSoftDelete(ids, tenantId, principal.getName(), reason);
            
            logger.info("Bulk soft delete operation completed by {}: {}/{} entries deactivated successfully", 
                    principal.getName(), result.getSuccessfullyDeleted(), result.getTotalRequested());

            return ResponseEntity.ok(Response.ok().setPayload(result).build());

        } catch (Exception e) {
            logger.error("Error in bulk soft delete operation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Bulk soft delete failed: " + e.getMessage()).build());
        }
    }

    @DeleteMapping("/by-scope/{scope}")
    @ApiOperation(value = "Delete all entries by scope", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Response> deleteByScope(
            @ApiParam(value = "Scope to delete", required = true) @PathVariable IpWhitelistScope scope,
            @ApiParam(value = "Force delete critical entries") @RequestParam(defaultValue = "false") boolean forceDelete,
            @ApiParam(value = "Reason for scope deletion") @RequestParam(required = false) String reason,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            BulkDeleteIpResponse result = ipWhitelistService.deleteByScope(scope, tenantId, principal.getName(), forceDelete, reason);
            
            logger.warn("Delete by scope operation completed by {}: scope={}, {}/{} entries deleted", 
                    principal.getName(), scope, result.getSuccessfullyDeleted(), result.getTotalRequested());

            return ResponseEntity.ok(Response.ok().setPayload(result).build());

        } catch (Exception e) {
            logger.error("Error deleting by scope {}: {}", scope, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Delete by scope failed: " + e.getMessage()).build());
        }
    }

    @DeleteMapping("/cleanup")
    @ApiOperation(value = "Delete inactive entries older than specified days", authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Response> cleanupInactiveEntries(
            @ApiParam(value = "Delete entries inactive for this many days", defaultValue = "30") @RequestParam(defaultValue = "30") @Min(1) int olderThanDays,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            Principal principal) {

        try {
            BulkDeleteIpResponse result = ipWhitelistService.deleteInactiveEntries(tenantId, olderThanDays, principal.getName());
            
            logger.info("Cleanup operation completed by {}: {}/{} inactive entries deleted (older than {} days)", 
                    principal.getName(), result.getSuccessfullyDeleted(), result.getTotalRequested(), olderThanDays);

            return ResponseEntity.ok(Response.ok().setPayload(result).build());

        } catch (Exception e) {
            logger.error("Error in cleanup operation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Cleanup failed: " + e.getMessage()).build());
        }
    }

    // ============================================================================
    // ACCESS TRACKING AND MONITORING
    // ============================================================================

    @PostMapping("/{id}/track-access")
    @ApiOperation(value = "Track access to an IP whitelist entry", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Response> trackAccess(
            @ApiParam(value = "Entry ID", required = true) @PathVariable String id,
            @ApiParam(value = "Client IP address") @RequestParam(required = false) String clientIp,
            @ApiParam(value = "User ID") @RequestParam(required = false) String userId,
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId,
            HttpServletRequest request,
            Principal principal) {

        try {
            // Extract client IP if not provided
            if (!StringUtils.hasText(clientIp)) {
                clientIp = extractClientIpAddress(request);
            }

            // Use principal name if userId not provided
            if (!StringUtils.hasText(userId)) {
                userId = principal.getName();
            }

            ipWhitelistService.updateLastAccess(id, tenantId, clientIp, userId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("entryId", id);
            responseData.put("clientIp", clientIp);
            responseData.put("userId", userId);
            responseData.put("trackedAt", LocalDateTime.now());

            return ResponseEntity.ok(Response.ok().setPayload(responseData).build());

        } catch (Exception e) {
            logger.error("Error tracking access for entry {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Response.badRequest().setMessage("Error tracking access: " + e.getMessage()).build());
        }
    }

    @GetMapping("/health-check")
    @ApiOperation(value = "Health check for IP whitelist service")
    public ResponseEntity<Response> healthCheck(
            @ApiParam(value = "Tenant ID", required = true) @RequestHeader("X-Tenant") String tenantId) {

        try {
            Map<String, Object> healthData = new HashMap<>();
            healthData.put("service", "IP Whitelist Service");
            healthData.put("status", "UP");
            healthData.put("timestamp", LocalDateTime.now());
            healthData.put("tenantId", tenantId);

            // Basic connectivity check
            try {
                ipWhitelistService.getAllByTenant(tenantId, PageRequest.of(0, 1));
                healthData.put("databaseConnection", "OK");
            } catch (Exception e) {
                healthData.put("databaseConnection", "ERROR: " + e.getMessage());
            }

            return ResponseEntity.ok(Response.ok().setPayload(healthData).build());

        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("service", "IP Whitelist Service");
            errorData.put("status", "DOWN");
            errorData.put("error", e.getMessage());
            errorData.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Response.badRequest().setPayload(errorData).build());
        }
    }

    // ============================================================================
    // HELPER METHODS AND UTILITIES
    // ============================================================================

    private IpWhitelistResponse mapToResponse(IpWhitelist ipWhitelist) {
        if (ipWhitelist == null) {
            return null;
        }

        return IpWhitelistResponse.builder()
                .id(ipWhitelist.getId())
                .tenantId(ipWhitelist.getTenantId())
                .ipAddress(ipWhitelist.getIpAddress())
                .description(ipWhitelist.getDescription())
                .scope(ipWhitelist.getScope())
                .type(ipWhitelist.getType())
                .allowedUserIds(ipWhitelist.getAllowedUserIds())
                .allowedRoleIds(ipWhitelist.getAllowedRoleIds())
                .priority(ipWhitelist.getPriority())
                .isActive(ipWhitelist.isActive())
                .notes(ipWhitelist.getNotes())
                .createdBy(ipWhitelist.getCreatedBy())
                .updatedBy(ipWhitelist.getUpdatedBy())
                .createdAt(ipWhitelist.getCreatedAt())
                .updatedAt(ipWhitelist.getUpdatedAt())
                .lastAccessedAt(ipWhitelist.getLastAccessedAt())
                .lastAccessedBy(ipWhitelist.getLastAccessedBy())
                .lastAccessedIp(ipWhitelist.getLastAccessedIp())
                .accessCount(Long.valueOf(ipWhitelist.getAccessCount()))
                .build();
    }

    private boolean isValidIpAddress(String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return false;
        }

        // Basic IP address validation (IPv4 and IPv6 with CIDR support)
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\/([0-9]|[1-2][0-9]|3[0-2]))?$";
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}(\\/([0-9]|[1-9][0-9]|1[0-1][0-9]|12[0-8]))?$";
        
        return ipAddress.equals("*")||ipAddress.matches(ipv4Pattern) || ipAddress.matches(ipv6Pattern) ||
               ipAddress.equals("0.0.0.0/0") || ipAddress.equals("::/0");
    }

    private String extractClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedForHeader) && !"unknown".equalsIgnoreCase(xForwardedForHeader)) {
            return xForwardedForHeader.split(",")[0].trim();
        }

        String xRealIpHeader = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIpHeader) && !"unknown".equalsIgnoreCase(xRealIpHeader)) {
            return xRealIpHeader;
        }

        String xForwardedHeader = request.getHeader("X-Forwarded");
        if (StringUtils.hasText(xForwardedHeader) && !"unknown".equalsIgnoreCase(xForwardedHeader)) {
            return xForwardedHeader;
        }

        String forwardedForHeader = request.getHeader("Forwarded-For");
        if (StringUtils.hasText(forwardedForHeader) && !"unknown".equalsIgnoreCase(forwardedForHeader)) {
            return forwardedForHeader;
        }

        String forwardedHeader = request.getHeader("Forwarded");
        if (StringUtils.hasText(forwardedHeader) && !"unknown".equalsIgnoreCase(forwardedHeader)) {
            return forwardedHeader;
        }

        return request.getRemoteAddr();
    }

    private String extractUserRole(Principal principal) {
        try {
            // This implementation depends on your JWT token structure
            String token = getCurrentAuthToken();
            if (StringUtils.hasText(token)) {
                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.SECRET)
                        .parseClaimsJws(token.replace("Bearer ", ""))
                        .getBody();

                return claims.get("role", String.class);
            }
        } catch (Exception e) {
            logger.debug("Could not extract user role from token: {}", e.getMessage());
        }
        
        return "USER"; // Default role
    }

    private String getCurrentAuthToken() {
        // Implementation depends on your security context
        // This is a placeholder - implement based on your authentication setup
        return "";
    }

    // ============================================================================
    // EXCEPTION HANDLERS
    // ============================================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Response> handleIllegalArgument(IllegalArgumentException e) {
        logger.warn("Invalid argument: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Response.badRequest().setMessage("Invalid argument: " + e.getMessage()).build());
    }

    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public ResponseEntity<Response> handleValidationError(javax.validation.ConstraintViolationException e) {
        logger.warn("Validation error: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Response.badRequest().setMessage("Validation error: " + e.getMessage()).build());
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Response> handleAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        logger.warn("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Response.forbidden().setMessage("Access denied: " + e.getMessage()).build());
    }
}