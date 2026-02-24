package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"createdDateTime",
"lastModifiedDateTime"
})
@Generated("jsonschema2pojo")
public class FileSystemInfo {

@JsonProperty("createdDateTime")
private String createdDateTime;
@JsonProperty("lastModifiedDateTime")
private String lastModifiedDateTime;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("createdDateTime")
public String getCreatedDateTime() {
return createdDateTime;
}

@JsonProperty("createdDateTime")
public void setCreatedDateTime(String createdDateTime) {
this.createdDateTime = createdDateTime;
}

@JsonProperty("lastModifiedDateTime")
public String getLastModifiedDateTime() {
return lastModifiedDateTime;
}

@JsonProperty("lastModifiedDateTime")
public void setLastModifiedDateTime(String lastModifiedDateTime) {
this.lastModifiedDateTime = lastModifiedDateTime;
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