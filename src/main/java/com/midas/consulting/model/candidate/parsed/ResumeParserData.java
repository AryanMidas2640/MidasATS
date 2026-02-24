package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeParserData{
    @JsonProperty("ResumeFileName")
private String resumeFileName;
    @JsonProperty("ResumeLanguage") 
private ResumeLanguage resumeLanguage;
    @JsonProperty("ParsingDate") 
private String parsingDate;
    @JsonProperty("ResumeCountry") 
private ResumeCountry resumeCountry;
    @JsonProperty("Name") 
private Name name;
    @JsonProperty("DateOfBirth") 
private String dateOfBirth;
    @JsonProperty("Gender") 
private String gender;
    @JsonProperty("FatherName") 
private String fatherName;
    @JsonProperty("MotherName") 
private String motherName;
    @JsonProperty("MaritalStatus") 
private String maritalStatus;
    @JsonProperty("Nationality") 
private String nationality;
    @JsonProperty("LanguageKnown") 
private ArrayList<LanguageKnown> languageKnown;
    @JsonProperty("UniqueID") 
private String uniqueID;
    @JsonProperty("LicenseNo") 
private String licenseNo;
    @JsonProperty("PassportDetail") 
private PassportDetail passportDetail;
    @JsonProperty("PanNo") 
private String panNo;
    @JsonProperty("VisaStatus") 
private String visaStatus;
    @JsonProperty("Email") 
private ArrayList<Email> email;
    @JsonProperty("PhoneNumber") 
private ArrayList<PhoneNumber> phoneNumber;
    @JsonProperty("WebSite") 
private ArrayList<WebSite> webSite;
    @JsonProperty("Address") 
private ArrayList<Address> address;
    @JsonProperty("Category") 
private String category;
    @JsonProperty("SubCategory") 
private String subCategory;
    @JsonProperty("CurrentSalary") 
private CurrentSalary currentSalary;
    @JsonProperty("ExpectedSalary") 
private ExpectedSalary expectedSalary;
    @JsonProperty("Qualification") 
private String qualification;
    @JsonProperty("SegregatedQualification") 
private ArrayList<SegregatedQualification> segregatedQualification;
    @JsonProperty("Certification") 
private String certification;
    @JsonProperty("SegregatedCertification") 
private ArrayList<Object> segregatedCertification;
    @JsonProperty("SkillBlock") 
private String skillBlock;
    @JsonProperty("SkillKeywords") 
private String skillKeywords;
    @JsonProperty("SegregatedSkill") 
private ArrayList<SegregatedSkill> segregatedSkill;
    @JsonProperty("Experience") 
private String experience;
    @JsonProperty("SegregatedExperience") 
private ArrayList<SegregatedExperience> segregatedExperience;
    @JsonProperty("CurrentEmployer") 
private String currentEmployer;
    @JsonProperty("JobProfile") 
private String jobProfile;
    @JsonProperty("WorkedPeriod") 
private WorkedPeriod workedPeriod;
    @JsonProperty("GapPeriod") 
private String gapPeriod;
    @JsonProperty("AverageStay") 
private String averageStay;
    @JsonProperty("LongestStay") 
private String longestStay;
    @JsonProperty("Summary") 
private String summary;
    @JsonProperty("ExecutiveSummary") 
private String executiveSummary;
    @JsonProperty("ManagementSummary") 
private String managementSummary;
    @JsonProperty("Coverletter") 
private String coverletter;
    @JsonProperty("Publication") 
private String publication;
    @JsonProperty("SegregatedPublication") 
private ArrayList<Object> segregatedPublication;
    @JsonProperty("CurrentLocation") 
private ArrayList<Object> currentLocation;
    @JsonProperty("PreferredLocation") 
private ArrayList<Object> preferredLocation;
    @JsonProperty("Availability") 
private String availability;
    @JsonProperty("Hobbies") 
private String hobbies;
    @JsonProperty("Objectives") 
private String objectives;
    @JsonProperty("Achievements") 
private String achievements;
    @JsonProperty("SegregatedAchievement") 
private ArrayList<Object> segregatedAchievement;
    @JsonProperty("References") 
private String references;
    @JsonProperty("CustomFields") 
private String customFields;
    @JsonProperty("EmailInfo") 
private EmailInfo emailInfo;
    @JsonProperty("Recommendations") 
private ArrayList<Recommendation> recommendations;
    @JsonProperty("DetailResume") 
private String detailResume;
    @JsonProperty("HtmlResume") 
private String htmlResume;
    @JsonProperty("CandidateImage") 
private CandidateImage candidateImage;
    @JsonProperty("TemplateOutput") 
private TemplateOutput templateOutput;
    @JsonProperty("ApiInfo") 
private ApiInfo apiInfo;
}
