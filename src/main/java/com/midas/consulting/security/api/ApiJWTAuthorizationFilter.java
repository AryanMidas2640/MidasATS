//package com.midas.consulting.security.api;
//
//import com.midas.consulting.security.SecurityConstants;
//import com.midas.consulting.service.TenantService;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
//
//import javax.servlet.FilterChain;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.ArrayList;
//
///**
// * Created by Dheeraj Singh.
// */
//public class ApiJWTAuthorizationFilter extends BasicAuthenticationFilter {
//
//    @Autowired
//private TenantService tenantService;
//    private static final String TENANT_HEADER = "X-Tenant";
////    private static final String DEFAULT_CONNECTION_STRING = "mongodb://admin:midasAdmin2203234@ec2-34-230-215-187.compute-1.amazonaws.com:27017/hrms-onprime?authSource=admin&maxPoolSize=50&minPoolSize=10&waitQueueTimeoutMS=3000";
////    private static final Logger logger = LoggerFactory.getLogger(TenantWithTokenFilter.class);
//
//
//    public ApiJWTAuthorizationFilter(AuthenticationManager authManager) {
//        super(authManager);
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest req,
//                                    HttpServletResponse res,
//                                    FilterChain chain) throws IOException, ServletException {
//        String header = req.getHeader(SecurityConstants.HEADER_STRING);
//        String headerTenantId = req.getHeader(SecurityConstants.HEADER_TENANT_STRING);
//        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
//            chain.doFilter(req, res);
//            return;
//        }
//        UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//        chain.doFilter(req, res);
//    }
//
//    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
//        String token = request.getHeader(SecurityConstants.HEADER_STRING);
//        String headerTenantId = request.getHeader(SecurityConstants.HEADER_TENANT_STRING);
//
//
//
//
//        if (token != null) {
//            Claims claims = Jwts.parser()
//                    .setSigningKey(SecurityConstants.SECRET)
//                    .parseClaimsJws(token.replace(SecurityConstants.TOKEN_PREFIX, ""))
//                    .getBody();
//            // Extract the UserName
//            String user = claims.getSubject();
//            // Extract the Roles
//            ArrayList<String> roles = (ArrayList<String>) claims.get("roles");
//            // Then convert Roles to GrantedAuthority Object for injecting
//            ArrayList<GrantedAuthority> list = new ArrayList<>();
//            if (roles != null) {
//                for (String a : roles) {
//                    GrantedAuthority g = new SimpleGrantedAuthority(a);
//                    list.add(g);
//                }
//            }
//            if (user != null) {
//                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken=  new UsernamePasswordAuthenticationToken(user, null, list);
//                // initialize the security context holder
//                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
//                return usernamePasswordAuthenticationToken;
//            }
//            return null;
//        }
//        return null;
//    }
//}


package com.midas.consulting.security.api;

import com.midas.consulting.security.SecurityConstants;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class ApiJWTAuthorizationFilter extends BasicAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiJWTAuthorizationFilter.class);

    public ApiJWTAuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {

        String header = req.getHeader(SecurityConstants.HEADER_STRING);
        String headerTenantId = req.getHeader(SecurityConstants.HEADER_TENANT_STRING);

        logger.debug("Processing JWT authorization for URI: {} with tenant: {}",
                req.getRequestURI(), headerTenantId);

        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            logger.debug("No JWT token found in request headers");
            chain.doFilter(req, res);
            return;
        }

        try {
            UsernamePasswordAuthenticationToken authentication = getAuthentication(req);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("JWT authentication successful for user: {}",
                    authentication != null ? authentication.getPrincipal() : "null");
        } catch (Exception e) {
            logger.error("JWT authentication failed: {}", e.getMessage());
            // Clear any partial authentication
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
        String token = request.getHeader(SecurityConstants.HEADER_STRING);
        String headerTenantId = request.getHeader(SecurityConstants.HEADER_TENANT_STRING);

        if (token != null) {
            try {
                String cleanToken = token.replace(SecurityConstants.TOKEN_PREFIX, "");
                logger.debug("Attempting to parse JWT token with secret length: {}",
                        SecurityConstants.SECRET.length());

                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.SECRET)
                        .parseClaimsJws(cleanToken)
                        .getBody();

                String user = claims.getSubject();
                ArrayList<String> roles = (ArrayList<String>) claims.get("roles");

                logger.debug("Successfully parsed JWT for user: {} with roles: {}", user, roles);

                ArrayList<GrantedAuthority> authorities = new ArrayList<>();
                if (roles != null) {
                    for (String role : roles) {
                        authorities.add(new SimpleGrantedAuthority(role));
                    }
                }

                if (user != null) {
                    return new UsernamePasswordAuthenticationToken(user, null, authorities);
                }

            } catch (SignatureException e) {
                logger.error("Invalid JWT signature - token may have been signed with different key: {}", e.getMessage());
                throw e;
            } catch (ExpiredJwtException e) {
                logger.error("JWT token has expired: {}", e.getMessage());
                throw e;
            } catch (MalformedJwtException e) {
                logger.error("Malformed JWT token: {}", e.getMessage());
                throw e;
            } catch (UnsupportedJwtException e) {
                logger.error("Unsupported JWT token: {}", e.getMessage());
                throw e;
            } catch (IllegalArgumentException e) {
                logger.error("JWT token is empty or invalid: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                logger.error("Unexpected error processing JWT: {}", e.getMessage(), e);
                throw e;
            }
        }

        logger.debug("No valid JWT token found");
        return null;
    }
}