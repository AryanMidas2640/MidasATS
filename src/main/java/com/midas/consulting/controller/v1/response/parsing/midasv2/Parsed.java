package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"address",
"certifications",
"companies_worked_at",
"degree",
"designition",
"education",
"email",
"experience",
"full_text",
"github",
"languages",
"linkedin",
"name",
"phone",
"projects",
"resume_segments",
"skills",
"summary",
"total_experience",
"university",
"website"
})
@Generated("jsonschema2pojo")
public class Parsed {

@JsonProperty("address")
private Address address;
@JsonProperty("certifications")
private List<String> certifications;
@JsonProperty("companies_worked_at")
private List<String> companiesWorkedAt;
@JsonProperty("degree")
private List<String> degree;
@JsonProperty("designition")
private List<String> designition;
@JsonProperty("education")
private List<Education> education;
@JsonProperty("email")
private String email;
@JsonProperty("experience")
private List<Experience> experience;
@JsonProperty("full_text")
private String fullText;
@JsonProperty("github")
private String github;
@JsonProperty("languages")
private List<String> languages;
@JsonProperty("linkedin")
private String linkedin;
@JsonProperty("name")
private String name;
@JsonProperty("phone")
private String phone;
@JsonProperty("projects")
private List<Object> projects;
@JsonProperty("resume_segments")
private ResumeSegments resumeSegments;
@JsonProperty("skills")
private List<String> skills;
@JsonProperty("summary")
private String summary;
@JsonProperty("total_experience")
private Integer totalExperience;
@JsonProperty("university")
private List<Object> university;
@JsonProperty("website")
private String website;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("address")
public Address getAddress() {
return address;
}

@JsonProperty("address")
public void setAddress(Address address) {
this.address = address;
}

@JsonProperty("certifications")
public List<String> getCertifications() {
return certifications;
}

@JsonProperty("certifications")
public void setCertifications(List<String> certifications) {
this.certifications = certifications;
}

@JsonProperty("companies_worked_at")
public List<String> getCompaniesWorkedAt() {
return companiesWorkedAt;
}

@JsonProperty("companies_worked_at")
public void setCompaniesWorkedAt(List<String> companiesWorkedAt) {
this.companiesWorkedAt = companiesWorkedAt;
}

@JsonProperty("degree")
public List<String> getDegree() {
return degree;
}

@JsonProperty("degree")
public void setDegree(List<String> degree) {
this.degree = degree;
}

@JsonProperty("designition")
public List<String> getDesignition() {
return designition;
}

@JsonProperty("designition")
public void setDesignition(List<String> designition) {
this.designition = designition;
}

@JsonProperty("education")
public List<Education> getEducation() {
return education;
}

@JsonProperty("education")
public void setEducation(List<Education> education) {
this.education = education;
}

@JsonProperty("email")
public String getEmail() {
return email;
}

@JsonProperty("email")
public void setEmail(String email) {
this.email = email;
}

@JsonProperty("experience")
public List<Experience> getExperience() {
return experience;
}

@JsonProperty("experience")
public void setExperience(List<Experience> experience) {
this.experience = experience;
}

@JsonProperty("full_text")
public String getFullText() {
return fullText;
}

@JsonProperty("full_text")
public void setFullText(String fullText) {
this.fullText = fullText;
}

@JsonProperty("github")
public String getGithub() {
return github;
}

@JsonProperty("github")
public void setGithub(String github) {
this.github = github;
}

@JsonProperty("languages")
public List<String> getLanguages() {
return languages;
}

@JsonProperty("languages")
public void setLanguages(List<String> languages) {
this.languages = languages;
}

@JsonProperty("linkedin")
public String getLinkedin() {
return linkedin;
}

@JsonProperty("linkedin")
public void setLinkedin(String linkedin) {
this.linkedin = linkedin;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("phone")
public String getPhone() {
return phone;
}

@JsonProperty("phone")
public void setPhone(String phone) {
this.phone = phone;
}

@JsonProperty("projects")
public List<Object> getProjects() {
return projects;
}

@JsonProperty("projects")
public void setProjects(List<Object> projects) {
this.projects = projects;
}

@JsonProperty("resume_segments")
public ResumeSegments getResumeSegments() {
return resumeSegments;
}

@JsonProperty("resume_segments")
public void setResumeSegments(ResumeSegments resumeSegments) {
this.resumeSegments = resumeSegments;
}

@JsonProperty("skills")
public List<String> getSkills() {
return skills;
}

@JsonProperty("skills")
public void setSkills(List<String> skills) {
this.skills = skills;
}

@JsonProperty("summary")
public String getSummary() {
return summary;
}

@JsonProperty("summary")
public void setSummary(String summary) {
this.summary = summary;
}

@JsonProperty("total_experience")
public Integer getTotalExperience() {
return totalExperience;
}

@JsonProperty("total_experience")
public void setTotalExperience(Integer totalExperience) {
this.totalExperience = totalExperience;
}

@JsonProperty("university")
public List<Object> getUniversity() {
return university;
}

@JsonProperty("university")
public void setUniversity(List<Object> university) {
this.university = university;
}

@JsonProperty("website")
public String getWebsite() {
return website;
}

@JsonProperty("website")
public void setWebsite(String website) {
this.website = website;
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