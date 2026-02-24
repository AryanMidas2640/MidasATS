package com.midas.consulting.model;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum defining the scope of IP whitelist entries
 * Determines the level and type of access control
 */
public enum IpWhitelistScope {
    /**
     * Global scope - applies to all tenants and users
     * Highest precedence, typically for system-wide IPs
     */
    GLOBAL("Global Access", "Applies to all tenants and users", 1000),

    /**
     * Admin scope - applies to admin operations only
     * High precedence, for administrative access
     */
    ADMIN("Admin Access", "Applies to admin operations only", 900),

    /**
     * Tenant scope - applies to all users within a tenant
     * Default scope for most entries
     */
    TENANT("Tenant Access", "Applies to all users within tenant", 500),

    /**
     * Hybrid scope - applies to specific users AND roles
     * Flexible scope combining user and role restrictions
     */
    HYBRID("Hybrid Access", "Applies to specific users and roles", 300),

    /**
     * Role-specific scope - applies to specific roles only
     * Medium precedence, for role-based restrictions
     */
    ROLE_SPECIFIC("Role-Specific Access", "Applies to specific roles only", 250),

    /**
     * User-specific scope - applies to specific users only
     * Lower precedence, for individual user restrictions
     */
    USER_SPECIFIC("User-Specific Access", "Applies to specific users only", 200);

    private final String displayName;
    private final String description;
    private final int precedence;

    IpWhitelistScope(String displayName, String description, int precedence) {
        this.displayName = displayName;
        this.description = description;
        this.precedence = precedence;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getPrecedence() {
        return precedence;
    }

    /**
     * Get priority order (alias for precedence to maintain compatibility)
     */
    public int getPriorityOrder() {
        return precedence;
    }

    // ============================================================================
    // SCOPE CAPABILITY CHECKS
    // ============================================================================

    /**
     * Check if this scope allows specific users
     */
    public boolean allowsSpecificUsers() {
        return this == USER_SPECIFIC || this == HYBRID;
    }

    /**
     * Check if this scope allows specific roles
     */
    public boolean allowsSpecificRoles() {
        return this == ROLE_SPECIFIC || this == HYBRID;
    }

    /**
     * Check if this is a global scope
     */
    public boolean isGlobal() {
        return this == GLOBAL;
    }

    /**
     * Check if this scope requires user IDs
     */
    public boolean requiresUserIds() {
        return this == USER_SPECIFIC || this == HYBRID;
    }

    /**
     * Check if this scope requires role IDs
     */
    public boolean requiresRoleIds() {
        return this == ROLE_SPECIFIC || this == HYBRID;
    }

    /**
     * Check if this scope allows tenant-wide access
     */
    public boolean allowsTenantAccess() {
        return this == TENANT || this == GLOBAL || this == ADMIN;
    }

    /**
     * Check if this scope is administrative
     */
    public boolean isAdministrative() {
        return this == ADMIN || this == GLOBAL;
    }

    /**
     * Check if this scope requires explicit user or role assignment
     */
    public boolean requiresExplicitAssignment() {
        return this == USER_SPECIFIC || this == ROLE_SPECIFIC || this == HYBRID;
    }

    /**
     * Check if this scope is restrictive (not tenant-wide)
     */
    public boolean isRestrictive() {
        return !allowsTenantAccess();
    }

    // ============================================================================
    // UTILITY AND COMPARISON METHODS
    // ============================================================================

    /**
     * Get the highest precedence scope from a collection
     */
    public static IpWhitelistScope getHighestPrecedence(IpWhitelistScope... scopes) {
        IpWhitelistScope highest = null;
        for (IpWhitelistScope scope : scopes) {
            if (scope != null && (highest == null || scope.precedence > highest.precedence)) {
                highest = scope;
            }
        }
        return highest;
    }

    /**
     * Get the lowest precedence scope from a collection
     */
    public static IpWhitelistScope getLowestPrecedence(IpWhitelistScope... scopes) {
        IpWhitelistScope lowest = null;
        for (IpWhitelistScope scope : scopes) {
            if (scope != null && (lowest == null || scope.precedence < lowest.precedence)) {
                lowest = scope;
            }
        }
        return lowest;
    }

    /**
     * Get all scopes sorted by precedence (highest first)
     */
    public static List<IpWhitelistScope> getAllByPrecedence() {
        return Arrays.stream(values())
                .sorted(Comparator.comparingInt(IpWhitelistScope::getPrecedence).reversed())
                .collect(Collectors.toList());
//        ();
    }

    /**
     * Get all restrictive scopes (those that require explicit assignment)
     */
    public static List<IpWhitelistScope> getRestrictiveScopes() {
        return Arrays.stream(values())
                .filter(IpWhitelistScope::isRestrictive)
                .sorted(Comparator.comparingInt(IpWhitelistScope::getPrecedence).reversed())
                .collect(Collectors.toList())
;
    }

    /**
     * Get all tenant-wide scopes
     */
    public static List<IpWhitelistScope> getTenantWideScopes() {
        return Arrays.stream(values())
                .filter(IpWhitelistScope::allowsTenantAccess)
                .sorted(Comparator.comparingInt(IpWhitelistScope::getPrecedence).reversed())
                .collect(Collectors.toList())
;
    }

    // ============================================================================
    // VALIDATION METHODS
    // ============================================================================

    /**
     * Validate scope compatibility with user/role IDs
     */
    public boolean isCompatibleWith(boolean hasUserIds, boolean hasRoleIds) {
        switch (this) {
            case USER_SPECIFIC:
                return hasUserIds && !hasRoleIds;
            case ROLE_SPECIFIC:
                return !hasUserIds && hasRoleIds;
            case HYBRID:
                return hasUserIds || hasRoleIds; // At least one required
            case TENANT:
            case ADMIN:
            case GLOBAL:
                return true; // Can have or not have user/role restrictions
            default:
                return false;
        }
    }

    /**
     * Validate that the provided user/role configuration is valid for this scope
     */
    public ValidationResult validateConfiguration(boolean hasUserIds, boolean hasRoleIds) {
        switch (this) {
            case USER_SPECIFIC:
                if (!hasUserIds) {
                    return ValidationResult.error("USER_SPECIFIC scope requires at least one user ID");
                }
                if (hasRoleIds) {
                    return ValidationResult.warning("USER_SPECIFIC scope should not have role IDs (they will be ignored)");
                }
                break;

            case ROLE_SPECIFIC:
                if (!hasRoleIds) {
                    return ValidationResult.error("ROLE_SPECIFIC scope requires at least one role ID");
                }
                if (hasUserIds) {
                    return ValidationResult.warning("ROLE_SPECIFIC scope should not have user IDs (they will be ignored)");
                }
                break;

            case HYBRID:
                if (!hasUserIds && !hasRoleIds) {
                    return ValidationResult.error("HYBRID scope requires at least one user ID or role ID");
                }
                break;

            case TENANT:
            case ADMIN:
            case GLOBAL:
                if (hasUserIds || hasRoleIds) {
                    return ValidationResult.info("Tenant-wide scope with user/role restrictions will apply additional filtering");
                }
                break;

            default:
                return ValidationResult.error("Unknown scope: " + this);
        }

        return ValidationResult.success("Scope configuration is valid");
    }

    /**
     * Get recommended scope based on user/role configuration
     */
    public static IpWhitelistScope recommendScope(boolean hasUserIds, boolean hasRoleIds) {
        if (hasUserIds && hasRoleIds) {
            return HYBRID;
        } else if (hasUserIds) {
            return USER_SPECIFIC;
        } else if (hasRoleIds) {
            return ROLE_SPECIFIC;
        } else {
            return TENANT; // Default for no restrictions
        }
    }

    /**
     * Check if this scope takes precedence over another
     */
    public boolean takesPrecedenceOver(IpWhitelistScope other) {
        return other != null && this.precedence > other.precedence;
    }

    /**
     * Get the effective scope when multiple scopes conflict
     * Returns the scope with highest precedence
     */
    public static IpWhitelistScope resolveConflict(IpWhitelistScope... scopes) {
        return getHighestPrecedence(scopes);
    }

    // ============================================================================
    // DISPLAY AND FORMATTING METHODS
    // ============================================================================

    /**
     * Get a detailed description including precedence
     */
    public String getDetailedDescription() {
        return String.format("%s (Priority: %d) - %s", displayName, precedence, description);
    }

    /**
     * Get scope summary for logging/debugging
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append(" {");
        sb.append("precedence=").append(precedence);
        sb.append(", allowsUsers=").append(allowsSpecificUsers());
        sb.append(", allowsRoles=").append(allowsSpecificRoles());
        sb.append(", tenantWide=").append(allowsTenantAccess());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return displayName + " (" + name() + ")";
    }

    // ============================================================================
    // VALIDATION RESULT HELPER CLASS
    // ============================================================================


}