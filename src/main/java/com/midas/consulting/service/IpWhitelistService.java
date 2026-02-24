package com.midas.consulting.service;

import com.midas.consulting.controller.v1.response.BulkDeleteIpResponse;
import com.midas.consulting.controller.v1.response.BulkDeletePreview;
import com.midas.consulting.model.IpWhitelist;
import com.midas.consulting.model.IpWhitelistScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extended interface for IP Whitelist operations with advanced features.
 * Extends WhitelistService to include all base operations.
 * TenantId is used ONLY for database routing, NOT in WHERE clauses.
 */
public interface IpWhitelistService extends WhitelistService {

    // ============================================================================
    // ADVANCED CREATION METHODS - tenantId for DB routing
    // ============================================================================

    /**
     * Create a user-specific IP whitelist entry.
     *
     * @param tenantId       used for database routing only
     * @param ipAddress      the IP address or CIDR range
     * @param description    description of the entry
     * @param userIds        set of user IDs allowed
     * @param createdBy      the user creating the entry
     * @param userSpecString
     */
    IpWhitelist createUserSpecific(String tenantId, String ipAddress, String description,
                                   Set<String> userIds, String createdBy, String userSpecString);

    /**
     * Create a role-specific IP whitelist entry.
     *
     * @param tenantId       used for database routing only
     * @param ipAddress      the IP address or CIDR range
     * @param description    description of the entry
     * @param roleIds        set of role IDs allowed
     * @param createdBy      the user creating the entry
     * @param roleSpecString
     */
    IpWhitelist createRoleSpecific(String tenantId, String ipAddress, String description,
                                   Set<String> roleIds, String createdBy, String roleSpecString);

    /**
     * Create a hybrid IP whitelist entry (both users and roles).
     * @param tenantId used for database routing only
     * @param ipAddress the IP address or CIDR range
     * @param description description of the entry
     * @param userIds set of user IDs allowed
     * @param roleIds set of role IDs allowed
     * @param createdBy the user creating the entry
     */
    IpWhitelist createHybrid(String tenantId, String ipAddress, String description,
                             Set<String> userIds, Set<String> roleIds, String createdBy);

    // ============================================================================
    // ADVANCED QUERYING METHODS - tenantId for DB routing
    // ============================================================================

    /**
     * Get entries filtered by scope.
     * @param tenantId used for database routing only
     * @param scope the scope to filter by
     * @param pageable pagination parameters
     */
    Page<IpWhitelist> getEntriesByScope(String tenantId, IpWhitelistScope scope, Pageable pageable);

    /**
     * Get entries for a specific user ID.
     * @param tenantId used for database routing only
     * @param userId the user ID to filter by
     * @param pageable pagination parameters
     */
    Page<IpWhitelist> getEntriesForUserId(String tenantId, String userId, Pageable pageable);

    /**
     * Get entries for a specific role ID.
     * @param tenantId used for database routing only
     * @param roleId the role ID to filter by
     * @param pageable pagination parameters
     */
    Page<IpWhitelist> getEntriesForRoleId(String tenantId, String roleId, Pageable pageable);

    /**
     * Get all entries applicable to a user (considering their roles).
     * @param tenantId used for database routing only
     * @param userId the user ID
     * @param userRoles the user's roles
     * @return list of applicable entries
     */
    List<IpWhitelist> getEntriesForUser(String tenantId, String userId, Set<String> userRoles);

    // ============================================================================
    // ACCESS TRACKING - tenantId for DB routing
    // ============================================================================

    /**
     * Update last access information for an entry.
     * @param entryId the entry ID
     * @param tenantId used for database routing only
     * @param clientIp the client IP that accessed
     * @param userId the user who accessed
     */
    void updateLastAccess(String entryId, String tenantId, String clientIp, String userId);

    // ============================================================================
    // ADVANCED STATISTICS - tenantId for DB routing
    // ============================================================================

    /**
     * Get advanced statistics including scope/type breakdowns.
     * @param tenantId used for database routing only
     * @return Map containing detailed statistics
     */
    Map<String, Object> getAdvancedStatistics(String tenantId);

    // ============================================================================
    // BULK OPERATIONS - tenantId for DB routing
    // ============================================================================

    /**
     * Bulk delete IP whitelist entries.
     * @param ids list of entry IDs to delete
     * @param tenantId used for database routing only
     * @param userId the user performing the deletion
     * @param forceDelete whether to force delete critical entries
     * @param skipValidation whether to skip validation checks
     * @param reason reason for deletion (for audit)
     * @return response with deletion results
     */
    BulkDeleteIpResponse bulkDelete(List<String> ids, String tenantId, String userId,
                                    boolean forceDelete, boolean skipValidation, String reason);

    /**
     * Bulk soft delete (deactivate) IP whitelist entries.
     * @param ids list of entry IDs to deactivate
     * @param tenantId used for database routing only
     * @param userId the user performing the operation
     * @param reason reason for deactivation (for audit)
     * @return response with operation results
     */
    BulkDeleteIpResponse bulkSoftDelete(List<String> ids, String tenantId, String userId, String reason);

    /**
     * Delete all entries with a specific scope.
     * @param scope the scope to delete
     * @param tenantId used for database routing only
     * @param userId the user performing the deletion
     * @param forceDelete whether to force delete critical entries
     * @param reason reason for deletion (for audit)
     * @return response with deletion results
     */
    BulkDeleteIpResponse deleteByScope(IpWhitelistScope scope, String tenantId, String userId,
                                       boolean forceDelete, String reason);

    /**
     * Delete inactive entries older than specified days.
     * @param tenantId used for database routing only
     * @param olderThanDays number of days threshold
     * @param userId the user performing the cleanup
     * @return response with deletion results
     */
    BulkDeleteIpResponse deleteInactiveEntries(String tenantId, int olderThanDays, String userId);

    /**
     * Preview bulk delete operation before executing.
     * @param ids list of entry IDs to preview
     * @param tenantId used for database routing only
     * @param userId the user requesting the preview
     * @return preview with impact assessment
     */
    BulkDeletePreview previewBulkDelete(List<String> ids, String tenantId, String userId);
}