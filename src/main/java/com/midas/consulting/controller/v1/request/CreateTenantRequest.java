package com.midas.consulting.controller.v1.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Request DTO for creating a comprehensive tenant with all three databases
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
public class CreateTenantRequest {

    @NotBlank(message = "Subdomain is required")
    @Size(min = 3, max = 30, message = "Subdomain must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-z0-9]([a-z0-9-]*[a-z0-9])?$",
            message = "Subdomain must contain only lowercase letters, numbers, and hyphens (not at start/end)")
    private String subdomain;

    @NotBlank(message = "Tenant name is required")
    @Size(max = 100, message = "Tenant name must not exceed 100 characters")
    private String tenantName;

    @NotBlank(message = "HRMS connection string is required")
    private String connectionString;

    @NotBlank(message = "Node/Checklist connection string is required")
    private String connectionStringNode;

    @NotBlank(message = "Job Sync connection string is required")
    private String connectionStringJobSync;

    private String host;

    @Email(message = "Invalid email format")
    private String email;

    private String webUrl;

    private String logo;

    private String checklistUrl;

    // Additional configuration options
    private Integer maxPoolSize = 50;
    private Integer minPoolSize = 10;
    private Integer maxWaitTimeMs = 3000;
    private Integer maxConnectionIdleTimeMs = 60000;
    private Integer maxConnectionLifeTimeMs = 1800000;

    // Service configuration flags
    private boolean enableHRMS = true;
    private boolean enableChecklist = true;
    private boolean enableJobSync = true;
    private boolean createDefaultData = true;
    private boolean createDefaultAdminUser = true;

    // Validation method
    public boolean isValid() {
        return subdomain != null && !subdomain.trim().isEmpty() &&
                connectionString != null && !connectionString.trim().isEmpty() &&
                connectionStringNode != null && !connectionStringNode.trim().isEmpty() &&
                connectionStringJobSync != null && !connectionStringJobSync.trim().isEmpty();
    }

    // Helper method to get all connection strings
    public String[] getAllConnectionStrings() {
        return new String[]{connectionString, connectionStringNode, connectionStringJobSync};
    }

    // Helper method to get database names from connection strings
    public String getHRMSDatabaseName() {
        return extractDatabaseName(connectionString);
    }

    public String getNodeDatabaseName() {
        return extractDatabaseName(connectionStringNode);
    }

    public String getJobSyncDatabaseName() {
        return extractDatabaseName(connectionStringJobSync);
    }

    private String extractDatabaseName(String connectionString) {
        if (connectionString == null) return null;

        String regex = "mongodb://.*?/(.*?)(\\?|$)";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(connectionString);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public String toString() {
        return "CreateTenantRequest{" +
                "subdomain='" + subdomain + '\'' +
                ", tenantName='" + tenantName + '\'' +
                ", host='" + host + '\'' +
                ", email='" + email + '\'' +
                ", webUrl='" + webUrl + '\'' +
                ", enableHRMS=" + enableHRMS +
                ", enableChecklist=" + enableChecklist +
                ", enableJobSync=" + enableJobSync +
                ", createDefaultData=" + createDefaultData +
                '}';
    }
}

//package com.midas.consulting.controller.v1.request;
//
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//import lombok.experimental.Accessors;
//
//import javax.validation.constraints.Email;
//import javax.validation.constraints.NotBlank;
//import javax.validation.constraints.Pattern;
//import javax.validation.constraints.Size;
//import java.util.Map;
//
//@Getter
//@Setter
//@Accessors(chain = true)
//@NoArgsConstructor
//public class CreateTenantRequest {
//
//    @NotBlank(message = "Tenant name cannot be blank")
//    @Size(min = 2, max = 100, message = "Tenant name must be between 2 and 100 characters")
//    private String tenantName;
//
//    @NotBlank(message = "Subdomain cannot be blank")
//    @Pattern(regexp = "^[a-z0-9]([a-z0-9-]*[a-z0-9])?$",
//            message = "Subdomain must contain only lowercase letters, numbers, and hyphens")
//    @Size(min = 3, max = 30, message = "Subdomain must be between 3 and 30 characters")
//    private String subdomain;
//
//    @NotBlank(message = "Main connection string cannot be blank")
//    private String connectionString;
//
//    private String connectionStringNode;
//
//    private String connectionStringJobSync;
//
//    @Size(max = 255, message = "Host cannot exceed 255 characters")
//    private String host;
//
//    @Email(message = "Email must be a valid email address")
//    private String email;
//
//    @Size(max = 500, message = "Web URL cannot exceed 500 characters")
//    private String webUrl;
//
//    @Size(max = 500, message = "Logo URL cannot exceed 500 characters")
//    private String logo;
//
//    @Size(max = 500, message = "Checklist URL cannot exceed 500 characters")
//    private String checklistUrl;
//
//    private Map<String, Object> connectionPoolConfig;
//}