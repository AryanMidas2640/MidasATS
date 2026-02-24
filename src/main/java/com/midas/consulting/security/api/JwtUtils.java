package com.midas.consulting.security.api;

import com.midas.consulting.model.user.User;
import com.midas.consulting.security.SecurityConstants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	/**
	 * Get the secret key for JWT signing
	 * This method handles different JWT library versions
	 */
	private SecretKey getSigningKey() {
		try {
			// For newer JWT library versions, use Keys.hmacShaKeyFor
			return Keys.hmacShaKeyFor(SecurityConstants.getSecretBytes());
		} catch (Exception e) {
			logger.warn("Failed to create SecretKey, falling back to base64 approach: {}", e.getMessage());
			// Fallback for older versions - this shouldn't happen but just in case
			return null;
		}
	}

	/**
	 * Generate JWT token from Authentication object
	 */
	public String generateJwtToken(Authentication authentication) {
		org.springframework.security.core.userdetails.User user =
				(org.springframework.security.core.userdetails.User) authentication.getPrincipal();

		try {
			SecretKey key = getSigningKey();
			if (key != null) {
				// Use newer API with SecretKey
				return Jwts.builder()
						.setSubject(user.getUsername())
						.setIssuedAt(new Date())
						.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
						.signWith(SignatureAlgorithm.HS512, key)
						.compact();
			} else {
				// Fallback to base64 string approach
				return Jwts.builder()
						.setSubject(user.getUsername())
						.setIssuedAt(new Date())
						.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
						.signWith(SignatureAlgorithm.HS512, SecurityConstants.getBase64Secret())
						.compact();
			}
		} catch (Exception e) {
			logger.error("Error generating JWT token: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to generate JWT token", e);
		}
	}

	/**
	 * Generate JWT token from User object
	 */
	public String genrateJWTToken(User user) {
		try {
			SecretKey key = getSigningKey();
			if (key != null) {
				// Use newer API with SecretKey
				return Jwts.builder()
						.setSubject(user.getEmail())
						.setIssuedAt(new Date())
						.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
						.signWith(SignatureAlgorithm.HS512, key)
						.compact();
			} else {
				// Fallback to base64 string approach
				return Jwts.builder()
						.setSubject(user.getEmail())
						.setIssuedAt(new Date())
						.setExpiration(new Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME))
						.signWith(SignatureAlgorithm.HS512, SecurityConstants.getBase64Secret())
						.compact();
			}
		} catch (Exception e) {
			logger.error("Error generating JWT token for user {}: {}", user.getEmail(), e.getMessage(), e);
			throw new RuntimeException("Failed to generate JWT token for user: " + user.getEmail(), e);
		}
	}

	/**
	 * Generate JWT token for Scrum (duplicate method - should be cleaned up)
	 */
	public String genrateJWTTokenScrum(User user) {
		return genrateJWTToken(user); // Reuse the main method
	}

	/**
	 * Extract username from JWT token
	 */
	public String getUserNameFromJwtToken(String token) {
		try {
			SecretKey key = getSigningKey();
			if (key != null) {
				return Jwts.parserBuilder()
						.setSigningKey(key)
						.build()
						.parseClaimsJws(token)
						.getBody()
						.getSubject();
			} else {
				// Fallback to legacy parser
				return Jwts.parser()
						.setSigningKey(SecurityConstants.getBase64Secret())
						.parseClaimsJws(token)
						.getBody()
						.getSubject();
			}
		} catch (Exception e) {
			logger.error("Error extracting username from JWT: {}", e.getMessage());
			throw new RuntimeException("Failed to extract username from JWT", e);
		}
	}

	/**
	 * Validate JWT token
	 */
	public boolean validateJwtToken(String authToken) {
		try {
			SecretKey key = getSigningKey();
			if (key != null) {
				Jwts.parserBuilder()
						.setSigningKey(key)
						.build()
						.parseClaimsJws(authToken);
			} else {
				// Fallback to legacy parser
				Jwts.parser()
						.setSigningKey(SecurityConstants.getBase64Secret())
						.parseClaimsJws(authToken);
			}
			return true;
		} catch (SignatureException e) {
			logger.error("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			logger.error("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("JWT claims string is empty: {}", e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected JWT validation error: {}", e.getMessage());
		}

		return false;
	}

	/**
	 * Debug method to log current configuration
	 */
	public void logConfiguration() {
		logger.info("JWT Configuration:");
		logger.info("- Secret string length: {}", SecurityConstants.getSecretString().length());
		logger.info("- Secret bytes length: {}", SecurityConstants.getSecretBytes().length);
		logger.info("- Base64 secret length: {}", SecurityConstants.getBase64Secret().length());
		logger.info("- Expiration time: {} ms", SecurityConstants.EXPIRATION_TIME);

		try {
			SecretKey key = getSigningKey();
			logger.info("- SecretKey creation: {}", key != null ? "SUCCESS" : "FAILED");
		} catch (Exception e) {
			logger.error("- SecretKey creation failed: {}", e.getMessage());
		}
	}
}
//package com.midas.consulting.security.api;
//
//import com.midas.consulting.model.user.User;
//import com.midas.consulting.security.SecurityConstants;
//import io.jsonwebtoken.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//
//@Component
//public class JwtUtils {
//	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
//
//	public String generateJwtToken(Authentication authentication) {
//		org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
//
//		Claims claims = Jwts.claims().setSubject(user.getUsername());
//		return Jwts.builder()
////		Claims claims = Jwts.claims().setSubject(login);
//				.setClaims(claims)
//				.setIssuedAt(new Date())
//				.setExpiration(new Date((new Date()).getTime() + SecurityConstants.EXPIRATION_TIME))
//				.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET)
//				.compact();
//	}
//
//	public String genrateJWTToken(User user){
//		return Jwts.builder()
//				.setSubject((user.getEmail()))
//				.setIssuedAt(new Date())
//				.setExpiration(new Date((new Date()).getTime() +SecurityConstants.EXPIRATION_TIME))
//				.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET)
//				.compact();
//	}
//
//	public String genrateJWTTokenScrum(User user){
//		return Jwts.builder()
//				.setSubject((user.getEmail()))
//				.setIssuedAt(new Date())
//				.setExpiration(new Date((new Date()).getTime() + SecurityConstants.EXPIRATION_TIME))
//				.signWith(SignatureAlgorithm.HS512, SecurityConstants.SECRET)
//				.compact();
//	}
//
//	public String getUserNameFromJwtToken(String token) {
//		return Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(token).getBody().getSubject();
//	}
//
//	public boolean validateJwtToken(String authToken) {
//		try {
//			Jwts.parser().setSigningKey(SecurityConstants.SECRET).parseClaimsJws(authToken);
//			return true;
//		} catch (SignatureException e) {
//			logger.error("Invalid JWT signature: {}", e.getMessage());
//		} catch (MalformedJwtException e) {
//			logger.error("Invalid JWT token: {}", e.getMessage());
//		} catch (ExpiredJwtException e) {
//			logger.error("JWT token is expired: {}", e.getMessage());
//		} catch (UnsupportedJwtException e) {
//			logger.error("JWT token is unsupported: {}", e.getMessage());
//		} catch (IllegalArgumentException e) {
//			logger.error("JWT claims string is empty: {}", e.getMessage());
//		}
//
//		return false;
//	}
//}
