package com.midas.consulting.util.testpojos;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"job_code",
"job_title",
"bill_rate",
"pay_rate",
"job_start_date",
"job_end_date",
"remote_job",
"country",
"states",
"city",
"zip_code",
"job_status",
"job_type",
"client",
"client_manager",
"end_client",
"client_job_id",
"required_documents_for_submissions",
"turnaround_time",
"priority",
"duration",
"work_authorization",
"ceipal_ref__",
"clearance",
"address",
"degree",
"experience",
"skills",
"languages",
"number_of_positions",
"maximum_allowed_submissions",
"tax_terms",
"sales_manager",
"department",
"recruitment_manager",
"account_manager",
"assigned_to",
"primary_recruiter",
"comments",
"additional_notifications",
"career_portal_published_date",
"job_description",
"post_job_on_career_portal",
"display_contact_details_on_career_portal",
"location",
"notice_period",
"referral_bonus",
"job_category",
"public_job_description",
"offerings",
"profession",
"speciality",
"required_certifications_for_submission",
"onboarding_owner",
"required_license_for_submission",
"no__of_beds",
"required_documents_for_eboarding",
"required_certifications_for_eboarding",
"required_license_for_eboarding",
"facility",
"vms_fee_percentage",
"required_skill_checklist_for_submission"
})
@Generated("jsonschema2pojo")
public class Ciepal {

@JsonProperty("job_code")
private String jobCode;
@JsonProperty("job_title")
private String jobTitle;
@JsonProperty("bill_rate")
private String billRate;
@JsonProperty("pay_rate")
private String payRate;
@JsonProperty("job_start_date")
private String jobStartDate;
@JsonProperty("job_end_date")
private String jobEndDate;
@JsonProperty("remote_job")
private String remoteJob;
@JsonProperty("country")
private String country;
@JsonProperty("states")
private String states;
@JsonProperty("city")
private String city;
@JsonProperty("zip_code")
private String zipCode;
@JsonProperty("job_status")
private String jobStatus;
@JsonProperty("job_type")
private String jobType;
@JsonProperty("client")
private String client;
@JsonProperty("client_manager")
private String clientManager;
@JsonProperty("end_client")
private String endClient;
@JsonProperty("client_job_id")
private String clientJobId;
@JsonProperty("required_documents_for_submissions")
private String requiredDocumentsForSubmissions;
@JsonProperty("turnaround_time")
private String turnaroundTime;
@JsonProperty("priority")
private String priority;
@JsonProperty("duration")
private String duration;
@JsonProperty("work_authorization")
private String workAuthorization;
@JsonProperty("ceipal_ref__")
private String ceipalRef;
@JsonProperty("clearance")
private String clearance;
@JsonProperty("address")
private String address;
@JsonProperty("degree")
private String degree;
@JsonProperty("experience")
private String experience;
@JsonProperty("skills")
private String skills;
@JsonProperty("languages")
private String languages;
@JsonProperty("number_of_positions")
private String numberOfPositions;
@JsonProperty("maximum_allowed_submissions")
private String maximumAllowedSubmissions;
@JsonProperty("tax_terms")
private String taxTerms;
@JsonProperty("sales_manager")
private String salesManager;
@JsonProperty("department")
private String department;
@JsonProperty("recruitment_manager")
private String recruitmentManager;
@JsonProperty("account_manager")
private String accountManager;
@JsonProperty("assigned_to")
private String assignedTo;
@JsonProperty("primary_recruiter")
private String primaryRecruiter;
@JsonProperty("comments")
private String comments;
@JsonProperty("additional_notifications")
private String additionalNotifications;
@JsonProperty("career_portal_published_date")
private String careerPortalPublishedDate;
@JsonProperty("job_description")
private String jobDescription;
@JsonProperty("post_job_on_career_portal")
private String postJobOnCareerPortal;
@JsonProperty("display_contact_details_on_career_portal")
private String displayContactDetailsOnCareerPortal;
@JsonProperty("location")
private String location;
@JsonProperty("notice_period")
private String noticePeriod;
@JsonProperty("referral_bonus")
private String referralBonus;
@JsonProperty("job_category")
private String jobCategory;
@JsonProperty("public_job_description")
private String publicJobDescription;
@JsonProperty("offerings")
private String offerings;
@JsonProperty("profession")
private String profession;
@JsonProperty("speciality")
private String speciality;
@JsonProperty("required_certifications_for_submission")
private String requiredCertificationsForSubmission;
@JsonProperty("onboarding_owner")
private String onboardingOwner;
@JsonProperty("required_license_for_submission")
private String requiredLicenseForSubmission;
@JsonProperty("no__of_beds")
private String noOfBeds;
@JsonProperty("required_documents_for_eboarding")
private String requiredDocumentsForEboarding;
@JsonProperty("required_certifications_for_eboarding")
private String requiredCertificationsForEboarding;
@JsonProperty("required_license_for_eboarding")
private String requiredLicenseForEboarding;
@JsonProperty("facility")
private String facility;
@JsonProperty("vms_fee_percentage")
private String vmsFeePercentage;
@JsonProperty("required_skill_checklist_for_submission")
private String requiredSkillChecklistForSubmission;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("job_code")
public String getJobCode() {
return jobCode;
}

@JsonProperty("job_code")
public void setJobCode(String jobCode) {
this.jobCode = jobCode;
}

@JsonProperty("job_title")
public String getJobTitle() {
return jobTitle;
}

@JsonProperty("job_title")
public void setJobTitle(String jobTitle) {
this.jobTitle = jobTitle;
}

@JsonProperty("bill_rate")
public String getBillRate() {
return billRate;
}

@JsonProperty("bill_rate")
public void setBillRate(String billRate) {
this.billRate = billRate;
}

@JsonProperty("pay_rate")
public String getPayRate() {
return payRate;
}

@JsonProperty("pay_rate")
public void setPayRate(String payRate) {
this.payRate = payRate;
}

@JsonProperty("job_start_date")
public String getJobStartDate() {
return jobStartDate;
}

@JsonProperty("job_start_date")
public void setJobStartDate(String jobStartDate) {
this.jobStartDate = jobStartDate;
}

@JsonProperty("job_end_date")
public String getJobEndDate() {
return jobEndDate;
}

@JsonProperty("job_end_date")
public void setJobEndDate(String jobEndDate) {
this.jobEndDate = jobEndDate;
}

@JsonProperty("remote_job")
public String getRemoteJob() {
return remoteJob;
}

@JsonProperty("remote_job")
public void setRemoteJob(String remoteJob) {
this.remoteJob = remoteJob;
}

@JsonProperty("country")
public String getCountry() {
return country;
}

@JsonProperty("country")
public void setCountry(String country) {
this.country = country;
}

@JsonProperty("states")
public String getStates() {
return states;
}

@JsonProperty("states")
public void setStates(String states) {
this.states = states;
}

@JsonProperty("city")
public String getCity() {
return city;
}

@JsonProperty("city")
public void setCity(String city) {
this.city = city;
}

@JsonProperty("zip_code")
public String getZipCode() {
return zipCode;
}

@JsonProperty("zip_code")
public void setZipCode(String zipCode) {
this.zipCode = zipCode;
}

@JsonProperty("job_status")
public String getJobStatus() {
return jobStatus;
}

@JsonProperty("job_status")
public void setJobStatus(String jobStatus) {
this.jobStatus = jobStatus;
}

@JsonProperty("job_type")
public String getJobType() {
return jobType;
}

@JsonProperty("job_type")
public void setJobType(String jobType) {
this.jobType = jobType;
}

@JsonProperty("client")
public String getClient() {
return client;
}

@JsonProperty("client")
public void setClient(String client) {
this.client = client;
}

@JsonProperty("client_manager")
public String getClientManager() {
return clientManager;
}

@JsonProperty("client_manager")
public void setClientManager(String clientManager) {
this.clientManager = clientManager;
}

@JsonProperty("end_client")
public String getEndClient() {
return endClient;
}

@JsonProperty("end_client")
public void setEndClient(String endClient) {
this.endClient = endClient;
}

@JsonProperty("client_job_id")
public String getClientJobId() {
return clientJobId;
}

@JsonProperty("client_job_id")
public void setClientJobId(String clientJobId) {
this.clientJobId = clientJobId;
}

@JsonProperty("required_documents_for_submissions")
public String getRequiredDocumentsForSubmissions() {
return requiredDocumentsForSubmissions;
}

@JsonProperty("required_documents_for_submissions")
public void setRequiredDocumentsForSubmissions(String requiredDocumentsForSubmissions) {
this.requiredDocumentsForSubmissions = requiredDocumentsForSubmissions;
}

@JsonProperty("turnaround_time")
public String getTurnaroundTime() {
return turnaroundTime;
}

@JsonProperty("turnaround_time")
public void setTurnaroundTime(String turnaroundTime) {
this.turnaroundTime = turnaroundTime;
}

@JsonProperty("priority")
public String getPriority() {
return priority;
}

@JsonProperty("priority")
public void setPriority(String priority) {
this.priority = priority;
}

@JsonProperty("duration")
public String getDuration() {
return duration;
}

@JsonProperty("duration")
public void setDuration(String duration) {
this.duration = duration;
}

@JsonProperty("work_authorization")
public String getWorkAuthorization() {
return workAuthorization;
}

@JsonProperty("work_authorization")
public void setWorkAuthorization(String workAuthorization) {
this.workAuthorization = workAuthorization;
}

@JsonProperty("ceipal_ref__")
public String getCeipalRef() {
return ceipalRef;
}

@JsonProperty("ceipal_ref__")
public void setCeipalRef(String ceipalRef) {
this.ceipalRef = ceipalRef;
}

@JsonProperty("clearance")
public String getClearance() {
return clearance;
}

@JsonProperty("clearance")
public void setClearance(String clearance) {
this.clearance = clearance;
}

@JsonProperty("address")
public String getAddress() {
return address;
}

@JsonProperty("address")
public void setAddress(String address) {
this.address = address;
}

@JsonProperty("degree")
public String getDegree() {
return degree;
}

@JsonProperty("degree")
public void setDegree(String degree) {
this.degree = degree;
}

@JsonProperty("experience")
public String getExperience() {
return experience;
}

@JsonProperty("experience")
public void setExperience(String experience) {
this.experience = experience;
}

@JsonProperty("skills")
public String getSkills() {
return skills;
}

@JsonProperty("skills")
public void setSkills(String skills) {
this.skills = skills;
}

@JsonProperty("languages")
public String getLanguages() {
return languages;
}

@JsonProperty("languages")
public void setLanguages(String languages) {
this.languages = languages;
}

@JsonProperty("number_of_positions")
public String getNumberOfPositions() {
return numberOfPositions;
}

@JsonProperty("number_of_positions")
public void setNumberOfPositions(String numberOfPositions) {
this.numberOfPositions = numberOfPositions;
}

@JsonProperty("maximum_allowed_submissions")
public String getMaximumAllowedSubmissions() {
return maximumAllowedSubmissions;
}

@JsonProperty("maximum_allowed_submissions")
public void setMaximumAllowedSubmissions(String maximumAllowedSubmissions) {
this.maximumAllowedSubmissions = maximumAllowedSubmissions;
}

@JsonProperty("tax_terms")
public String getTaxTerms() {
return taxTerms;
}

@JsonProperty("tax_terms")
public void setTaxTerms(String taxTerms) {
this.taxTerms = taxTerms;
}

@JsonProperty("sales_manager")
public String getSalesManager() {
return salesManager;
}

@JsonProperty("sales_manager")
public void setSalesManager(String salesManager) {
this.salesManager = salesManager;
}

@JsonProperty("department")
public String getDepartment() {
return department;
}

@JsonProperty("department")
public void setDepartment(String department) {
this.department = department;
}

@JsonProperty("recruitment_manager")
public String getRecruitmentManager() {
return recruitmentManager;
}

@JsonProperty("recruitment_manager")
public void setRecruitmentManager(String recruitmentManager) {
this.recruitmentManager = recruitmentManager;
}

@JsonProperty("account_manager")
public String getAccountManager() {
return accountManager;
}

@JsonProperty("account_manager")
public void setAccountManager(String accountManager) {
this.accountManager = accountManager;
}

@JsonProperty("assigned_to")
public String getAssignedTo() {
return assignedTo;
}

@JsonProperty("assigned_to")
public void setAssignedTo(String assignedTo) {
this.assignedTo = assignedTo;
}

@JsonProperty("primary_recruiter")
public String getPrimaryRecruiter() {
return primaryRecruiter;
}

@JsonProperty("primary_recruiter")
public void setPrimaryRecruiter(String primaryRecruiter) {
this.primaryRecruiter = primaryRecruiter;
}

@JsonProperty("comments")
public String getComments() {
return comments;
}

@JsonProperty("comments")
public void setComments(String comments) {
this.comments = comments;
}

@JsonProperty("additional_notifications")
public String getAdditionalNotifications() {
return additionalNotifications;
}

@JsonProperty("additional_notifications")
public void setAdditionalNotifications(String additionalNotifications) {
this.additionalNotifications = additionalNotifications;
}

@JsonProperty("career_portal_published_date")
public String getCareerPortalPublishedDate() {
return careerPortalPublishedDate;
}

@JsonProperty("career_portal_published_date")
public void setCareerPortalPublishedDate(String careerPortalPublishedDate) {
this.careerPortalPublishedDate = careerPortalPublishedDate;
}

@JsonProperty("job_description")
public String getJobDescription() {
return jobDescription;
}

@JsonProperty("job_description")
public void setJobDescription(String jobDescription) {
this.jobDescription = jobDescription;
}

@JsonProperty("post_job_on_career_portal")
public String getPostJobOnCareerPortal() {
return postJobOnCareerPortal;
}

@JsonProperty("post_job_on_career_portal")
public void setPostJobOnCareerPortal(String postJobOnCareerPortal) {
this.postJobOnCareerPortal = postJobOnCareerPortal;
}

@JsonProperty("display_contact_details_on_career_portal")
public String getDisplayContactDetailsOnCareerPortal() {
return displayContactDetailsOnCareerPortal;
}

@JsonProperty("display_contact_details_on_career_portal")
public void setDisplayContactDetailsOnCareerPortal(String displayContactDetailsOnCareerPortal) {
this.displayContactDetailsOnCareerPortal = displayContactDetailsOnCareerPortal;
}

@JsonProperty("location")
public String getLocation() {
return location;
}

@JsonProperty("location")
public void setLocation(String location) {
this.location = location;
}

@JsonProperty("notice_period")
public String getNoticePeriod() {
return noticePeriod;
}

@JsonProperty("notice_period")
public void setNoticePeriod(String noticePeriod) {
this.noticePeriod = noticePeriod;
}

@JsonProperty("referral_bonus")
public String getReferralBonus() {
return referralBonus;
}

@JsonProperty("referral_bonus")
public void setReferralBonus(String referralBonus) {
this.referralBonus = referralBonus;
}

@JsonProperty("job_category")
public String getJobCategory() {
return jobCategory;
}

@JsonProperty("job_category")
public void setJobCategory(String jobCategory) {
this.jobCategory = jobCategory;
}

@JsonProperty("public_job_description")
public String getPublicJobDescription() {
return publicJobDescription;
}

@JsonProperty("public_job_description")
public void setPublicJobDescription(String publicJobDescription) {
this.publicJobDescription = publicJobDescription;
}

@JsonProperty("offerings")
public String getOfferings() {
return offerings;
}

@JsonProperty("offerings")
public void setOfferings(String offerings) {
this.offerings = offerings;
}

@JsonProperty("profession")
public String getProfession() {
return profession;
}

@JsonProperty("profession")
public void setProfession(String profession) {
this.profession = profession;
}

@JsonProperty("speciality")
public String getSpeciality() {
return speciality;
}

@JsonProperty("speciality")
public void setSpeciality(String speciality) {
this.speciality = speciality;
}

@JsonProperty("required_certifications_for_submission")
public String getRequiredCertificationsForSubmission() {
return requiredCertificationsForSubmission;
}

@JsonProperty("required_certifications_for_submission")
public void setRequiredCertificationsForSubmission(String requiredCertificationsForSubmission) {
this.requiredCertificationsForSubmission = requiredCertificationsForSubmission;
}

@JsonProperty("onboarding_owner")
public String getOnboardingOwner() {
return onboardingOwner;
}

@JsonProperty("onboarding_owner")
public void setOnboardingOwner(String onboardingOwner) {
this.onboardingOwner = onboardingOwner;
}

@JsonProperty("required_license_for_submission")
public String getRequiredLicenseForSubmission() {
return requiredLicenseForSubmission;
}

@JsonProperty("required_license_for_submission")
public void setRequiredLicenseForSubmission(String requiredLicenseForSubmission) {
this.requiredLicenseForSubmission = requiredLicenseForSubmission;
}

@JsonProperty("no__of_beds")
public String getNoOfBeds() {
return noOfBeds;
}

@JsonProperty("no__of_beds")
public void setNoOfBeds(String noOfBeds) {
this.noOfBeds = noOfBeds;
}

@JsonProperty("required_documents_for_eboarding")
public String getRequiredDocumentsForEboarding() {
return requiredDocumentsForEboarding;
}

@JsonProperty("required_documents_for_eboarding")
public void setRequiredDocumentsForEboarding(String requiredDocumentsForEboarding) {
this.requiredDocumentsForEboarding = requiredDocumentsForEboarding;
}

@JsonProperty("required_certifications_for_eboarding")
public String getRequiredCertificationsForEboarding() {
return requiredCertificationsForEboarding;
}

@JsonProperty("required_certifications_for_eboarding")
public void setRequiredCertificationsForEboarding(String requiredCertificationsForEboarding) {
this.requiredCertificationsForEboarding = requiredCertificationsForEboarding;
}

@JsonProperty("required_license_for_eboarding")
public String getRequiredLicenseForEboarding() {
return requiredLicenseForEboarding;
}

@JsonProperty("required_license_for_eboarding")
public void setRequiredLicenseForEboarding(String requiredLicenseForEboarding) {
this.requiredLicenseForEboarding = requiredLicenseForEboarding;
}

@JsonProperty("facility")
public String getFacility() {
return facility;
}

@JsonProperty("facility")
public void setFacility(String facility) {
this.facility = facility;
}

@JsonProperty("vms_fee_percentage")
public String getVmsFeePercentage() {
return vmsFeePercentage;
}

@JsonProperty("vms_fee_percentage")
public void setVmsFeePercentage(String vmsFeePercentage) {
this.vmsFeePercentage = vmsFeePercentage;
}

@JsonProperty("required_skill_checklist_for_submission")
public String getRequiredSkillChecklistForSubmission() {
return requiredSkillChecklistForSubmission;
}

@JsonProperty("required_skill_checklist_for_submission")
public void setRequiredSkillChecklistForSubmission(String requiredSkillChecklistForSubmission) {
this.requiredSkillChecklistForSubmission = requiredSkillChecklistForSubmission;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}