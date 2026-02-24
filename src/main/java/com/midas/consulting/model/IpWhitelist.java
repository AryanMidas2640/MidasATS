package com.midas.consulting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "ip_whitelist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpWhitelist {
    @Id
    private String id;

    @Indexed
    private String updatedBy;

    @Indexed
    private String notes;

    @Indexed
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;

    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\/(?:[0-2]?[0-9]|3[0-2])$|^\\*$",
            message = "Invalid IP address format")
    private String ipAddress;

    private String ipRange; // CIDR notation - kept for backward compatibility

    @NotBlank(message = "Description is required")
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    private boolean isActive = true;

    @Indexed
    @NotNull(message = "IP whitelist type is required")
    private IpWhitelistType type;

    // SCOPE-BASED ACCESS CONTROL FIELDS
    @NotNull(message = "IP whitelist scope is required")
    private IpWhitelistScope scope = IpWhitelistScope.GLOBAL; // Default to global behavior

    @Indexed
    private Set<String> allowedUserIds = new HashSet<>(); // Specific users allowed

    @Indexed
    private Set<String> allowedRoleIds = new HashSet<>(); // Specific roles allowed

    private Integer priority = 0; // For conflict resolution, higher = more priority

    // EXISTING FIELDS
    private String createdBy;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Audit fields
    private String lastAccessedBy;
    private LocalDateTime lastAccessedAt;
    private String lastAccessedIp;
    private Long accessCount = 0L;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        validateAndSetDefaults();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        validateScopeConsistency();
    }

    /**
     * Initialize defaults and validate on creation
     */
    private void validateAndSetDefaults() {
        // Set defaults if null
        if (scope == null) {
            scope = IpWhitelistScope.GLOBAL;
        }
        if (type == null && ipAddress != null) {
            type = IpWhitelistType.determineFromIpAddress(ipAddress);
        }
        if (priority == null) {
            priority = 0;
        }
        if (accessCount == null) {
            accessCount = 0L;
        }

        validateScopeConsistency();
    }

    /**
     * Validate that the scope matches the provided user/role IDs
     */
    private void validateScopeConsistency() {
        if (scope == null) {
            scope = IpWhitelistScope.GLOBAL;
        }

        boolean hasUserIds = allowedUserIds != null && !allowedUserIds.isEmpty();
        boolean hasRoleIds = allowedRoleIds != null && !allowedRoleIds.isEmpty();

        switch (scope) {
            case USER_SPECIFIC:
                if (!hasUserIds) {
                    throw new IllegalArgumentException("USER_SPECIFIC scope requires at least one user ID");
                }
                if (hasRoleIds) {
                    throw new IllegalArgumentException("USER_SPECIFIC scope should not have role IDs");
                }
                if (type != IpWhitelistType.USER_SPECIFIC) {
                    type = IpWhitelistType.USER_SPECIFIC;
                }
                break;

            case ROLE_SPECIFIC:
                if (!hasRoleIds) {
                    throw new IllegalArgumentException("ROLE_SPECIFIC scope requires at least one role ID");
                }
                if (hasUserIds) {
                    throw new IllegalArgumentException("ROLE_SPECIFIC scope should not have user IDs");
                }
                if (type != IpWhitelistType.ROLE_SPECIFIC) {
                    type = IpWhitelistType.ROLE_SPECIFIC;
                }
                break;

            case HYBRID:
                if (!hasUserIds && !hasRoleIds) {
                    throw new IllegalArgumentException("HYBRID scope requires at least one user ID or role ID");
                }
                if (type != IpWhitelistType.HYBRID) {
                    type = IpWhitelistType.HYBRID;
                }
                break;

            case GLOBAL:
                // Global scope can have user/role restrictions or be completely open
                // If it has restrictions, it behaves like tenant-wide but with user/role filters
                // Set appropriate type based on IP format if not already an access-based type
                if (type == null || (!type.requiresIpValidation() && !hasUserIds && !hasRoleIds)) {
                    type = IpWhitelistType.determineFromIpAddress(ipAddress);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown scope: " + scope);
        }

        // Validate IP address format against type
        if (type != null && type.requiresIpValidation() && ipAddress != null) {
            if (!type.isValidIpFormat(ipAddress)) {
                throw new IllegalArgumentException(
                        "IP address '" + ipAddress + "' is not valid for type " + type.getDisplayName()
                );
            }
        }

        // Set priority based on scope if not explicitly set
        if (priority == null || priority == 0) {
            priority = scope.getPriorityOrder();
        }
    }

    /**
     * Check if this whitelist entry applies to a specific user and their roles
     */
//    public boolean appliesToUser(String userId, Set<String> userRoles) {
//        if (!isActive) {
//            return false;
//        }
//
//        switch (scope) {
//            case GLOBAL:
//                // Global entries apply to everyone, but may have additional user/role filters
//                if (allowedUserIds.isEmpty() && allowedRoleIds.isEmpty()) {
//                    return true; // Truly global
//                }
//                // Has filters - check them
//                boolean userMatch = allowedUserIds.contains(userId);
//                boolean roleMatch = userRoles != null && !Collections.disjoint(allowedRoleIds, userRoles);
//                return userMatch || roleMatch;
//
//            case USER_SPECIFIC:
//                return allowedUserIds != null && allowedUserIds.contains(userId);
//
//            case ROLE_SPECIFIC:
//                return allowedRoleIds != null && userRoles != null &&
//                        !Collections.disjoint(allowedRoleIds, userRoles);
//
//            case HYBRID:
//                boolean userMatchHybrid = allowedUserIds != null && allowedUserIds.contains(userId);
//                boolean roleMatchHybrid = allowedRoleIds != null && userRoles != null &&
//                        !Collections.disjoint(allowedRoleIds, userRoles);
//                return userMatchHybrid || roleMatchHybrid;
//
//            default:
//                return false;
//        }
//    }

    /**
     * Check if this whitelist entry applies to a specific user and their roles
     *
     * CRITICAL FIX: Added missing TENANT and ADMIN scope cases
     *
     * @param userId the user ID to check
     * @param userRoles the user's roles
     * @return true if this entry grants access to the user, false otherwise
     */
    public boolean appliesToUser(String userId, Set<String> userRoles) {
        if (!isActive) {
            return false;
        }

        switch (scope) {
            case GLOBAL:
                // Global entries apply to everyone, but may have additional user/role filters
                if (allowedUserIds.isEmpty() && allowedRoleIds.isEmpty()) {
                    return true; // Truly global
                }
                // Has filters - check them
                boolean userMatch = allowedUserIds.contains(userId);
                boolean roleMatch = userRoles != null && !Collections.disjoint(allowedRoleIds, userRoles);
                return userMatch || roleMatch;

            case TENANT:
                // CRITICAL FIX: TENANT scope was missing!
                // Tenant entries apply to ALL users in the tenant
                // But may have additional user/role filters for further restrictions
                if (allowedUserIds.isEmpty() && allowedRoleIds.isEmpty()) {
                    return true; // All users in tenant
                }
                // Has filters - check them
                boolean tenantUserMatch = allowedUserIds.contains(userId);
                boolean tenantRoleMatch = userRoles != null && !Collections.disjoint(allowedRoleIds, userRoles);
                return tenantUserMatch || tenantRoleMatch;

            case ADMIN:
                // CRITICAL FIX: ADMIN scope was missing!
                // Admin entries apply to admin operations
                // Can have user/role filters or apply to all admins
                if (allowedUserIds.isEmpty() && allowedRoleIds.isEmpty()) {
                    return true; // All admin access
                }
                // Has filters - check them
                boolean adminUserMatch = allowedUserIds.contains(userId);
                boolean adminRoleMatch = userRoles != null && !Collections.disjoint(allowedRoleIds, userRoles);
                return adminUserMatch || adminRoleMatch;

            case USER_SPECIFIC:
                return allowedUserIds != null && allowedUserIds.contains(userId);

            case ROLE_SPECIFIC:
                return allowedRoleIds != null && userRoles != null &&
                        !Collections.disjoint(allowedRoleIds, userRoles);

            case HYBRID:
                boolean userMatchHybrid = allowedUserIds != null && allowedUserIds.contains(userId);
                boolean roleMatchHybrid = allowedRoleIds != null && userRoles != null &&
                        !Collections.disjoint(allowedRoleIds, userRoles);
                return userMatchHybrid || roleMatchHybrid;

            default:
                // Should never reach here if all scopes are handled
                return false;
        }
    }
    /**
     * Check if this is a critical IP range that requires special handling
     */
    public boolean isCriticalIpRange() {
        if (ipAddress == null) {
            return false;
        }

        return ipAddress.equals("0.0.0.0/0") ||
                ipAddress.equals("::/0") ||
                ipAddress.startsWith("127.") ||
                ipAddress.startsWith("10.") ||
                ipAddress.startsWith("172.") ||
                ipAddress.startsWith("192.168.") ||
                scope == IpWhitelistScope.GLOBAL ||
                "*".equals(ipAddress.trim());
    }

    /**
     * Update access tracking information
     */
    public void updateLastAccess(String clientIp, String userId) {
        this.lastAccessedAt = LocalDateTime.now();
        this.lastAccessedBy = userId;
        this.lastAccessedIp = clientIp;
        if (this.accessCount == null) {
            this.accessCount = 0L;
        }
        this.accessCount++;
    }

    /**
     * Get effective priority for sorting (combines scope priority and custom priority)
     */
    public int getEffectivePriority() {
        return (scope.getPriorityOrder() * 1000) + (priority != null ? priority : 0);
    }

    /**
     * Check if this entry allows specific users
     */
    public boolean allowsSpecificUsers() {
        return scope.allowsSpecificUsers();
    }

    /**
     * Check if this entry allows specific roles
     */
    public boolean allowsSpecificRoles() {
        return scope.allowsSpecificRoles();
    }

    /**
     * Check if this is a global entry
     */
    public boolean isGlobal() {
        return scope.isGlobal();
    }

    // ============================================================================
    // CONVENIENCE METHODS FOR SETTING SCOPE-SPECIFIC DATA
    // ============================================================================

    /**
     * Configure this entry for user-specific access
     */
    public IpWhitelist setUserScope(Set<String> userIds) {
        this.scope = IpWhitelistScope.USER_SPECIFIC;
        this.type = IpWhitelistType.USER_SPECIFIC;
        this.allowedUserIds = userIds != null ? new HashSet<>(userIds) : new HashSet<>();
        this.allowedRoleIds = new HashSet<>();
        return this;
    }

    /**
     * Configure this entry for role-specific access
     */
    public IpWhitelist setRoleScope(Set<String> roleIds) {
        this.scope = IpWhitelistScope.ROLE_SPECIFIC;
        this.type = IpWhitelistType.ROLE_SPECIFIC;
        this.allowedRoleIds = roleIds != null ? new HashSet<>(roleIds) : new HashSet<>();
        this.allowedUserIds = new HashSet<>();
        return this;
    }

    /**
     * Configure this entry for hybrid user/role access
     */
    public IpWhitelist setHybridScope(Set<String> userIds, Set<String> roleIds) {
        this.scope = IpWhitelistScope.HYBRID;
        this.type = IpWhitelistType.HYBRID;
        this.allowedUserIds = userIds != null ? new HashSet<>(userIds) : new HashSet<>();
        this.allowedRoleIds = roleIds != null ? new HashSet<>(roleIds) : new HashSet<>();
        return this;
    }

    /**
     * Configure this entry for global access (no user/role restrictions)
     */
    public IpWhitelist setGlobalScope() {
        this.scope = IpWhitelistScope.GLOBAL;
        this.allowedUserIds = new HashSet<>();
        this.allowedRoleIds = new HashSet<>();
        // Keep existing type or determine from IP address
        if (this.type == IpWhitelistType.USER_SPECIFIC ||
                this.type == IpWhitelistType.ROLE_SPECIFIC ||
                this.type == IpWhitelistType.HYBRID) {
            this.type = IpWhitelistType.determineFromIpAddress(this.ipAddress);
        }
        return this;
    }

    /**
     * Add a user to the allowed users list
     */
    public IpWhitelist addAllowedUser(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            if (this.allowedUserIds == null) {
                this.allowedUserIds = new HashSet<>();
            }
            this.allowedUserIds.add(userId);

            // Adjust scope if necessary
            if (scope == IpWhitelistScope.ROLE_SPECIFIC) {
                scope = IpWhitelistScope.HYBRID;
                type = IpWhitelistType.HYBRID;
            } else if (scope == IpWhitelistScope.GLOBAL && allowedRoleIds.isEmpty()) {
                scope = IpWhitelistScope.USER_SPECIFIC;
                type = IpWhitelistType.USER_SPECIFIC;
            }
        }
        return this;
    }

    /**
     * Add a role to the allowed roles list
     */
    public IpWhitelist addAllowedRole(String roleId) {
        if (roleId != null && !roleId.trim().isEmpty()) {
            if (this.allowedRoleIds == null) {
                this.allowedRoleIds = new HashSet<>();
            }
            this.allowedRoleIds.add(roleId);

            // Adjust scope if necessary
            if (scope == IpWhitelistScope.USER_SPECIFIC) {
                scope = IpWhitelistScope.HYBRID;
                type = IpWhitelistType.HYBRID;
            } else if (scope == IpWhitelistScope.GLOBAL && allowedUserIds.isEmpty()) {
                scope = IpWhitelistScope.ROLE_SPECIFIC;
                type = IpWhitelistType.ROLE_SPECIFIC;
            }
        }
        return this;
    }

    /**
     * Remove a user from the allowed users list
     */
    public IpWhitelist removeAllowedUser(String userId) {
        if (allowedUserIds != null) {
            allowedUserIds.remove(userId);
            adjustScopeAfterRemoval();
        }
        return this;
    }

    /**
     * Remove a role from the allowed roles list
     */
    public IpWhitelist removeAllowedRole(String roleId) {
        if (allowedRoleIds != null) {
            allowedRoleIds.remove(roleId);
            adjustScopeAfterRemoval();
        }
        return this;
    }

    /**
     * Adjust scope after removing users or roles
     */
    private void adjustScopeAfterRemoval() {
        boolean hasUsers = allowedUserIds != null && !allowedUserIds.isEmpty();
        boolean hasRoles = allowedRoleIds != null && !allowedRoleIds.isEmpty();

        if (scope == IpWhitelistScope.HYBRID) {
            if (!hasUsers && hasRoles) {
                scope = IpWhitelistScope.ROLE_SPECIFIC;
                type = IpWhitelistType.ROLE_SPECIFIC;
            } else if (hasUsers && !hasRoles) {
                scope = IpWhitelistScope.USER_SPECIFIC;
                type = IpWhitelistType.USER_SPECIFIC;
            } else if (!hasUsers && !hasRoles) {
                scope = IpWhitelistScope.GLOBAL;
                type = IpWhitelistType.determineFromIpAddress(ipAddress);
            }
        } else if (scope == IpWhitelistScope.USER_SPECIFIC && !hasUsers) {
            scope = IpWhitelistScope.GLOBAL;
            type = IpWhitelistType.determineFromIpAddress(ipAddress);
        } else if (scope == IpWhitelistScope.ROLE_SPECIFIC && !hasRoles) {
            scope = IpWhitelistScope.GLOBAL;
            type = IpWhitelistType.determineFromIpAddress(ipAddress);
        }
    }

    /**
     * Get a summary description of this whitelist entry
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("IP: ").append(ipAddress);
        summary.append(", Scope: ").append(scope.getDescription());
        summary.append(", Type: ").append(type.getDisplayName());
        summary.append(", Active: ").append(isActive);

        if (allowsSpecificUsers() && allowedUserIds != null && !allowedUserIds.isEmpty()) {
            summary.append(", Users: ").append(allowedUserIds.size());
        }
        if (allowsSpecificRoles() && allowedRoleIds != null && !allowedRoleIds.isEmpty()) {
            summary.append(", Roles: ").append(allowedRoleIds.size());
        }

        return summary.toString();
    }

    /**
     * Check if this entry has any access restrictions
     */
    public boolean hasAccessRestrictions() {
        return (allowedUserIds != null && !allowedUserIds.isEmpty()) ||
                (allowedRoleIds != null && !allowedRoleIds.isEmpty());
    }

    /**
     * Get total number of entities (users + roles) that have access
     */
    public int getAccessEntityCount() {
        int count = 0;
        if (allowedUserIds != null) {
            count += allowedUserIds.size();
        }
        if (allowedRoleIds != null) {
            count += allowedRoleIds.size();
        }
        return count;
    }

    public void setCreatedDate(LocalDateTime now) {
    }
}