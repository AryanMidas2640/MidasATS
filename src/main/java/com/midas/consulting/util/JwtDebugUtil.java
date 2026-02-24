package com.midas.consulting.util;

import com.midas.consulting.security.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Date;

@Component
public class JwtDebugUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtDebugUtil.class);

    /**
     * Debug JWT token without validation - useful for troubleshooting
     */
    public void debugToken(String token) {
        try {
            // Remove Bearer prefix if present
            String cleanToken = token.replace("Bearer ", "");
            
            // Split JWT parts
            String[] parts = cleanToken.split("\\.");
            if (parts.length != 3) {
                logger.error("Invalid JWT format - expected 3 parts, got {}", parts.length);
                return;
            }

            // Decode header (without signature verification)
            String header = new String(Base64.getUrlDecoder().decode(parts[0]));
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            
            logger.info("JWT Debug Information:");
            logger.info("Header: {}", header);
            logger.info("Payload: {}", payload);
            logger.info("Current Secret Length: {}", SecurityConstants.SECRET.length());
            logger.info("Current Secret (first 10 chars): {}", 
                       SecurityConstants.SECRET.substring(0, Math.min(10, SecurityConstants.SECRET.length())));
            
            // Try to parse with current secret
            try {
                Claims claims = Jwts.parser()
                        .setSigningKey(SecurityConstants.SECRET)
                        .parseClaimsJws(cleanToken)
                        .getBody();
                
                logger.info("Token validation SUCCESS");
                logger.info("Subject: {}", claims.getSubject());
                logger.info("Issued At: {}", claims.getIssuedAt());
                logger.info("Expires At: {}", claims.getExpiration());
                logger.info("Is Expired: {}", claims.getExpiration().before(new Date()));
                
            } catch (Exception e) {
                logger.error("Token validation FAILED: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error debugging JWT token: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate a test token for debugging
     */
    public String generateTestToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, SecurityConstants.SECRET)
                .compact();
    }
}