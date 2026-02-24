package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"company",
"description",
"end_date",
"start_date",
"title"
})
@Generated("jsonschema2pojo")
public class Experience {

@JsonProperty("company")
private String company;
@JsonProperty("description")
private String description;
@JsonProperty("end_date")
private String endDate;
@JsonProperty("start_date")
private String startDate;
@JsonProperty("title")
private String title;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("company")
public String getCompany() {
return company;
}

@JsonProperty("company")
public void setCompany(String company) {
this.company = company;
}

@JsonProperty("description")
public String getDescription() {
return description;
}

@JsonProperty("description")
public void setDescription(String description) {
this.description = description;
}

@JsonProperty("end_date")
public String getEndDate() {
return endDate;
}

@JsonProperty("end_date")
public void setEndDate(String endDate) {
this.endDate = endDate;
}

@JsonProperty("start_date")
public String getStartDate() {
return startDate;
}

@JsonProperty("start_date")
public void setStartDate(String startDate) {
this.startDate = startDate;
}

@JsonProperty("title")
public String getTitle() {
return title;
}

@JsonProperty("title")
public void setTitle(String title) {
this.title = title;
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