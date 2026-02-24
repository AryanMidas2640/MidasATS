package com.midas.consulting.controller.v1.response;

import com.midas.consulting.model.IpWhitelistScope;
import com.midas.consulting.model.IpWhitelistType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Preview response for bulk delete operations")
public class BulkDeletePreview {

    @ApiModelProperty(value = "Total number of entries to be deleted")
    private int totalEntries;

    @ApiModelProperty(value = "Number of active entries that will be deleted")
    private int activeEntries;

    @ApiModelProperty(value = "Number of inactive entries that will be deleted")
    private int inactiveEntries;

    @ApiModelProperty(value = "List of entries to be deleted")
    private List<PreviewEntry> entries;

    @ApiModelProperty(value = "List of critical entries requiring special attention")
    private List<PreviewEntry> criticalEntries;

    @ApiModelProperty(value = "List of warnings about the deletion")
    private List<String> warnings;

    @ApiModelProperty(value = "List of validation errors")
    private List<String> errors;

    @ApiModelProperty(value = "Summary by scope")
    private Map<IpWhitelistScope, Integer> scopeSummary;

    @ApiModelProperty(value = "Summary by type")
    private Map<IpWhitelistType, Integer> typeSummary;

    @ApiModelProperty(value = "Estimated impact on active sessions")
    private String impactAssessment;

    @ApiModelProperty(value = "Whether force delete is required")
    private boolean requiresForceDelete;

    @ApiModelProperty(value = "Whether the operation can proceed")
    private boolean canProceed;

    @ApiModelProperty(value = "Preview generation timestamp")
    private LocalDateTime timestamp;

    // Inner class for preview entries
    public static class PreviewEntry {
        private String id;
        private String ipAddress;
        private String description;
        private IpWhitelistScope scope;
        private IpWhitelistType type;
        private boolean isActive;
        private boolean isCritical;
        private String riskLevel;
        private List<String> affectedUsers;
        private List<String> affectedRoles;

        public PreviewEntry() {
        }

        public PreviewEntry(String id, String ipAddress, String description,
                            IpWhitelistScope scope, IpWhitelistType type, boolean isActive) {
            this.id = id;
            this.ipAddress = ipAddress;
            this.description = description;
            this.scope = scope;
            this.type = type;
            this.isActive = isActive;
            this.isCritical = determineCritical(ipAddress, scope);
            this.riskLevel = determineRiskLevel();
        }

        private boolean determineCritical(String ipAddress, IpWhitelistScope scope) {
            return ipAddress.equals("0.0.0.0/0") ||
                    ipAddress.equals("::/0") ||
                    ipAddress.startsWith("127.") ||
                    scope == IpWhitelistScope.GLOBAL ||
                    scope == IpWhitelistScope.ADMIN;
        }

        private String determineRiskLevel() {
            if (isCritical) return "HIGH";
            if (isActive && (scope == IpWhitelistScope.TENANT || scope == IpWhitelistScope.HYBRID)) return "MEDIUM";
            return "LOW";
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public IpWhitelistScope getScope() { return scope; }
        public void setScope(IpWhitelistScope scope) { this.scope = scope; }

        public IpWhitelistType getType() { return type; }
        public void setType(IpWhitelistType type) { this.type = type; }

        public boolean isActive() { return isActive; }
        public void setActive(boolean active) { isActive = active; }

        public boolean isCritical() { return isCritical; }
        public void setCritical(boolean critical) { isCritical = critical; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public List<String> getAffectedUsers() { return affectedUsers; }
        public void setAffectedUsers(List<String> affectedUsers) { this.affectedUsers = affectedUsers; }

        public List<String> getAffectedRoles() { return affectedRoles; }
        public void setAffectedRoles(List<String> affectedRoles) { this.affectedRoles = affectedRoles; }
    }

    // Constructors
    public BulkDeletePreview() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public int getTotalEntries() { return totalEntries; }
    public void setTotalEntries(int totalEntries) { this.totalEntries = totalEntries; }

    public int getActiveEntries() { return activeEntries; }
    public void setActiveEntries(int activeEntries) { this.activeEntries = activeEntries; }

    public int getInactiveEntries() { return inactiveEntries; }
    public void setInactiveEntries(int inactiveEntries) { this.inactiveEntries = inactiveEntries; }

    public List<PreviewEntry> getEntries() { return entries; }
    public void setEntries(List<PreviewEntry> entries) { this.entries = entries; }

    public List<PreviewEntry> getCriticalEntries() { return criticalEntries; }
    public void setCriticalEntries(List<PreviewEntry> criticalEntries) { this.criticalEntries = criticalEntries; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }

    public Map<IpWhitelistScope, Integer> getScopeSummary() { return scopeSummary; }
    public void setScopeSummary(Map<IpWhitelistScope, Integer> scopeSummary) { this.scopeSummary = scopeSummary; }

    public Map<IpWhitelistType, Integer> getTypeSummary() { return typeSummary; }
    public void setTypeSummary(Map<IpWhitelistType, Integer> typeSummary) { this.typeSummary = typeSummary; }

    public String getImpactAssessment() { return impactAssessment; }
    public void setImpactAssessment(String impactAssessment) { this.impactAssessment = impactAssessment; }

    public boolean isRequiresForceDelete() { return requiresForceDelete; }
    public void setRequiresForceDelete(boolean requiresForceDelete) { this.requiresForceDelete = requiresForceDelete; }

    public boolean isCanProceed() { return canProceed; }
    public void setCanProceed(boolean canProceed) { this.canProceed = canProceed; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}