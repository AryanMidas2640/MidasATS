package com.midas.consulting.security.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.midas.consulting.security.SecurityConstants;
import com.midas.consulting.service.ValidationResult;
import com.midas.consulting.service.WhitelistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.*;


public class IpWhitelistFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistFilter.class);

    private final WhitelistService ipWhitelistService;
    private final Cache<String, ValidationResult> ipCache;

    // Endpoints that should skip IP whitelist check
    private static final Set<String> SKIP_WHITELIST_PATHS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    "/health",
                    "/actuator",
                    "/swagger",
                    "/api-docs",
                    "/auth/login",
                    "/auth/refresh",
                    "/public"
            ))
    );

    @Autowired
    public IpWhitelistFilter(WhitelistService ipWhitelistService) {
        this.ipWhitelistService = ipWhitelistService;

        // Initialize cache for performance (5 minute TTL)
        this.ipCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofSeconds(SecurityConstants.IP_VALIDATION_CACHE_TTL != 0
                        ? SecurityConstants.IP_VALIDATION_CACHE_TTL : 300))
                .build();
    }

//    @Override
//    public void init(FilterConfig filterConfig) throws ServletException {
//        logger.info("Initializing IP Whitelist Filter");
//    }
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
//            throws IOException, ServletException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        // Skip whitelist check for certain endpoints
//        String requestURI = httpRequest.getRequestURI();
//        if (shouldSkipWhitelistCheck(requestURI)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        String clientIp = getClientIpAddress(httpRequest);
//        String tenantId = httpRequest.getHeader("X-Tenant");
//
//        // Skip check if no tenant ID (might be a system call)
//        if (!StringUtils.hasText(tenantId)) {
//            logger.debug("No tenant ID found, skipping IP whitelist check for URI: {}", requestURI);
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        try {
//            // Extract user context from JWT token
//            UserContext userContext = extractUserContext(httpRequest);
//            String userId = userContext != null ? userContext.getUserId() : null;
//            Set<String> userRoles = userContext != null ? userContext.getRoles() : Collections.emptySet();
//
//            // Check cache first
//            String cacheKey = buildCacheKey(clientIp, tenantId, userId, userRoles);
//            ValidationResult cachedResult = ipCache.getIfPresent(cacheKey);
//
//            ValidationResult validationResult;
//            if (cachedResult != null) {
//                validationResult = cachedResult;
//                logger.debug("Using cached validation result for IP {} and tenant {}", clientIp, tenantId);
//            } else {
//                // Perform validation
//                validationResult = ipWhitelistService.validateUserIpAccess(clientIp, tenantId, userId, userRoles);
//
//                // Cache successful results only (failures shouldn't be cached to allow retry)
//                if (validationResult.isAllowed()) {
//                    ipCache.put(cacheKey, validationResult);
//                }
//            }
//
//            if (validationResult.isAllowed()) {
//                // Add enhanced headers for downstream processing
//                HttpServletRequestWrapper requestWrapper = createEnhancedRequestWrapper(
//                        httpRequest, clientIp, validationResult
//                );
//
//                logger.debug("IP {} allowed for tenant {} on URI {} via {} scope",
//                        clientIp, tenantId, requestURI, validationResult.getScope());
//
//                filterChain.doFilter(requestWrapper, response);
//            } else {
//                logger.warn("IP {} blocked for tenant {} on URI {} - Reason: {}",
//                        clientIp, tenantId, requestURI, validationResult.getMessage());
//                sendAccessDeniedResponse(httpResponse, validationResult);
//            }
//
//        } catch (Exception e) {
//            logger.error("Error checking IP whitelist for IP {} and tenant {}: {}",
//                    clientIp, tenantId, e.getMessage(), e);
//
//            // Decide whether to fail-open or fail-closed based on configuration
//            if (shouldFailOpen()) {
//                logger.warn("Failing open due to IP whitelist service error");
//                filterChain.doFilter(request, response);
//            } else {
//                sendServiceUnavailableResponse(httpResponse, "IP validation service temporarily unavailable");
//            }
//        }
//    }



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String clientIp = extractClientIpAddress(httpRequest);
        String tenantId = httpRequest.getHeader(SecurityConstants.HEADER_TENANT_STRING);

        // Your existing filter logic here...
        // Make sure to call filterChain.doFilter(request, response) at the end
        filterChain.doFilter(request, response);
    }


    @Override
    public void destroy() {
        logger.info("Destroying IP Whitelist Filter");
        if (ipCache != null) {
            ipCache.invalidateAll();
        }
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private boolean shouldSkipWhitelistCheck(String requestURI) {
        if (!StringUtils.hasText(requestURI)) {
            return false;
        }

        return SKIP_WHITELIST_PATHS.stream()
                .anyMatch(path -> requestURI.startsWith(path));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        // Check various headers in order of preference
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "X-Originating-IP",
                "CF-Connecting-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
                // Take the first IP if there are multiple (comma-separated)
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }

                // Validate IP format
                if (isValidIpAddress(ip)) {
                    return ip;
                }
            }
        }

        // Fall back to remote address
        String remoteAddr = request.getRemoteAddr();
        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
    }

    private boolean isValidIpAddress(String ip) {
        if (!StringUtils.hasText(ip)) {
            return false;
        }

        // Basic IPv4 validation
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        // Basic IPv6 validation (simplified)
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$";

        return ip.matches(ipv4Pattern) || ip.matches(ipv6Pattern);
    }

    private UserContext extractUserContext(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader(SecurityConstants.HEADER_STRING != null
                    ? SecurityConstants.HEADER_STRING : "Authorization");

            if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX != null
                    ? SecurityConstants.TOKEN_PREFIX : "Bearer ")) {

                String token = authHeader.replace(SecurityConstants.TOKEN_PREFIX != null
                        ? SecurityConstants.TOKEN_PREFIX : "Bearer ", "");

                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.getSecretBytes() != null
                                ? SecurityConstants.getSecretBytes() : "defaultSecret".getBytes())
                        .parseClaimsJws(token)
                        .getBody();

                String userId = claims.getSubject();
                String tenantId = claims.get("tenantId", String.class);
                String email = claims.get("email", String.class);

                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);
                Set<String> roleSet = roles != null ? new HashSet<>(roles) : Collections.emptySet();

                return new UserContext(userId, roleSet, tenantId, email);
            }
        } catch (Exception e) {
            logger.debug("Could not extract user context from token: {}", e.getMessage());
        }
        return null;
    }

    private String buildCacheKey(String clientIp, String tenantId, String userId, Set<String> userRoles) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(clientIp).append(":").append(tenantId);

        if (StringUtils.hasText(userId)) {
            keyBuilder.append(":").append(userId);
        }

        if (userRoles != null && !userRoles.isEmpty()) {
            List<String> sortedRoles = new ArrayList<>(userRoles);
            Collections.sort(sortedRoles);
            keyBuilder.append(":").append(String.join(",", sortedRoles));
        }

        return keyBuilder.toString();
    }

    private HttpServletRequestWrapper createEnhancedRequestWrapper(
            HttpServletRequest originalRequest, String clientIp, ValidationResult validationResult) {

        return new HttpServletRequestWrapper(originalRequest) {
            @Override
            public String getHeader(String name) {
                switch (name) {
                    case SecurityConstants.HEADER_CLIENT_IP : ;//!= null ? SecurityConstants.HEADER_CLIENT_IP : "X-Client-IP":
                        return clientIp;
                    case "X-IP-Whitelist-Scope":
                        return validationResult.getScope() != null ? validationResult.getScope().name() : null;
                    case "X-IP-Whitelist-Matched":
                        return "true";
                    case "X-IP-Whitelist-Entry-ID":
                        return validationResult.getMatchedEntry() != null ?
                                validationResult.getMatchedEntry().getId() : null;
                    default:
                        return super.getHeader(name);
                }
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> headerNames = new HashSet<>();
                Enumeration<String> originalHeaders = super.getHeaderNames();
                while (originalHeaders.hasMoreElements()) {
                    headerNames.add(originalHeaders.nextElement());
                }

                // Add our custom headers
                headerNames.add(SecurityConstants.HEADER_CLIENT_IP != null ? SecurityConstants.HEADER_CLIENT_IP : "X-Client-IP");
                headerNames.add("X-IP-Whitelist-Scope");
                headerNames.add("X-IP-Whitelist-Matched");
                headerNames.add("X-IP-Whitelist-Entry-ID");

                return Collections.enumeration(headerNames);
            }
        };
    }

    private void sendAccessDeniedResponse(HttpServletResponse response, ValidationResult validationResult)
            throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        String jsonResponse = String.format(
                "{\"error\": \"IP_ACCESS_DENIED\", \"message\": \"%s\", \"errorCode\": \"%s\", \"timestamp\": \"%s\"}",
                validationResult.getMessage() != null ? validationResult.getMessage() : "IP address not whitelisted",
                validationResult.getErrorCode() != null ? validationResult.getErrorCode() : "ACCESS_DENIED",
                java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }

    private void sendServiceUnavailableResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType("application/json");

        String jsonResponse = String.format(
                "{\"error\": \"SERVICE_UNAVAILABLE\", \"message\": \"%s\", \"timestamp\": \"%s\"}",
                message,
                java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
    private String extractClientIpAddress(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp != null && clientIp.contains(",") ?
                clientIp.split(",")[0].trim() : clientIp;
    }


    private boolean shouldFailOpen() {
        // This should be configurable via application properties
        // For now, defaulting to fail-open to prevent lockouts during service issues
        return true;
    }
}
//package com.midas.consulting.security.api;
//
//// ============================================================================
//// COMPLETE IMPORT STATEMENTS FOR IP WHITELIST FEATURE
//// ============================================================================
//
//// 1. ENHANCED IP WHITELIST FILTER IMPORTS
//// ============================================================================
//
//
//// Caching
//
//import com.github.benmanes.caffeine.cache.Cache;
//import com.midas.consulting.security.SecurityConstants;
//import com.midas.consulting.service.WhitelistService;
//import com.midas.consulting.service.ValidationResult;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.util.StringUtils;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletRequestWrapper;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.time.Instant;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//
//public class IpWhitelistFilter extends OncePerRequestFilter {
//
//    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistFilter.class);
//
//    @Autowired
//    private WhitelistService ipWhitelistService;
//
//    @Autowired
//    private Cache<String, Boolean> ipCache;
//
//    public IpWhitelistFilter(WhitelistService ipWhitelistService) {
//        this.ipWhitelistService = ipWhitelistService;
//    }
//
//    public IpWhitelistFilter() {
//
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        String requestURI = httpRequest.getRequestURI();
//        String clientIp = extractClientIpAddress(httpRequest);
//        String tenantId = httpRequest.getHeader(SecurityConstants.HEADER_TENANT_STRING);
//
//        // Skip whitelist check for public endpoints
//        if (isPublicEndpoint(requestURI)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//
//        // Log the request for audit
//        logger.debug("IP whitelist check: IP={}, Tenant={}, URI={}", clientIp, tenantId, requestURI);
//
//        if (StringUtils.isEmpty(tenantId)) {
//            sendAccessDeniedResponse(httpResponse, "Missing tenant information");
//            return;
//        }
//
//        try {
//            // Check admin bypass
//            String bypassHeader = httpRequest.getHeader(SecurityConstants.IP_WHITELIST_BYPASS_HEADER);
//            if ("admin-override".equals(bypassHeader)) {
//                logger.warn("IP whitelist bypassed for IP {} with admin override", clientIp);
//                filterChain.doFilter(request, response);
//                return;
//            }
//
//            // Extract user context from JWT if available
//            UserContext userContext = extractUserContext(httpRequest);
//            String userId = userContext != null ? userContext.getUserId() : null;
//            Set<String> userRoles = userContext != null ? userContext.getRoles() : Collections.emptySet();
//
//            // Enhanced validation with user context
//            ValidationResult validationResult = ipWhitelistService.validateUserIpAccess(
//                    clientIp, tenantId, userId, userRoles
//            );
//
//            if (validationResult.isAllowed()) {
//                // Add enhanced headers for downstream processing
//                HttpServletRequestWrapper requestWrapper = createEnhancedRequestWrapper(
//                        httpRequest, clientIp, validationResult
//                );
//                filterChain.doFilter(requestWrapper, response);
//            } else {
//                logger.warn("IP {} blocked for tenant {} on URI {} - Reason: {}",
//                        clientIp, tenantId, requestURI, validationResult.getMessage());
//                sendAccessDeniedResponse(httpResponse, validationResult.getMessage());
//            }
//
//        } catch (Exception e) {
//            logger.error("Error checking IP whitelist for IP {} and tenant {}: {}",
//                    clientIp, tenantId, e.getMessage());
//            // Fallback: allow access to prevent lockout
//            filterChain.doFilter(request, response);
//        }
//    }
//
//    private UserContext extractUserContext(HttpServletRequest request) {
//        try {
//            String authHeader = request.getHeader(SecurityConstants.HEADER_STRING);
//            if (authHeader != null && authHeader.startsWith(SecurityConstants.TOKEN_PREFIX)) {
//                String token = authHeader.replace(SecurityConstants.TOKEN_PREFIX, "");
//                Claims claims = Jwts.parser()
//                        .setSigningKey(SecurityConstants.getSecretBytes())
//                        .parseClaimsJws(token)
//                        .getBody();
//
//                String userId = claims.getSubject();
//                List<String> roles = claims.get("roles", List.class);
//                Set<String> roleSet = roles != null ? new HashSet<>(roles) : Collections.emptySet();
//
//                return new UserContext(userId, roleSet);
//            }
//        } catch (Exception e) {
//            logger.debug("Could not extract user context: {}", e.getMessage());
//        }
//        return null;
//    }
//
//    private HttpServletRequestWrapper createEnhancedRequestWrapper(
//            HttpServletRequest originalRequest, String clientIp, ValidationResult validationResult) {
//
//        return new HttpServletRequestWrapper(originalRequest) {
//            @Override
//            public String getHeader(String name) {
//                switch (name) {
//                    case SecurityConstants.HEADER_CLIENT_IP:
//                        return clientIp;
//                    case "X-IP-Whitelist-Scope":
//                        return validationResult.getScope() != null ?
//                                validationResult.getScope().name() : "NONE";
//                    case "X-IP-Whitelist-Entry-ID":
//                        return validationResult.getMatchedEntry() != null ?
//                                validationResult.getMatchedEntry().getId() : null;
//                    default:
//                        return super.getHeader(name);
//                }
//            }
//        };
//    }
//
//    private boolean isPublicEndpoint(String requestURI) {
//        return requestURI.startsWith("/api/v1/user/signup") ||
//                requestURI.startsWith("/api/v1/user/authenticate") ||
//                requestURI.startsWith("/health") ||
//                requestURI.startsWith("/actuator") ||
//                requestURI.startsWith("/swagger") ||
//                requestURI.startsWith("/v3/api-docs");
//    }
//
//    private void sendAccessDeniedResponse(HttpServletResponse response, String message)
//            throws IOException {
//        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//        response.setContentType("application/json");
//        response.getWriter().write(String.format(
//                "{\"error\":\"Access Denied\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
//                message, Instant.now()
//        ));
//    }
//
//    private String extractClientIpAddress(HttpServletRequest request) {
//        String clientIp = request.getHeader("X-Forwarded-For");
//        if (clientIp == null || clientIp.isEmpty()) {
//            clientIp = request.getHeader("X-Real-IP");
//        }
//        if (clientIp == null || clientIp.isEmpty()) {
//            clientIp = request.getRemoteAddr();
//        }
//        return clientIp != null && clientIp.contains(",") ?
//                clientIp.split(",")[0].trim() : clientIp;
//    }
//
//    // Inner class for user context
//    private static class UserContext {
//        private final String userId;
//        private final Set<String> roles;
//
//        public UserContext(String userId, Set<String> roles) {
//            this.userId = userId;
//            this.roles = roles;
//        }
//
//        public String getUserId() { return userId; }
//        public Set<String> getRoles() { return roles; }
//    }
//}
//
////package com.midas.consulting.security.api;
////
////import com.github.benmanes.caffeine.cache.Cache;
////import com.github.benmanes.caffeine.cache.Caffeine;
////import com.midas.consulting.security.SecurityConstants;
////import com.midas.consulting.service.IpWhitelistService;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.http.HttpStatus;
////import org.springframework.util.StringUtils;
////
////import javax.servlet.*;
////import javax.servlet.http.HttpServletRequest;
////import javax.servlet.http.HttpServletRequestWrapper;
////import javax.servlet.http.HttpServletResponse;
////import java.io.IOException;
////import java.net.InetAddress;
////import java.time.Duration;
////import java.time.Instant;
////
////// Removed @Component and @Order annotations - this is now managed by Spring Security config
////public class IpWhitelistFilter implements Filter {
////
////    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistFilter.class);
////
////    private final IpWhitelistService ipWhitelistService;
////    private final Cache<String, Boolean> ipCache;
////
////    public IpWhitelistFilter(IpWhitelistService ipWhitelistService) {
////        this.ipWhitelistService = ipWhitelistService;
////
////        // Initialize cache for performance (5 minute TTL)
////        this.ipCache = Caffeine.newBuilder()
////                .maximumSize(1000)
////                .expireAfterWrite(Duration.ofSeconds(SecurityConstants.IP_VALIDATION_CACHE_TTL))
////                .build();
////    }
////
////    @Override
////    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
////            throws IOException, ServletException {
////
////        HttpServletRequest httpRequest = (HttpServletRequest) request;
////        HttpServletResponse httpResponse = (HttpServletResponse) response;
////
////        // Skip whitelist check for certain endpoints
////        String requestURI = httpRequest.getRequestURI();
////        if (shouldSkipWhitelistCheck(requestURI)) {
////            chain.doFilter(request, response);
////            return;
////        }
////
////        String clientIp = getClientIpAddress(httpRequest);
////        String tenantId = httpRequest.getHeader(SecurityConstants.HEADER_TENANT_STRING);
////
////        // Log the request for audit purposes
////        logger.debug("IP whitelist check: IP={}, Tenant={}, URI={}", clientIp, tenantId, requestURI);
////
////        if (StringUtils.isEmpty(tenantId)) {
////            // No tenant header, skip whitelist check for public endpoints
////            if (isPublicEndpoint(requestURI)) {
////                chain.doFilter(request, response);
////                return;
////            }
////
////            // For non-public endpoints without tenant, block request
////            sendAccessDeniedResponse(httpResponse, "Missing tenant information");
////            return;
////        }
////
////        try {
////            // Check admin bypass header (for emergency access)
////            String bypassHeader = httpRequest.getHeader(SecurityConstants.IP_WHITELIST_BYPASS_HEADER);
////            if ("admin-override".equals(bypassHeader)) {
////                // Log the bypass for security audit
////                logger.warn("IP whitelist bypassed for IP {} with admin override", clientIp);
////                chain.doFilter(request, response);
////                return;
////            }
////
////            // Check cache first for performance
////            String cacheKey = tenantId + ":" + clientIp;
////            Boolean cachedResult = ipCache.getIfPresent(cacheKey);
////
////            boolean isAllowed;
////            if (cachedResult != null) {
////                isAllowed = cachedResult;
////                logger.debug("Using cached result for IP {}: {}", clientIp, isAllowed);
////            } else {
////                isAllowed = ipWhitelistService.isIpWhitelisted(clientIp, tenantId);
////                ipCache.put(cacheKey, isAllowed);
////                logger.debug("Computed and cached result for IP {}: {}", clientIp, isAllowed);
////            }
////
////            if (isAllowed) {
////                // Add client IP to request headers for downstream processing
////                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
////                    @Override
////                    public String getHeader(String name) {
////                        if (SecurityConstants.HEADER_CLIENT_IP.equals(name)) {
////                            return clientIp;
////                        }
////                        return super.getHeader(name);
////                    }
////                };
////
////                chain.doFilter(requestWrapper, response);
////            } else {
////                logger.warn("IP {} blocked for tenant {} on URI {}", clientIp, tenantId, requestURI);
////                sendAccessDeniedResponse(httpResponse, "Your IP address is not whitelisted");
////            }
////
////        } catch (Exception e) {
////            logger.error("Error checking IP whitelist for IP {} and tenant {}: {}",
////                    clientIp, tenantId, e.getMessage());
////
////            // In case of error, allow access to prevent system lockout
////            // But log the incident for investigation
////            logger.error("IP whitelist check failed, allowing access as fallback", e);
////            chain.doFilter(request, response);
////        }
////    }
////
////    private boolean shouldSkipWhitelistCheck(String requestURI) {
////        // Skip whitelist check for public endpoints and health checks
////        return requestURI.startsWith("/api/v1/user/authenticate") ||
////                requestURI.startsWith("/v2/api-docs") ||
////                requestURI.startsWith("/swagger-resources") ||
////                requestURI.startsWith("/webjars/") ||
////                requestURI.startsWith("/swagger-ui") ||
////                requestURI.startsWith("/api/v1/user/signup") ||
////                requestURI.startsWith("/swagger") ||
////                requestURI.startsWith("/actuator/health") ||
////                requestURI.startsWith("/static") ||
////                requestURI.startsWith("/css") ||
////                requestURI.startsWith("/js") ||
////                requestURI.startsWith("/images") ||
////                requestURI.equals("/favicon.ico");
////    }
////
////    private boolean isPublicEndpoint(String requestURI) {
////        return requestURI.startsWith("/api/v1/user/authenticate") ||
////                requestURI.startsWith("/api/v1/user/signup") ||
////                requestURI.startsWith("/api/v1/notifications");
////    }
////
////    private String getClientIpAddress(HttpServletRequest request) {
////        // Check various headers for the real client IP
////        String[] headers = {
////                "X-Forwarded-For",
////                "X-Real-IP",
////                "X-Client-IP",
////                "CF-Connecting-IP", // Cloudflare
////                "True-Client-IP",   // Akamai
////                "X-Cluster-Client-IP"
////        };
////
////        for (String header : headers) {
////            String ip = request.getHeader(header);
////            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
////                // X-Forwarded-For can contain multiple IPs, take the first one
////                if (ip.contains(",")) {
////                    ip = ip.split(",")[0].trim();
////                }
////
////                // Validate IP format
////                if (isValidIpAddress(ip)) {
////                    return ip;
////                }
////            }
////        }
////
////        // Fallback to remote address
////        String remoteAddr = request.getRemoteAddr();
////        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
////    }
////
////    private boolean isValidIpAddress(String ip) {
////        try {
////            InetAddress.getByName(ip);
////            return true;
////        } catch (Exception e) {
////            return false;
////        }
////    }
////
////    private void sendAccessDeniedResponse(HttpServletResponse response, String message)
////            throws IOException {
////        response.setStatus(HttpStatus.FORBIDDEN.value());
////        response.setContentType("application/json");
////        response.setCharacterEncoding("UTF-8");
////
////        String jsonResponse = String.format(
////                "{\"error\":\"Access Denied\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
////                message,
////                Instant.now().toString()
////        );
////
////        response.getWriter().write(jsonResponse);
////    }
////}
////
//////package com.midas.consulting.security.api;
//////
//////import com.github.benmanes.caffeine.cache.Cache;
//////import com.github.benmanes.caffeine.cache.Caffeine;
//////import com.midas.consulting.security.SecurityConstants;
//////import com.midas.consulting.service.IpWhitelistService;
//////import org.slf4j.Logger;
//////import org.slf4j.LoggerFactory;
//////import org.springframework.beans.factory.annotation.Autowired;
//////import org.springframework.http.HttpStatus;
//////import org.springframework.util.StringUtils;
//////
//////import javax.servlet.*;
//////import javax.servlet.http.HttpServletRequest;
//////import javax.servlet.http.HttpServletRequestWrapper;
//////import javax.servlet.http.HttpServletResponse;
//////import java.io.IOException;
//////import java.net.InetAddress;
//////import java.time.Duration;
//////import java.time.Instant;
//////
//////
//////public class IpWhitelistFilter implements Filter {
//////
//////    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistFilter.class);
//////
//////    private final IpWhitelistService ipWhitelistService;
//////    private final Cache<String, Boolean> ipCache;
//////
//////    @Autowired
//////    public IpWhitelistFilter(IpWhitelistService ipWhitelistService) {
//////        this.ipWhitelistService = ipWhitelistService;
//////
//////        // Initialize cache for performance (5 minute TTL)
//////        this.ipCache = Caffeine.newBuilder()
//////                .maximumSize(1000)
//////                .expireAfterWrite(Duration.ofSeconds(SecurityConstants.IP_VALIDATION_CACHE_TTL))
//////                .build();
//////    }
//////
//////    @Override
//////    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//////            throws IOException, ServletException {
//////
//////        HttpServletRequest httpRequest = (HttpServletRequest) request;
//////        HttpServletResponse httpResponse = (HttpServletResponse) response;
//////
//////        // Skip whitelist check for certain endpoints
//////        String requestURI = httpRequest.getRequestURI();
//////        if (shouldSkipWhitelistCheck(requestURI)) {
//////            chain.doFilter(request, response);
//////            return;
//////        }
//////
//////        String clientIp = getClientIpAddress(httpRequest);
//////        String tenantId = httpRequest.getHeader(SecurityConstants.HEADER_TENANT_STRING);
//////
//////        // Log the request for audit purposes
//////        logger.debug("IP whitelist check: IP={}, Tenant={}, URI={}", clientIp, tenantId, requestURI);
//////
//////        if (StringUtils.isEmpty(tenantId)) {
//////            // No tenant header, skip whitelist check for public endpoints
//////            if (isPublicEndpoint(requestURI)) {
//////                chain.doFilter(request, response);
//////                return;
//////            }
//////
//////            // For non-public endpoints without tenant, block request
//////            sendAccessDeniedResponse(httpResponse, "Missing tenant information");
//////            return;
//////        }
//////
//////        try {
//////            // Check admin bypass header (for emergency access)
//////            String bypassHeader = httpRequest.getHeader(SecurityConstants.IP_WHITELIST_BYPASS_HEADER);
//////            if ("admin-override".equals(bypassHeader)) {
//////                // Log the bypass for security audit
//////                logger.warn("IP whitelist bypassed for IP {} with admin override", clientIp);
//////                chain.doFilter(request, response);
//////                return;
//////            }
//////
//////            // Check cache first for performance
//////            String cacheKey = tenantId + ":" + clientIp;
//////            Boolean cachedResult = ipCache.getIfPresent(cacheKey);
//////
//////            boolean isAllowed;
//////            if (cachedResult != null) {
//////                isAllowed = cachedResult;
//////                logger.debug("Using cached result for IP {}: {}", clientIp, isAllowed);
//////            } else {
//////                isAllowed = ipWhitelistService.isIpWhitelisted(clientIp, tenantId);
//////                ipCache.put(cacheKey, isAllowed);
//////                logger.debug("Computed and cached result for IP {}: {}", clientIp, isAllowed);
//////            }
//////
//////            if (isAllowed) {
//////                // Add client IP to request headers for downstream processing
//////                HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
//////                    @Override
//////                    public String getHeader(String name) {
//////                        if (SecurityConstants.HEADER_CLIENT_IP.equals(name)) {
//////                            return clientIp;
//////                        }
//////                        return super.getHeader(name);
//////                    }
//////                };
//////
//////                chain.doFilter(requestWrapper, response);
//////            } else {
//////                logger.warn("IP {} blocked for tenant {} on URI {}", clientIp, tenantId, requestURI);
//////                sendAccessDeniedResponse(httpResponse, "Your IP address is not whitelisted");
//////            }
//////
//////        } catch (Exception e) {
//////            logger.error("Error checking IP whitelist for IP {} and tenant {}: {}",
//////                        clientIp, tenantId, e.getMessage());
//////
//////            // In case of error, allow access to prevent system lockout
//////            // But log the incident for investigation
//////            logger.error("IP whitelist check failed, allowing access as fallback", e);
//////            chain.doFilter(request, response);
//////        }
//////    }
//////
//////    private boolean shouldSkipWhitelistCheck(String requestURI) {
//////        // Skip whitelist check for public endpoints and health checks
//////        return requestURI.startsWith("/api/v1/user/authenticate") ||
//////                requestURI.startsWith("/v2/api-docs") ||             // <-- ADD THIS
//////                requestURI.startsWith("/swagger-resources") ||       // <-- ADD THIS
//////                requestURI.startsWith("/webjars/") ||                // <-- ADD THIS
//////                requestURI.startsWith("/swagger-ui") ||              // <-- ADD THIS (in case of SpringFox 3)
//////                requestURI.startsWith("/api/v1/user/signup") ||
//////               requestURI.startsWith("/swagger") ||
//////               requestURI.startsWith("/actuator/health") ||
//////               requestURI.startsWith("/static") ||
//////               requestURI.startsWith("/css") ||
//////               requestURI.startsWith("/js") ||
//////               requestURI.startsWith("/images") ||
//////               requestURI.equals("/favicon.ico");
//////    }
//////
//////    private boolean isPublicEndpoint(String requestURI) {
//////        return requestURI.startsWith("/api/v1/user/authenticate") ||
//////               requestURI.startsWith("/api/v1/user/signup") ||
//////               requestURI.startsWith("/api/v1/notifications");
//////    }
//////
//////    private String getClientIpAddress(HttpServletRequest request) {
//////        // Check various headers for the real client IP
//////        String[] headers = {
//////            "X-Forwarded-For",
//////            "X-Real-IP",
//////            "X-Client-IP",
//////            "CF-Connecting-IP", // Cloudflare
//////            "True-Client-IP",   // Akamai
//////            "X-Cluster-Client-IP"
//////        };
//////
//////        for (String header : headers) {
//////            String ip = request.getHeader(header);
//////            if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
//////                // X-Forwarded-For can contain multiple IPs, take the first one
//////                if (ip.contains(",")) {
//////                    ip = ip.split(",")[0].trim();
//////                }
//////
//////                // Validate IP format
//////                if (isValidIpAddress(ip)) {
//////                    return ip;
//////                }
//////            }
//////        }
//////
//////        // Fallback to remote address
//////        String remoteAddr = request.getRemoteAddr();
//////        return StringUtils.hasText(remoteAddr) ? remoteAddr : "unknown";
//////    }
//////
//////    private boolean isValidIpAddress(String ip) {
//////        try {
//////            InetAddress.getByName(ip);
//////            return true;
//////        } catch (Exception e) {
//////            return false;
//////        }
//////    }
//////
//////    private void sendAccessDeniedResponse(HttpServletResponse response, String message)
//////            throws IOException {
//////        response.setStatus(HttpStatus.FORBIDDEN.value());
//////        response.setContentType("application/json");
//////        response.setCharacterEncoding("UTF-8");
//////
//////        String jsonResponse = String.format(
//////            "{\"error\":\"Access Denied\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
//////            message,
//////            Instant.now().toString()
//////        );
//////
//////        response.getWriter().write(jsonResponse);
//////    }
//////}
