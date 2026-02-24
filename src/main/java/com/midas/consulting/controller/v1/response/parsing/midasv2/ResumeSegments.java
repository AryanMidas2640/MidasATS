package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"accomplishments",
"certifications",
"contact_info",
"education_and_training",
"misc",
"objective",
"projects",
"skills",
"work_and_employment"
})
@Generated("jsonschema2pojo")
public class ResumeSegments {

@JsonProperty("accomplishments")
private Accomplishments accomplishments;
@JsonProperty("certifications")
private Certifications certifications;
@JsonProperty("contact_info")
private List<String> contactInfo;
@JsonProperty("education_and_training")
private EducationAndTraining educationAndTraining;
@JsonProperty("misc")
private Misc misc;
@JsonProperty("objective")
private Objective objective;
@JsonProperty("projects")
private Projects projects;
@JsonProperty("skills")
private Skills skills;
@JsonProperty("work_and_employment")
private WorkAndEmployment workAndEmployment;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("accomplishments")
public Accomplishments getAccomplishments() {
return accomplishments;
}

@JsonProperty("accomplishments")
public void setAccomplishments(Accomplishments accomplishments) {
this.accomplishments = accomplishments;
}

@JsonProperty("certifications")
public Certifications getCertifications() {
return certifications;
}

@JsonProperty("certifications")
public void setCertifications(Certifications certifications) {
this.certifications = certifications;
}

@JsonProperty("contact_info")
public List<String> getContactInfo() {
return contactInfo;
}

@JsonProperty("contact_info")
public void setContactInfo(List<String> contactInfo) {
this.contactInfo = contactInfo;
}

@JsonProperty("education_and_training")
public EducationAndTraining getEducationAndTraining() {
return educationAndTraining;
}

@JsonProperty("education_and_training")
public void setEducationAndTraining(EducationAndTraining educationAndTraining) {
this.educationAndTraining = educationAndTraining;
}

@JsonProperty("misc")
public Misc getMisc() {
return misc;
}

@JsonProperty("misc")
public void setMisc(Misc misc) {
this.misc = misc;
}

@JsonProperty("objective")
public Objective getObjective() {
return objective;
}

@JsonProperty("objective")
public void setObjective(Objective objective) {
this.objective = objective;
}

@JsonProperty("projects")
public Projects getProjects() {
return projects;
}

@JsonProperty("projects")
public void setProjects(Projects projects) {
this.projects = projects;
}

@JsonProperty("skills")
public Skills getSkills() {
return skills;
}

@JsonProperty("skills")
public void setSkills(Skills skills) {
this.skills = skills;
}

@JsonProperty("work_and_employment")
public WorkAndEmployment getWorkAndEmployment() {
return workAndEmployment;
}

@JsonProperty("work_and_employment")
public void setWorkAndEmployment(WorkAndEmployment workAndEmployment) {
this.workAndEmployment = workAndEmployment;
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