package com.midas.consulting.service;

import com.midas.consulting.model.IpWhitelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Base interface for IP Whitelist operations.
 * TenantId is used ONLY for database routing (setting TenantContext),
 * NOT as a business parameter in queries.
 */
public interface WhitelistService {

    // ============================================================================
    // CORE WHITELIST CHECKING - tenantId needed for DB routing
    // ============================================================================

    /**
     * Check if an IP is whitelisted.
     * @param clientIp the client IP address
     * @param tenantId used for database routing only
     */
    boolean isIpWhitelisted(String clientIp, String tenantId);

    /**
     * Check if an IP is whitelisted for a specific user with roles.
     * @param clientIp the client IP address
     * @param tenantId used for database routing only
     * @param userId the user ID to check
     * @param userRoles the user's roles
     */
    boolean isIpWhitelisted(String clientIp, String tenantId, String userId, Set<String> userRoles);

    /**
     * Validate user IP access with detailed result.
     * @param clientIp the client IP address
     * @param tenantId used for database routing only
     * @param userId the user ID
     * @param userRoles the user's roles
     * @return ValidationResult with details
     */
    ValidationResult validateUserIpAccess(String clientIp, String tenantId, String userId, Set<String> userRoles);

    // ============================================================================
    // CRUD OPERATIONS - tenantId needed for DB routing
    // ============================================================================

    /**
     * Create a new IP whitelist entry.
     * @param ipWhitelist the entry to create (tenantId will be set internally)
     * @param tenantId used for database routing only
     * @param createdBy the user creating the entry
     */
    IpWhitelist create(IpWhitelist ipWhitelist, String tenantId, String createdBy);

    /**
     * Get an IP whitelist entry by ID.
     * @param id the entry ID
     * @param tenantId used for database routing only
     */
    Optional<IpWhitelist> getById(String id, String tenantId);

    /**
     * Get all IP whitelist entries with pagination.
     * @param tenantId used for database routing only
     * @param pageable pagination parameters
     */
    Page<IpWhitelist> getAllByTenant(String tenantId, Pageable pageable);

    /**
     * Update an IP whitelist entry.
     * @param id the entry ID
     * @param ipWhitelist the updated entry data
     * @param tenantId used for database routing only
     * @param updatedBy the user updating the entry
     */
    IpWhitelist update(String id, IpWhitelist ipWhitelist, String tenantId, String updatedBy);

    /**
     * Delete an IP whitelist entry.
     * @param id the entry ID
     * @param tenantId used for database routing only
     */
    void delete(String id, String tenantId);

    // ============================================================================
    // STATISTICS - tenantId needed for DB routing
    // ============================================================================

    /**
     * Get basic statistics about IP whitelist entries.
     * @param tenantId used for database routing only
     * @return Map containing statistics
     */
    Map<String, Object> getStatistics(String tenantId);
}