package com.midas.consulting.controller.v1.api;

import com.midas.consulting.util.JwtDebugUtil;
import com.midas.consulting.security.SecurityConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller - ONLY enable in development/testing environments
 * Add this to application.properties: jwt.debug.enabled=true
 */
@RestController
@RequestMapping("/api/v1/debug/jwt")
@ConditionalOnProperty(name = "jwt.debug.enabled", havingValue = "true")
public class JwtDebugController {

    @Autowired
    private JwtDebugUtil jwtDebugUtil;

    @PostMapping("/analyze")
    public Map<String, Object> analyzeToken(@RequestBody Map<String, String> request, 
                                           HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = request.get("token");
            if (token == null) {
                // Try to get from Authorization header
                token = httpRequest.getHeader("Authorization");
            }
            
            if (token == null) {
                response.put("error", "No token provided");
                return response;
            }
            
            jwtDebugUtil.debugToken(token);
            
            response.put("status", "success");
            response.put("message", "Token analysis completed - check server logs");
            response.put("currentSecret", SecurityConstants.SECRET.substring(0, 
                        Math.min(10, SecurityConstants.SECRET.length())) + "...");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/generate-test-token")
    public Map<String, Object> generateTestToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            if (email == null) {
                email = "test@example.com";
            }
            
            String token = jwtDebugUtil.generateTestToken(email);
            
            response.put("status", "success");
            response.put("token", "Bearer " + token);
            response.put("email", email);
            response.put("secretUsed", SecurityConstants.SECRET.substring(0, 
                        Math.min(10, SecurityConstants.SECRET.length())) + "...");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @GetMapping("/config")
    public Map<String, Object> getConfig() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("secretLength", SecurityConstants.SECRET.length());
        response.put("secretPrefix", SecurityConstants.SECRET.substring(0, 
                    Math.min(10, SecurityConstants.SECRET.length())) + "...");
        response.put("tokenPrefix", SecurityConstants.TOKEN_PREFIX);
        response.put("expirationTime", SecurityConstants.EXPIRATION_TIME);
        
        return response;
    }
}