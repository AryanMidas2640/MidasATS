package com.midas.consulting.security.api;

import com.midas.consulting.security.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class to extract user context from JWT tokens
 */
@Component
public class JwtUserContextExtractor {
    private static final Logger logger = LoggerFactory.getLogger(JwtUserContextExtractor.class);

    /**
     * Extract user ID from JWT token in the request
     */
    public String extractUserId(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return null;
            }

            Claims claims = parseToken(token);
            if (claims == null) {
                return null;
            }

            // The subject usually contains the user ID or email
            String userId = claims.getSubject();
            logger.debug("Extracted user ID from token: {}", userId);
            return userId;

        } catch (Exception e) {
            logger.debug("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract user roles from JWT token in the request
     */
    public Set<String> extractUserRoles(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return Collections.emptySet();
            }

            Claims claims = parseToken(token);
            if (claims == null) {
                return Collections.emptySet();
            }

            // Extract roles from claims - adjust this based on your JWT structure
            Object rolesObj = claims.get("roles");
            if (rolesObj == null) {
                rolesObj = claims.get("authorities");
            }

            Set<String> roles = extractRolesFromClaims(rolesObj);
            logger.debug("Extracted user roles from token: {}", roles);
            return roles;

        } catch (Exception e) {
            logger.debug("Failed to extract user roles from token: {}", e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Extract user ID and roles together for efficiency
     */
    public UserContext extractUserContext(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) {
                return new UserContext(null, Collections.emptySet());
            }

            Claims claims = parseToken(token);
            if (claims == null) {
                return new UserContext(null, Collections.emptySet());
            }

            String userId = claims.getSubject();
            Object rolesObj = claims.get("roles");
            if (rolesObj == null) {
                rolesObj = claims.get("authorities");
            }

            Set<String> roles = extractRolesFromClaims(rolesObj);
            
            logger.debug("Extracted user context - ID: {}, Roles: {}", userId, roles);
            return new UserContext(userId, roles);

        } catch (Exception e) {
            logger.debug("Failed to extract user context from token: {}", e.getMessage());
            return new UserContext(null, Collections.emptySet());
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.HEADER_STRING);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * Parse JWT token and extract claims
     */
    private Claims parseToken(String token) {
        try {
            // Use the same secret used for signing tokens
            byte[] keyBytes = SecurityConstants.getSecretString().getBytes(StandardCharsets.UTF_8);
            
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
                    
        } catch (Exception e) {
            logger.debug("Failed to parse JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract roles from claims object (handles different formats)
     */
    @SuppressWarnings("unchecked")
    private Set<String> extractRolesFromClaims(Object rolesObj) {
        if (rolesObj == null) {
            return Collections.emptySet();
        }

        try {
            if (rolesObj instanceof List) {
                List<Object> rolesList = (List<Object>) rolesObj;
                return rolesList.stream()
                        .map(Object::toString)
                        .map(this::extractRoleId)
                        .collect(Collectors.toSet());
            } else if (rolesObj instanceof String) {
                // Single role as string
                Set<String> roles = new HashSet<>();
                roles.add(extractRoleId(rolesObj.toString()));
                return roles;

            }
        } catch (Exception e) {
            logger.debug("Failed to extract roles from claims object: {}", e.getMessage());
        }

        return Collections.emptySet();
    }

    /**
     * Extract role ID from role string (handles different formats)
     * Adjust this method based on how roles are stored in your JWT
     */
    private String extractRoleId(String roleStr) {
        if (roleStr == null) {
            return null;
        }

        // If roles are stored as objects with 'id' field: {"id": "roleId", "role": "ADMIN"}
        if (roleStr.contains("\"id\"")) {
            try {
                // Simple extraction - you might want to use a proper JSON parser
                int idStart = roleStr.indexOf("\"id\":\"") + 6;
                int idEnd = roleStr.indexOf("\"", idStart);
                if (idStart > 5 && idEnd > idStart) {
                    return roleStr.substring(idStart, idEnd);
                }
            } catch (Exception e) {
                logger.debug("Failed to extract role ID from JSON string: {}", e.getMessage());
            }
        }

        // If roles are stored as simple strings, return as-is
        return roleStr.trim();
    }

    /**
     * Data class to hold user context
     */
    public static class UserContext {
        private final String userId;
        private final Set<String> roles;

        public UserContext(String userId, Set<String> roles) {
            this.userId = userId;
            this.roles = roles != null ? roles : Collections.emptySet();
        }

        public String getUserId() {
            return userId;
        }

        public Set<String> getRoles() {
            return roles;
        }

        public boolean hasRole(String roleId) {
            return roles.contains(roleId);
        }

        public boolean hasAnyRole(Set<String> roleIds) {
            return roles.stream().anyMatch(roleIds::contains);
        }

        @Override
        public String toString() {
            return String.format("UserContext{userId='%s', roles=%s}", userId, roles);
        }
    }
}