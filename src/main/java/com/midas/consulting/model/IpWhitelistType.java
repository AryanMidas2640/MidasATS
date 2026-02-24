// IpWhitelistType.java
package com.midas.consulting.model;

public enum IpWhitelistType {
    SINGLE_IP("Single IP Address", "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"),
    IP_RANGE("IP Address Range (CIDR)", "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\/(?:[0-2]?[0-9]|3[0-2])$"),
    WILDCARD("Wildcard (*)", "^\\*$"),
    USER_SPECIFIC("User-specific access", null),
    ROLE_SPECIFIC("Role-specific access", null),
    HYBRID("Hybrid user/role access", null);

    private final String displayName;
    private final String validationPattern;

    IpWhitelistType(String displayName, String validationPattern) {
        this.displayName = displayName;
        this.validationPattern = validationPattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getValidationPattern() {
        return validationPattern;
    }

    /**
     * Check if this type requires IP pattern validation
     */
    public boolean requiresIpValidation() {
        return validationPattern != null;
    }

    /**
     * Validate IP address against this type's pattern
     */
    public boolean isValidIpFormat(String ipAddress) {
        if (validationPattern == null) {
            return true; // No validation required for access-based types
        }
        return ipAddress != null && ipAddress.matches(validationPattern);
    }

    /**
     * Get the appropriate type based on IP address format
     */
    public static IpWhitelistType determineFromIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            return SINGLE_IP; // Default
        }

        String trimmed = ipAddress.trim();
        
        if (WILDCARD.isValidIpFormat(trimmed)) {
            return WILDCARD;
        } else if (IP_RANGE.isValidIpFormat(trimmed)) {
            return IP_RANGE;
        } else if (SINGLE_IP.isValidIpFormat(trimmed)) {
            return SINGLE_IP;
        }
        
        return SINGLE_IP; // Default fallback
    }
}