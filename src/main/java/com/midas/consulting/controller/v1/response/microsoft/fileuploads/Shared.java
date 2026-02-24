package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"scope"
})
@Generated("jsonschema2pojo")
public class Shared {

@JsonProperty("scope")
private String scope;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("scope")
public String getScope() {
return scope;
}

@JsonProperty("scope")
public void setScope(String scope) {
this.scope = scope;
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