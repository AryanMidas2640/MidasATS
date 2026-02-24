package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"degree",
"end_date",
"gpa",
"start_date",
"university"
})
@Generated("jsonschema2pojo")
public class Education {

@JsonProperty("degree")
private String degree;
@JsonProperty("end_date")
private String endDate;
@JsonProperty("gpa")
private String gpa;
@JsonProperty("start_date")
private String startDate;
@JsonProperty("university")
private String university;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("degree")
public String getDegree() {
return degree;
}

@JsonProperty("degree")
public void setDegree(String degree) {
this.degree = degree;
}

@JsonProperty("end_date")
public String getEndDate() {
return endDate;
}

@JsonProperty("end_date")
public void setEndDate(String endDate) {
this.endDate = endDate;
}

@JsonProperty("gpa")
public String getGpa() {
return gpa;
}

@JsonProperty("gpa")
public void setGpa(String gpa) {
this.gpa = gpa;
}

@JsonProperty("start_date")
public String getStartDate() {
return startDate;
}

@JsonProperty("start_date")
public void setStartDate(String startDate) {
this.startDate = startDate;
}

@JsonProperty("university")
public String getUniversity() {
return university;
}

@JsonProperty("university")
public void setUniversity(String university) {
this.university = university;
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