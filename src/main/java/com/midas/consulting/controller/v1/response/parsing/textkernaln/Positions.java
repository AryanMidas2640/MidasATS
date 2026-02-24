package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import com.midas.consulting.controller.v1.response.parsing.textkernal.StartDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Positions {

   @SerializedName("Id")
   Date Id;

   @SerializedName("Employer")
   com.midas.consulting.controller.v1.response.parsing.textkernal.Employer Employer;

   @SerializedName("IsSelfEmployed")
   boolean IsSelfEmployed;

   @SerializedName("IsCurrent")
   boolean IsCurrent;

   @SerializedName("JobTitle")
   com.midas.consulting.controller.v1.response.parsing.textkernal.JobTitle JobTitle;

   @SerializedName("StartDate")
   com.midas.consulting.controller.v1.response.parsing.textkernal.StartDate StartDate;

   @SerializedName("EndDate")
   com.midas.consulting.controller.v1.response.parsing.textkernal.EndDate EndDate;

   @SerializedName("JobType")
   String JobType;

   @SerializedName("JobLevel")
   String JobLevel;

   @SerializedName("TaxonomyPercentage")
   int TaxonomyPercentage;

   @SerializedName("Description")
   String Description;


    public void setId(Date Id) {
        this.Id = Id;
    }
    public Date getId() {
        return Id;
    }
    
    public void setEmployer(com.midas.consulting.controller.v1.response.parsing.textkernal.Employer Employer) {
        this.Employer = Employer;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernal.Employer getEmployer() {
        return Employer;
    }
    
    public void setIsSelfEmployed(boolean IsSelfEmployed) {
        this.IsSelfEmployed = IsSelfEmployed;
    }
    public boolean getIsSelfEmployed() {
        return IsSelfEmployed;
    }
    
    public void setIsCurrent(boolean IsCurrent) {
        this.IsCurrent = IsCurrent;
    }
    public boolean getIsCurrent() {
        return IsCurrent;
    }
    
    public void setJobTitle(com.midas.consulting.controller.v1.response.parsing.textkernal.JobTitle JobTitle) {
        this.JobTitle = JobTitle;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernal.JobTitle getJobTitle() {
        return JobTitle;
    }
    
    public void setStartDate(StartDate StartDate) {
        this.StartDate = StartDate;
    }
    public StartDate getStartDate() {
        return StartDate;
    }
    
    public void setEndDate(com.midas.consulting.controller.v1.response.parsing.textkernal.EndDate EndDate) {
        this.EndDate = EndDate;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernal.EndDate getEndDate() {
        return EndDate;
    }
    
    public void setJobType(String JobType) {
        this.JobType = JobType;
    }
    public String getJobType() {
        return JobType;
    }
    
    public void setJobLevel(String JobLevel) {
        this.JobLevel = JobLevel;
    }
    public String getJobLevel() {
        return JobLevel;
    }
    
    public void setTaxonomyPercentage(int TaxonomyPercentage) {
        this.TaxonomyPercentage = TaxonomyPercentage;
    }
    public int getTaxonomyPercentage() {
        return TaxonomyPercentage;
    }
    
    public void setDescription(String Description) {
        this.Description = Description;
    }
    public String getDescription() {
        return Description;
    }
    
}