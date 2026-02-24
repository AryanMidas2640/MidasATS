package com.midas.consulting.service;

import com.midas.consulting.model.StorageConfig;
import com.midas.consulting.service.storage.TenantConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultStorageConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(DefaultStorageConfigService.class);
    
    @Autowired
    private TenantConfigService tenantConfigService;
    
    public void createDefaultStorageConfig(String tenantId) {
        try {
            // Check if default config already exists
            StorageConfig existingConfig = tenantConfigService.getStorageConfig(tenantId);
            if (existingConfig != null) {
                logger.info("Storage config already exists for tenant: {}", tenantId);
                return;
            }
            
            // Create default storage configuration
            StorageConfig defaultConfig = new StorageConfig()
                    .setTenantId(tenantId)
                    .setPrimaryProvider(StorageConfig.StorageProvider.ONEDRIVE) // Default to OneDrive
                    .setBaseFolderPath("/" + tenantId + "/hrms-employee-docs/")
                    .setEnableEncryption(false) // Start with encryption disabled
                    .setRetentionDays(365) // 1 year retention
                    .setEnableBackup(false) // Start with backup disabled
                    .setEnableVersioning(true) // Enable versioning by default
                    .setMaxFileSize(50 * 1024 * 1024) // 50MB max file size
                    .setActive(true);
            
            // Set allowed file types
            String[] allowedTypes = {"pdf", "doc", "docx", "txt", "png", "jpg", "jpeg", "xlsx", "xls"};
            defaultConfig.setAllowedFileTypes(allowedTypes);
            
            // Create default OneDrive config (tenant will need to update with real credentials)
            StorageConfig.OneDriveConfig oneDriveConfig = new StorageConfig.OneDriveConfig()
                    .setClientId("") // Empty - tenant needs to configure
                    .setClientSecret("") // Empty - tenant needs to configure
                    .setTenantId("") // Empty - tenant needs to configure
                    .setDriveId(""); // Empty - tenant needs to configure
            
            defaultConfig.setOneDriveConfig(oneDriveConfig);
            
            // Save the configuration
            StorageConfig savedConfig = tenantConfigService.saveStorageConfig(defaultConfig);
            
            logger.info("Created default storage configuration for tenant: {} with ID: {}", 
                       tenantId, savedConfig.getId());
            
        } catch (Exception e) {
            logger.error("Failed to create default storage configuration for tenant {}: {}", 
                        tenantId, e.getMessage(), e);
        }
    }
    
    /**
     * Create OneDrive-specific storage configuration
     */
    public StorageConfig createOneDriveConfig(String tenantId, String clientId, String clientSecret, 
                                              String oneDriveTenantId, String driveId) {
        try {
            StorageConfig config = new StorageConfig()
                    .setTenantId(tenantId)
                    .setPrimaryProvider(StorageConfig.StorageProvider.ONEDRIVE)
                    .setBaseFolderPath("/" + tenantId + "/hrms-employee-docs/")
                    .setEnableEncryption(true)
                    .setRetentionDays(365)
                    .setEnableBackup(true)
                    .setEnableVersioning(true)
                    .setMaxFileSize(50 * 1024 * 1024)
                    .setActive(true);
            
            StorageConfig.OneDriveConfig oneDriveConfig = new StorageConfig.OneDriveConfig()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setTenantId(oneDriveTenantId)
                    .setDriveId(driveId);
            
            config.setOneDriveConfig(oneDriveConfig);
            
            return tenantConfigService.saveStorageConfig(config);
            
        } catch (Exception e) {
            logger.error("Failed to create OneDrive storage configuration for tenant {}: {}", 
                        tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to create OneDrive storage configuration", e);
        }
    }
    
    /**
     * Create Google Drive-specific storage configuration
     */
    public StorageConfig createGoogleDriveConfig(String tenantId, String serviceAccountKeyPath, 
                                                  String folderId, String credentialsFilePath) {
        try {
            StorageConfig config = new StorageConfig()
                    .setTenantId(tenantId)
                    .setPrimaryProvider(StorageConfig.StorageProvider.GOOGLE_DRIVE)
                    .setBaseFolderPath("/" + tenantId + "/hrms-employee-docs/")
                    .setEnableEncryption(true)
                    .setRetentionDays(365)
                    .setEnableBackup(true)
                    .setEnableVersioning(true)
                    .setMaxFileSize(50 * 1024 * 1024)
                    .setActive(true);
            
            StorageConfig.GoogleDriveConfig googleDriveConfig = new StorageConfig.GoogleDriveConfig()
                    .setServiceAccountKeyPath(serviceAccountKeyPath)
                    .setFolderId(folderId)
                    .setCredentialsFilePath(credentialsFilePath);
            
            config.setGoogleDriveConfig(googleDriveConfig);
            
            return tenantConfigService.saveStorageConfig(config);
            
        } catch (Exception e) {
            logger.error("Failed to create Google Drive storage configuration for tenant {}: {}", 
                        tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to create Google Drive storage configuration", e);
        }
    }
    
    /**
     * Create AWS S3-specific storage configuration
     */
    public StorageConfig createAwsS3Config(String tenantId, String accessKey, String secretKey, 
                                           String region, String bucketName) {
        try {
            StorageConfig config = new StorageConfig()
                    .setTenantId(tenantId)
                    .setPrimaryProvider(StorageConfig.StorageProvider.AWS_S3)
                    .setBaseFolderPath("/" + tenantId + "/hrms-employee-docs/")
                    .setEnableEncryption(true)
                    .setRetentionDays(365)
                    .setEnableBackup(false) // S3 has its own versioning
                    .setEnableVersioning(true)
                    .setMaxFileSize(50 * 1024 * 1024)
                    .setActive(true);
            
            StorageConfig.AwsS3Config awsS3Config = new StorageConfig.AwsS3Config()
                    .setAccessKey(accessKey)
                    .setSecretKey(secretKey)
                    .setRegion(region)
                    .setBucketName(bucketName)
                    .setStorageClass("STANDARD");
            
            config.setAwsS3Config(awsS3Config);
            
            return tenantConfigService.saveStorageConfig(config);
            
        } catch (Exception e) {
            logger.error("Failed to create AWS S3 storage configuration for tenant {}: {}", 
                        tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to create AWS S3 storage configuration", e);
        }
    }
}