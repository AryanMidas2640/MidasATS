package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"languages"
})
@Generated("jsonschema2pojo")
public class Misc {

@JsonProperty("languages")
private List<Object> languages;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("languages")
public List<Object> getLanguages() {
return languages;
}

@JsonProperty("languages")
public void setLanguages(List<Object> languages) {
this.languages = languages;
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