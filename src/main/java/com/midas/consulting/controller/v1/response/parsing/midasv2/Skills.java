package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"languages",
"other_skills",
"skills"
})
@Generated("jsonschema2pojo")
public class Skills {

@JsonProperty("languages")
private List<String> languages;
@JsonProperty("other_skills")
private List<String> otherSkills;
@JsonProperty("skills")
private List<String> skills;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("languages")
public List<String> getLanguages() {
return languages;
}

@JsonProperty("languages")
public void setLanguages(List<String> languages) {
this.languages = languages;
}

@JsonProperty("other_skills")
public List<String> getOtherSkills() {
return otherSkills;
}

@JsonProperty("other_skills")
public void setOtherSkills(List<String> otherSkills) {
this.otherSkills = otherSkills;
}

@JsonProperty("skills")
public List<String> getSkills() {
return skills;
}

@JsonProperty("skills")
public void setSkills(List<String> skills) {
this.skills = skills;
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