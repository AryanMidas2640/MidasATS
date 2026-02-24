
package com.midas.consulting.controller.v1.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel(description = "Complete bulk delete preview response with all details")
public class BulkDeletePreview {
    
    @ApiModelProperty(value = "Total number of entries to be deleted")
    private int totalEntries;
    
    @ApiModelProperty(value = "Number of active entries to be deleted")
    private int activeEntries;
    
    @ApiModelProperty(value = "Number of inactive entries to be deleted")
    private int inactiveEntries;
    
    @ApiModelProperty(value = "List of critical IP ranges that will be deleted")
    private List<String> criticalIpRanges;
    
    @ApiModelProperty(value = "List of warnings about the deletion")
    private List<String> warnings;
    
    @ApiModelProperty(value = "Breakdown by scope")
    private Map<String, Integer> scopeBreakdown;
    
    @ApiModelProperty(value = "Breakdown by type")
    private Map<String, Integer> typeBreakdown;
    
    @ApiModelProperty(value = "Detailed entries to be deleted")
    private List<PreviewEntry> entries;
    
    @ApiModelProperty(value = "Estimated impact score (1-10)")
    private int impactScore;
    
    @ApiModelProperty(value = "Risk assessment")
    private String riskLevel;
    
    @ApiModelProperty(value = "Recommended actions before deletion")
    private List<String> recommendations;

    // Default constructor
    public BulkDeletePreview() {
        this.criticalIpRanges = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.scopeBreakdown = new HashMap<>();
        this.typeBreakdown = new HashMap<>();
        this.entries = new ArrayList<>();
        this.recommendations = new ArrayList<>();
        this.riskLevel = "LOW";
        this.impactScore = 1;
    }

    // Constructor with all basic fields
    public BulkDeletePreview(int totalEntries, int activeEntries, int inactiveEntries,
                           List<String> criticalIpRanges, List<String> warnings,
                           Map<String, Integer> scopeBreakdown, Map<String, Integer> typeBreakdown,
                           List<PreviewEntry> entries) {
        this();
        this.totalEntries = totalEntries;
        this.activeEntries = activeEntries;
        this.inactiveEntries = inactiveEntries;
        this.criticalIpRanges = criticalIpRanges != null ? criticalIpRanges : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.scopeBreakdown = scopeBreakdown != null ? scopeBreakdown : new HashMap<>();
        this.typeBreakdown = typeBreakdown != null ? typeBreakdown : new HashMap<>();
        this.entries = entries != null ? entries : new ArrayList<>();
        calculateRiskAssessment();
    }

    // Builder pattern methods for easier construction
    public BulkDeletePreview withTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
        return this;
    }

    public BulkDeletePreview withActiveEntries(int activeEntries) {
        this.activeEntries = activeEntries;
        return this;
    }

    public BulkDeletePreview withInactiveEntries(int inactiveEntries) {
        this.inactiveEntries = inactiveEntries;
        return this;
    }

    public BulkDeletePreview withCriticalIpRanges(List<String> criticalIpRanges) {
        this.criticalIpRanges = criticalIpRanges != null ? criticalIpRanges : new ArrayList<>();
        calculateRiskAssessment();
        return this;
    }

    public BulkDeletePreview addCriticalIpRange(String ipRange) {
        if (this.criticalIpRanges == null) {
            this.criticalIpRanges = new ArrayList<>();
        }
        this.criticalIpRanges.add(ipRange);
        calculateRiskAssessment();
        return this;
    }

    public BulkDeletePreview withWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        return this;
    }

    public BulkDeletePreview addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
        return this;
    }

    public BulkDeletePreview withScopeBreakdown(Map<String, Integer> scopeBreakdown) {
        this.scopeBreakdown = scopeBreakdown != null ? scopeBreakdown : new HashMap<>();
        return this;
    }

    public BulkDeletePreview withTypeBreakdown(Map<String, Integer> typeBreakdown) {
        this.typeBreakdown = typeBreakdown != null ? typeBreakdown : new HashMap<>();
        return this;
    }

    public BulkDeletePreview withEntries(List<PreviewEntry> entries) {
        this.entries = entries != null ? entries : new ArrayList<>();
        return this;
    }

    public BulkDeletePreview addEntry(PreviewEntry entry) {
        if (this.entries == null) {
            this.entries = new ArrayList<>();
        }
        this.entries.add(entry);
        return this;
    }

    public BulkDeletePreview addRecommendation(String recommendation) {
        if (this.recommendations == null) {
            this.recommendations = new ArrayList<>();
        }
        this.recommendations.add(recommendation);
        return this;
    }

    // Calculate risk assessment based on current data
    private void calculateRiskAssessment() {
        int score = 1; // Base score
        
        // Increase score based on critical IPs
        if (criticalIpRanges != null && !criticalIpRanges.isEmpty()) {
            score += criticalIpRanges.size() * 3;
        }
        
        // Increase score based on active entries
        if (activeEntries > 0) {
            score += Math.min(activeEntries, 5); // Cap at 5 points
        }
        
        // Increase score for large deletions
        if (totalEntries > 50) {
            score += 2;
        } else if (totalEntries > 20) {
            score += 1;
        }
        
        // Set risk level based on score
        this.impactScore = Math.min(score, 10); // Cap at 10
        
        if (score >= 8) {
            this.riskLevel = "CRITICAL";
        } else if (score >= 5) {
            this.riskLevel = "HIGH";
        } else if (score >= 3) {
            this.riskLevel = "MEDIUM";
        } else {
            this.riskLevel = "LOW";
        }
        
        // Generate recommendations based on risk
        generateRecommendations();
    }

    private void generateRecommendations() {
        if (this.recommendations == null) {
            this.recommendations = new ArrayList<>();
        }
        this.recommendations.clear();
        
        if (criticalIpRanges != null && !criticalIpRanges.isEmpty()) {
            recommendations.add("Review critical IP ranges before deletion");
            recommendations.add("Ensure alternative access methods are available");
        }
        
        if (activeEntries > 0) {
            recommendations.add("Consider the impact on active user sessions");
            recommendations.add("Notify affected users before deletion");
        }
        
        if (totalEntries > 20) {
            recommendations.add("Consider performing deletion in smaller batches");
        }
        
        if ("CRITICAL".equals(riskLevel) || "HIGH".equals(riskLevel)) {
            recommendations.add("Backup current IP whitelist configuration");
            recommendations.add("Have rollback plan ready");
        }
    }

    // All getters and setters
    public int getTotalEntries() {
        return totalEntries;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
        calculateRiskAssessment();
    }

    public int getActiveEntries() {
        return activeEntries;
    }

    public void setActiveEntries(int activeEntries) {
        this.activeEntries = activeEntries;
        calculateRiskAssessment();
    }

    public int getInactiveEntries() {
        return inactiveEntries;
    }

    public void setInactiveEntries(int inactiveEntries) {
        this.inactiveEntries = inactiveEntries;
    }

    public List<String> getCriticalIpRanges() {
        return criticalIpRanges;
    }

    public void setCriticalIpRanges(List<String> criticalIpRanges) {
        this.criticalIpRanges = criticalIpRanges;
        calculateRiskAssessment();
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Map<String, Integer> getScopeBreakdown() {
        return scopeBreakdown;
    }

    public void setScopeBreakdown(Map<String, Integer> scopeBreakdown) {
        this.scopeBreakdown = scopeBreakdown;
    }

    public Map<String, Integer> getTypeBreakdown() {
        return typeBreakdown;
    }

    public void setTypeBreakdown(Map<String, Integer> typeBreakdown) {
        this.typeBreakdown = typeBreakdown;
    }

    public List<PreviewEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<PreviewEntry> entries) {
        this.entries = entries;
    }

    public int getImpactScore() {
        return impactScore;
    }

    public void setImpactScore(int impactScore) {
        this.impactScore = Math.max(1, Math.min(10, impactScore)); // Ensure between 1-10
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }

    // Utility methods
    public boolean hasCriticalIps() {
        return criticalIpRanges != null && !criticalIpRanges.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public boolean isHighRisk() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    public int getCriticalEntriesCount() {
        if (entries == null) return 0;
        return (int) entries.stream().filter(PreviewEntry::isCritical).count();
    }

    @Override
    public String toString() {
        return "BulkDeletePreview{" +
                "totalEntries=" + totalEntries +
                ", activeEntries=" + activeEntries +
                ", inactiveEntries=" + inactiveEntries +
                ", criticalIpRanges=" + (criticalIpRanges != null ? criticalIpRanges.size() : 0) +
                ", warnings=" + (warnings != null ? warnings.size() : 0) +
                ", riskLevel='" + riskLevel + '\'' +
                ", impactScore=" + impactScore +
                '}';
    }
}