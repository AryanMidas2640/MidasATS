package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.api.ComprehensiveTenantController;
import com.midas.consulting.model.StorageConfig;
import com.midas.consulting.service.storage.TenantConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ComprehensiveStorageSetupService {
    
    private static final Logger logger = LoggerFactory.getLogger(ComprehensiveStorageSetupService.class);
    
    @Autowired private TenantConfigService tenantConfigService;
    @Autowired private MongoTemplateProvider mongoTemplateProvider;
    
    public ComprehensiveTenantController.ComprehensiveStorageSetupResult setupComprehensiveStorageSystem(
            String tenantId, ComprehensiveTenantController.ComprehensiveStorageConfig config) {
        
        logger.info("Setting up comprehensive storage system for tenant: {} with provider: {}", 
                   tenantId, config.getProvider());
        
        ComprehensiveTenantController.ComprehensiveStorageSetupResult result = new ComprehensiveTenantController.ComprehensiveStorageSetupResult();
        
        try {
            // Deactivate any existing storage configurations
            deactivateAllStorageConfigs(tenantId);
            
            // Create new storage configuration based on provider
            StorageConfig storageConfig = createStorageConfigFromRequest(tenantId, config);
            StorageConfig saved = tenantConfigService.saveStorageConfig(storageConfig);
            
            // Test the configuration
            boolean testPassed = testStorageConfiguration(saved);
            
            result.setSuccess(true)
                  .setConfigId(saved.getId())
                  .setProvider(config.getProvider().name())
                  .setMessage("Storage system configured successfully with " + config.getProvider().name());
                  
            logger.info("Storage system setup completed successfully for tenant: {}", tenantId);
                  
        } catch (Exception e) {
            logger.error("Storage system setup failed for tenant {}: {}", tenantId, e.getMessage(), e);
            result.setSuccess(false)
                  .setMessage("Storage setup failed: " + e.getMessage());
        }
        
        return result;
    }
    
    public void deactivateAllStorageConfigs(String tenantId) {
        try {
            logger.info("Deactivating all storage configurations for tenant: {}", tenantId);
            
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            
            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            Update update = new Update()
                .set("active", false)
                .set("dateModified", new Date());
            
            mongoTemplate.updateMulti(query, update, StorageConfig.class);
            
            logger.info("All storage configurations deactivated for tenant: {}", tenantId);
            
        } catch (Exception e) {
            logger.error("Failed to deactivate storage configs for tenant {}: {}", tenantId, e.getMessage());
        }
    }
    
    public ComprehensiveTenantController.ComprehensiveStorageStatusResult getComprehensiveStorageStatus(String tenantId) {
        ComprehensiveTenantController.ComprehensiveStorageStatusResult status = new ComprehensiveTenantController.ComprehensiveStorageStatusResult();
        
        try {
            StorageConfig activeConfig = tenantConfigService.getStorageConfig(tenantId);
            
            if (activeConfig != null && activeConfig.isActive()) {
                status.setConfigured(true)
                      .setActiveProvider(activeConfig.getPrimaryProvider().getValue())
                      .setBackupEnabled(activeConfig.isEnableBackup())
                      .setConfigId(activeConfig.getId());
                
                // Test the configuration
                boolean testPassed = testStorageConfiguration(activeConfig);
                status.setLastTested(new Date());
            } else {
                status.setConfigured(false);
            }
            
        } catch (Exception e) {
            logger.error("Failed to get storage status for tenant {}: {}", tenantId, e.getMessage());
            status.setConfigured(false);
        }
        
        return status;
    }
    
    private StorageConfig createStorageConfigFromRequest(String tenantId,
            ComprehensiveTenantController.ComprehensiveStorageConfig config) {
        
        logger.debug("Creating storage config for provider: {}", config.getProvider());
        
        StorageConfig storageConfig = new StorageConfig()
            .setTenantId(tenantId)
            .setBaseFolderPath("/" + tenantId + "/hrms-employee-docs/")
            .setActive(true)
            .setEnableBackup(config.getEnableBackup())
            .setEnableVersioning(true)
            .setEnableEncryption(true)
            .setMaxFileSize(50 * 1024 * 1024) // 50MB
            .setAllowedFileTypes(new String[]{"pdf", "doc", "docx", "txt", "png", "jpg", "jpeg"})
            .setDateCreated(new Date())
            .setDateModified(new Date());
            
        switch (config.getProvider()) {
            case ONEDRIVE:
                validateOneDriveConfig(config);
                storageConfig.setPrimaryProvider(StorageConfig.StorageProvider.ONEDRIVE);
                StorageConfig.OneDriveConfig oneDriveConfig = new StorageConfig.OneDriveConfig()
                    .setClientId(config.getOneDriveClientId())
                    .setClientSecret(config.getOneDriveClientSecret())
                    .setTenantId(config.getOneDriveTenantId())
                    .setDriveId(config.getDriveId());
                storageConfig.setOneDriveConfig(oneDriveConfig);
                break;
                
            case GOOGLE_DRIVE:
                validateGoogleDriveConfig(config);
                storageConfig.setPrimaryProvider(StorageConfig.StorageProvider.GOOGLE_DRIVE);
                StorageConfig.GoogleDriveConfig googleDriveConfig = new StorageConfig.GoogleDriveConfig()
                    .setServiceAccountKeyPath(config.getGoogleServiceAccountKeyPath())
                    .setCredentialsFilePath(config.getGoogleCredentialsFilePath())
                    .setFolderId(config.getGoogleFolderId());
                storageConfig.setGoogleDriveConfig(googleDriveConfig);
                break;
                
            case AWS_S3:
                validateAwsS3Config(config);
                storageConfig.setPrimaryProvider(StorageConfig.StorageProvider.AWS_S3);
                StorageConfig.AwsS3Config awsS3Config = new StorageConfig.AwsS3Config()
                    .setAccessKey(config.getAwsAccessKey())
                    .setSecretKey(config.getAwsSecretKey())
                    .setRegion(config.getAwsRegion())
                    .setBucketName(config.getAwsBucketName())
                    .setStorageClass("STANDARD");
                storageConfig.setAwsS3Config(awsS3Config);
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported storage provider: " + config.getProvider());
        }
        
        return storageConfig;
    }
    
    private void validateOneDriveConfig(ComprehensiveTenantController.ComprehensiveStorageConfig config) {
        if (config.getOneDriveClientId() == null || config.getOneDriveClientId().trim().isEmpty()) {
            throw new IllegalArgumentException("OneDrive Client ID is required");
        }
        if (config.getOneDriveClientSecret() == null || config.getOneDriveClientSecret().trim().isEmpty()) {
            throw new IllegalArgumentException("OneDrive Client Secret is required");
        }
        if (config.getOneDriveTenantId() == null || config.getOneDriveTenantId().trim().isEmpty()) {
            throw new IllegalArgumentException("OneDrive Tenant ID is required");
        }
    }
    
    private void validateGoogleDriveConfig(ComprehensiveTenantController.ComprehensiveStorageConfig config) {
        if (config.getGoogleServiceAccountKeyPath() == null || config.getGoogleServiceAccountKeyPath().trim().isEmpty()) {
            throw new IllegalArgumentException("Google Drive Service Account Key Path is required");
        }
    }
    
    private void validateAwsS3Config(ComprehensiveTenantController.ComprehensiveStorageConfig config) {
        if (config.getAwsAccessKey() == null || config.getAwsAccessKey().trim().isEmpty()) {
            throw new IllegalArgumentException("AWS Access Key is required");
        }
        if (config.getAwsSecretKey() == null || config.getAwsSecretKey().trim().isEmpty()) {
            throw new IllegalArgumentException("AWS Secret Key is required");
        }
        if (config.getAwsRegion() == null || config.getAwsRegion().trim().isEmpty()) {
            throw new IllegalArgumentException("AWS Region is required");
        }
        if (config.getAwsBucketName() == null || config.getAwsBucketName().trim().isEmpty()) {
            throw new IllegalArgumentException("AWS Bucket Name is required");
        }
    }
    
    private boolean testStorageConfiguration(StorageConfig config) {
        try {
            logger.info("Testing storage configuration: {}", config.getId());
            // TODO: Implement actual storage connectivity test based on provider
            // For now, return true if configuration is valid
            return config.getPrimaryProvider() != null;
        } catch (Exception e) {
            logger.warn("Storage configuration test failed: {}", e.getMessage());
            return false;
        }
    }
}