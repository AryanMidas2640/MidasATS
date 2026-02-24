package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedactedResumeData {

   @SerializedName("ContactInformation")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.ContactInformation ContactInformation;

   @SerializedName("ProfessionalSummary")
   String ProfessionalSummary;

   @SerializedName("Education")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.Education Education;

   @SerializedName("EmploymentHistory")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.EmploymentHistory EmploymentHistory;

   @SerializedName("Skills")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.Skills Skills;

   @SerializedName("Certifications")
   List<Certifications> Certifications;

   @SerializedName("LanguageCompetencies")
   List<LanguageCompetencies> LanguageCompetencies;

   @SerializedName("QualificationsSummary")
   String QualificationsSummary;

   @SerializedName("ResumeMetadata")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.ResumeMetadata ResumeMetadata;


    public void setContactInformation(com.midas.consulting.controller.v1.response.parsing.textkernaln.ContactInformation ContactInformation) {
        this.ContactInformation = ContactInformation;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernaln.ContactInformation getContactInformation() {
        return ContactInformation;
    }
    
    public void setProfessionalSummary(String ProfessionalSummary) {
        this.ProfessionalSummary = ProfessionalSummary;
    }
    public String getProfessionalSummary() {
        return ProfessionalSummary;
    }
    
    public void setEducation(com.midas.consulting.controller.v1.response.parsing.textkernaln.Education Education) {
        this.Education = Education;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernaln.Education getEducation() {
        return Education;
    }
    
    public void setEmploymentHistory(com.midas.consulting.controller.v1.response.parsing.textkernaln.EmploymentHistory EmploymentHistory) {
        this.EmploymentHistory = EmploymentHistory;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernaln.EmploymentHistory getEmploymentHistory() {
        return EmploymentHistory;
    }
    
    public void setSkills(Skills Skills) {
        this.Skills = Skills;
    }
    public Skills getSkills() {
        return Skills;
    }
    
    public void setCertifications(List<Certifications> Certifications) {
        this.Certifications = Certifications;
    }
    public List<Certifications> getCertifications() {
        return Certifications;
    }
    
    public void setLanguageCompetencies(List<LanguageCompetencies> LanguageCompetencies) {
        this.LanguageCompetencies = LanguageCompetencies;
    }
    public List<LanguageCompetencies> getLanguageCompetencies() {
        return LanguageCompetencies;
    }
    
    public void setQualificationsSummary(String QualificationsSummary) {
        this.QualificationsSummary = QualificationsSummary;
    }
    public String getQualificationsSummary() {
        return QualificationsSummary;
    }
    
    public void setResumeMetadata(ResumeMetadata ResumeMetadata) {
        this.ResumeMetadata = ResumeMetadata;
    }
    public ResumeMetadata getResumeMetadata() {
        return ResumeMetadata;
    }
    
}