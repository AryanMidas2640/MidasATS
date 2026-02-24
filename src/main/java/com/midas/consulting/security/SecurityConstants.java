
package com.midas.consulting.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecurityConstants {

    // Base secret string
    private static final String SECRET_STRING = "MyVerySecureSecretKeyForJWTSigning2024!@#$%^&*()ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789";

    // Base64 encoded secret for JWT HS512 signing
    public static final String SECRET = Base64.getEncoder()
            .encodeToString(SECRET_STRING.getBytes(StandardCharsets.UTF_8));

    // Raw bytes for newer JWT API (if needed)
    public static final byte[] SECRET_BYTES = SECRET_STRING.getBytes(StandardCharsets.UTF_8);

    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String HEADER_TENANT_STRING = "X-Tenant";
    public static final String HEADER_CLIENT_IP = "X-Client-IP";
    public static final String SIGN_UP_URL = "/api/v1/user/signup";
    public static final String LOGIN_URL = "/api/v1/user/authenticate";
    public static long EXPIRATION_TIME = 86_400_000; // 1 day

    // IP Whitelist specific constants
    public static final String IP_WHITELIST_BYPASS_HEADER = "X-Bypass-IP-Check";
    public static final long IP_VALIDATION_CACHE_TTL = 300; // 5 minutes in seconds

    // Utility method to get the secret in different formats
    public static String getSecretString() {
        return SECRET_STRING;
    }

    public static byte[] getSecretBytes() {
        return SECRET_BYTES;
    }

    public static String getBase64Secret() {
        return SECRET;
    }
}
//package com.midas.consulting.security;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SecurityConstants {
//
//    // Use environment variable or application.properties
//    @Value("${jwt.secret:fallback-secret-key-change-me-in-production}")
//    public static String SECRET;
//
//    // Alternative: Use a strong, consistent secret
//    // public static final String SECRET = "MyVerySecureSecretKeyForJWTSigning2024!@#$%^&*()";
//
//    public static final String TOKEN_PREFIX = "Bearer ";
//    public static final String HEADER_STRING = "Authorization";
//    public static final String HEADER_TENANT_STRING = "X-Tenant";
//    public static final String HEADER_CLIENT_IP = "X-Client-IP";
//    public static final String SIGN_UP_URL = "/api/v1/user/signup";
//    public static final String LOGIN_URL = "/api/v1/user/authenticate";
//    public static long EXPIRATION_TIME = 86_400_000; // 1 day
//
//    // IP Whitelist specific constants
//    public static final String IP_WHITELIST_BYPASS_HEADER = "X-Bypass-IP-Check";
//    public static final long IP_VALIDATION_CACHE_TTL = 300; // 5 minutes in seconds
//}