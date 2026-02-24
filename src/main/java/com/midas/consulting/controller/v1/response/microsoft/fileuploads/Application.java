package com.midas.consulting.controller.v1.response.microsoft.fileuploads;


import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"displayName"
})
@Generated("jsonschema2pojo")
public class Application {

@JsonProperty("id")
private String id;
@JsonProperty("displayName")
private String displayName;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("displayName")
public String getDisplayName() {
return displayName;
}

@JsonProperty("displayName")
public void setDisplayName(String displayName) {
this.displayName = displayName;
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




