package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class RedactedResumeData{
    @JsonProperty("ContactInformation")
    public ContactInformation contactInformation;
    @JsonProperty("ProfessionalSummary") 
    public String professionalSummary;
    @JsonProperty("Objective") 
    public String objective;
    @JsonProperty("CoverLetter") 
    public String coverLetter;
    @JsonProperty("PersonalAttributes") 
    public PersonalAttributes personalAttributes;
    @JsonProperty("Education") 
    public Education education;
    @JsonProperty("EmploymentHistory") 
    public EmploymentHistory employmentHistory;
    @JsonProperty("SkillsData") 
    public ArrayList<SkillsDatum> skillsData;
    @JsonProperty("Certifications") 
    public ArrayList<Certification> certifications;
    @JsonProperty("Licenses") 
    public ArrayList<License> licenses;
    @JsonProperty("Associations") 
    public ArrayList<Association> associations;
    @JsonProperty("LanguageCompetencies") 
    public ArrayList<LanguageCompetency> languageCompetencies;
    @JsonProperty("MilitaryExperience") 
    public ArrayList<MilitaryExperience> militaryExperience;
    @JsonProperty("SecurityCredentials") 
    public ArrayList<SecurityCredential> securityCredentials;
    @JsonProperty("References") 
    public ArrayList<Reference> references;
    @JsonProperty("Achievements") 
    public ArrayList<String> achievements;
    @JsonProperty("Training") 
    public Training training;
    @JsonProperty("Hobbies") 
    public String hobbies;
    @JsonProperty("Patents") 
    public String patents;
    @JsonProperty("Publications") 
    public String publications;
    @JsonProperty("SpeakingEngagements") 
    public String speakingEngagements;
    @JsonProperty("ResumeMetadata") 
    public ResumeMetadata resumeMetadata;
    @JsonProperty("UserDefinedTags") 
    public ArrayList<String> userDefinedTags;
}

