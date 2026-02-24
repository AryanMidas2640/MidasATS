package com.midas.consulting.controller.v1.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Bulk delete response for IP whitelist operations")
public class BulkDeleteIpResponse {

    @ApiModelProperty(value = "Total number of entries requested for deletion")
    private int totalRequested;

    @ApiModelProperty(value = "Number of entries successfully deleted")
    private int successfullyDeleted;

    @ApiModelProperty(value = "Number of entries that failed to delete")
    private int failedToDelete;

    @ApiModelProperty(value = "List of successfully deleted entry IDs")
    private List<String> deletedIds;

    @ApiModelProperty(value = "List of failed deletions with reasons")
    private List<BulkDeleteIpResponse.FailedDeletion> failedDeletions;

    @ApiModelProperty(value = "List of warnings for potentially risky deletions")
    private List<String> warnings;

    @ApiModelProperty(value = "Timestamp of the bulk delete operation")
    private LocalDateTime timestamp;

    @ApiModelProperty(value = "User who performed the bulk delete")
    private String performedBy;

    @ApiModelProperty(value = "Summary of affected IP ranges and scopes")
    private Map<String, Object> deletionSummary;

    // Inner class for failed deletions
    public static class FailedDeletion {
        private String id;
        private String ipAddress;
        private String reason;
        private String errorCode;

        public FailedDeletion(String id, String ipAddress, String reason, String errorCode) {
            this.id = id;
            this.ipAddress = ipAddress;
            this.reason = reason;
            this.errorCode = errorCode;
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getIpAddress() {
            return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
    }

    // Constructors
    public BulkDeleteIpResponse() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getTotalRequested() {
        return totalRequested;
    }

    public void setTotalRequested(int totalRequested) {
        this.totalRequested = totalRequested;
    }

    public int getSuccessfullyDeleted() {
        return successfullyDeleted;
    }

    public void setSuccessfullyDeleted(int successfullyDeleted) {
        this.successfullyDeleted = successfullyDeleted;
    }

    public int getFailedToDelete() {
        return failedToDelete;
    }

    public void setFailedToDelete(int failedToDelete) {
        this.failedToDelete = failedToDelete;
    }

    public List<String> getDeletedIds() {
        return deletedIds;
    }

    public void setDeletedIds(List<String> deletedIds) {
        this.deletedIds = deletedIds;
    }

    public List<BulkDeleteIpResponse.FailedDeletion> getFailedDeletions() {
        return failedDeletions;
    }

    public void setFailedDeletions(List<BulkDeleteIpResponse.FailedDeletion> failedDeletions) {
        this.failedDeletions = failedDeletions;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public Map<String, Object> getDeletionSummary() {
        return deletionSummary;
    }

    public void setDeletionSummary(Map<String, Object> deletionSummary) {
        this.deletionSummary = deletionSummary;
    }
}
