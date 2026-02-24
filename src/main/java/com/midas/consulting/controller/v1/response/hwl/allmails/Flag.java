package com.midas.consulting.controller.v1.response.hwl.allmails;

import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"flagStatus"
})
public class Flag {

@JsonProperty("flagStatus")
private String flagStatus;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("flagStatus")
public String getFlagStatus() {
return flagStatus;
}

@JsonProperty("flagStatus")
public void setFlagStatus(String flagStatus) {
this.flagStatus = flagStatus;
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