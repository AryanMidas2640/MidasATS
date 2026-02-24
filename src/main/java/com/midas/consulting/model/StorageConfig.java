package com.midas.consulting.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "storage_config")
public class StorageConfig {

    @Id
    private String id;
    
    @Indexed(unique = true)
    private String tenantId;  // For tenant separation
    
    // Storage provider type: "onedrive", "google_drive", or "both"
    private StorageProvider primaryProvider;
    private StorageProvider secondaryProvider; // Optional backup storage
    
    // OneDrive configuration
    private OneDriveConfig oneDriveConfig;
    
    // Google Drive configuration  
    private GoogleDriveConfig googleDriveConfig;
    
    // AWS S3 configuration
    private AwsS3Config awsS3Config;
    
    // Common settings
    private String baseFolderPath;  // Base folder structure: /{tenantId}/hrms-employee-docs/
    private boolean enableEncryption;
    private int retentionDays;
    private boolean enableBackup;
    private boolean enableVersioning;
    private long maxFileSize = 50 * 1024 * 1024; // 50MB default
    private String[] allowedFileTypes = {"pdf", "doc", "docx", "txt", "png", "jpg", "jpeg"};
    
    private Date dateCreated;
    private Date dateModified;
    private boolean active;

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class OneDriveConfig {
        private String clientId;
        private String clientSecret;
        private String tenantId;
        private String driveId;
        private String accessToken;
        private Date tokenExpiry;
        private String refreshToken;
        private String webhookUrl; // For notifications
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class GoogleDriveConfig {
        private String clientId;
        private String clientSecret;
        private String credentialsFilePath;
        private String serviceAccountKeyPath;
        private String folderId; // Parent folder ID in Google Drive
        private String accessToken;
        private Date tokenExpiry;
        private String refreshToken;
        private String webhookUrl; // For notifications
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class AwsS3Config {
        private String accessKey;
        private String secretKey;
        private String region;
        private String bucketName;
        private String kmsKeyId; // For encryption
        private String storageClass = "STANDARD";
    }

    public enum StorageProvider {
        ONEDRIVE("onedrive"),
        GOOGLE_DRIVE("google_drive"),
        AWS_S3("aws_s3"),
        BOTH("both");

        private final String value;

        StorageProvider(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}