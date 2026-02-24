package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperienceSummary {

   @SerializedName("Description")
   String Description;

   @SerializedName("MonthsOfWorkExperience")
   int MonthsOfWorkExperience;

   @SerializedName("MonthsOfManagementExperience")
   int MonthsOfManagementExperience;

   @SerializedName("ExecutiveType")
   String ExecutiveType;

   @SerializedName("AverageMonthsPerEmployer")
   int AverageMonthsPerEmployer;

   @SerializedName("FulltimeDirectHirePredictiveIndex")
   int FulltimeDirectHirePredictiveIndex;

   @SerializedName("ManagementStory")
   String ManagementStory;

   @SerializedName("CurrentManagementLevel")
   String CurrentManagementLevel;

   @SerializedName("ManagementScore")
   int ManagementScore;

   @SerializedName("AttentionNeeded")
   String AttentionNeeded;


    public void setDescription(String Description) {
        this.Description = Description;
    }
    public String getDescription() {
        return Description;
    }
    
    public void setMonthsOfWorkExperience(int MonthsOfWorkExperience) {
        this.MonthsOfWorkExperience = MonthsOfWorkExperience;
    }
    public int getMonthsOfWorkExperience() {
        return MonthsOfWorkExperience;
    }
    
    public void setMonthsOfManagementExperience(int MonthsOfManagementExperience) {
        this.MonthsOfManagementExperience = MonthsOfManagementExperience;
    }
    public int getMonthsOfManagementExperience() {
        return MonthsOfManagementExperience;
    }
    
    public void setExecutiveType(String ExecutiveType) {
        this.ExecutiveType = ExecutiveType;
    }
    public String getExecutiveType() {
        return ExecutiveType;
    }
    
    public void setAverageMonthsPerEmployer(int AverageMonthsPerEmployer) {
        this.AverageMonthsPerEmployer = AverageMonthsPerEmployer;
    }
    public int getAverageMonthsPerEmployer() {
        return AverageMonthsPerEmployer;
    }
    
    public void setFulltimeDirectHirePredictiveIndex(int FulltimeDirectHirePredictiveIndex) {
        this.FulltimeDirectHirePredictiveIndex = FulltimeDirectHirePredictiveIndex;
    }
    public int getFulltimeDirectHirePredictiveIndex() {
        return FulltimeDirectHirePredictiveIndex;
    }
    
    public void setManagementStory(String ManagementStory) {
        this.ManagementStory = ManagementStory;
    }
    public String getManagementStory() {
        return ManagementStory;
    }
    
    public void setCurrentManagementLevel(String CurrentManagementLevel) {
        this.CurrentManagementLevel = CurrentManagementLevel;
    }
    public String getCurrentManagementLevel() {
        return CurrentManagementLevel;
    }
    
    public void setManagementScore(int ManagementScore) {
        this.ManagementScore = ManagementScore;
    }
    public int getManagementScore() {
        return ManagementScore;
    }
    
    public void setAttentionNeeded(String AttentionNeeded) {
        this.AttentionNeeded = AttentionNeeded;
    }
    public String getAttentionNeeded() {
        return AttentionNeeded;
    }
    
}