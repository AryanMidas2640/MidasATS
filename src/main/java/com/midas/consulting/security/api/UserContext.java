package com.midas.consulting.security.api;

import java.util.Set;
import java.util.Collections;

/**
 * Context class to hold user information extracted from JWT tokens
 * Used by IP whitelist filter for user-specific validations
 */
public class UserContext {
    private final String userId;
    private final Set<String> roles;
    private final String tenantId;
    private final String email;

    public UserContext(String userId, Set<String> roles) {
        this(userId, roles, null, null);
    }

    public UserContext(String userId, Set<String> roles, String tenantId, String email) {
        this.userId = userId;
        this.roles = roles != null ? roles : Collections.emptySet();
        this.tenantId = tenantId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getEmail() {
        return email;
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(Set<String> requiredRoles) {
        if (requiredRoles == null || requiredRoles.isEmpty()) {
            return true;
        }
        return !Collections.disjoint(roles, requiredRoles);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("SUPERADMIN");
    }

    public boolean isSuperAdmin() {
        return hasRole("SUPERADMIN");
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "userId='" + userId + '\'' +
                ", roles=" + roles +
                ", tenantId='" + tenantId + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}