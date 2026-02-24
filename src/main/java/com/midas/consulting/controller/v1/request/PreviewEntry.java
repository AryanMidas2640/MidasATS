package com.midas.consulting.controller.v1.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@ApiModel(description = "Preview entry for bulk delete operations")
public class PreviewEntry {

    @ApiModelProperty(value = "Entry ID")
    private String id;

    @ApiModelProperty(value = "IP address or range")
    private String ipAddress;

    @ApiModelProperty(value = "Description of the entry")
    private String description;

    @ApiModelProperty(value = "Scope of the whitelist entry")
    private String scope;

    @ApiModelProperty(value = "Type of the whitelist entry")
    private String type;

    @ApiModelProperty(value = "Whether the entry is currently active")
    private boolean active;

    @ApiModelProperty(value = "Whether this is a critical IP range")
    private boolean critical;

    @ApiModelProperty(value = "Risk level for deleting this entry")
    private String riskLevel;

    @ApiModelProperty(value = "Number of users affected by this entry")
    private int affectedUsers;

    @ApiModelProperty(value = "Number of roles affected by this entry")
    private int affectedRoles;

    @ApiModelProperty(value = "When the entry was created")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "Who created the entry")
    private String createdBy;

    @ApiModelProperty(value = "When the entry was last accessed")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAccessedAt;

    @ApiModelProperty(value = "Warning message for this specific entry")
    private String warning;

    // Default constructor
    public PreviewEntry() {
        this.riskLevel = "LOW";
        this.affectedUsers = 0;
        this.affectedRoles = 0;
    }

    // Constructor with basic fields
    public PreviewEntry(String id, String ipAddress, String description, String scope,
                        String type, boolean active, boolean critical) {
        this();
        this.id = id;
        this.ipAddress = ipAddress;
        this.description = description;
        this.scope = scope;
        this.type = type;
        this.active = active;
        this.critical = critical;
        this.riskLevel = critical ? "HIGH" : (active ? "MEDIUM" : "LOW");
    }

    // Builder pattern methods
    public PreviewEntry withRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
        return this;
    }

    public PreviewEntry withAffectedUsers(int affectedUsers) {
        this.affectedUsers = affectedUsers;
        return this;
    }

    public PreviewEntry withAffectedRoles(int affectedRoles) {
        this.affectedRoles = affectedRoles;
        return this;
    }

    public PreviewEntry withWarning(String warning) {
        this.warning = warning;
        return this;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
        // Auto-adjust risk level when critical flag changes
        if (critical && !"HIGH".equals(this.riskLevel)) {
            this.riskLevel = "HIGH";
        }
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public int getAffectedUsers() {
        return affectedUsers;
    }

    public void setAffectedUsers(int affectedUsers) {
        this.affectedUsers = affectedUsers;
    }

    public int getAffectedRoles() {
        return affectedRoles;
    }

    public void setAffectedRoles(int affectedRoles) {
        this.affectedRoles = affectedRoles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    @Override
    public String toString() {
        return "PreviewEntry{" +
                "id='" + id + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", description='" + description + '\'' +
                ", scope='" + scope + '\'' +
                ", type='" + type + '\'' +
                ", active=" + active +
                ", critical=" + critical +
                ", riskLevel='" + riskLevel + '\'' +
                ", affectedUsers=" + affectedUsers +
                ", affectedRoles=" + affectedRoles +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", lastAccessedAt=" + lastAccessedAt +
                ", warning='" + warning + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PreviewEntry that = (PreviewEntry) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}