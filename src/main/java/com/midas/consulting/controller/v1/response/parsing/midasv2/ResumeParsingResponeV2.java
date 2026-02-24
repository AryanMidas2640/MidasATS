package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"message",
"parsed"
})
@Generated("jsonschema2pojo")
public class ResumeParsingResponeV2 {

@JsonProperty("message")
private String message;
@JsonProperty("parsed")
private Parsed parsed;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("message")
public String getMessage() {
return message;
}

@JsonProperty("message")
public void setMessage(String message) {
this.message = message;
}

@JsonProperty("parsed")
public Parsed getParsed() {
return parsed;
}

@JsonProperty("parsed")
public void setParsed(Parsed parsed) {
this.parsed = parsed;
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