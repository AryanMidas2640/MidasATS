package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"driveType",
"driveId",
"id",
"name",
"path",
"siteId"
})
@Generated("jsonschema2pojo")
public class ParentReference {

@JsonProperty("driveType")
private String driveType;
@JsonProperty("driveId")
private String driveId;
@JsonProperty("id")
private String id;
@JsonProperty("name")
private String name;
@JsonProperty("path")
private String path;
@JsonProperty("siteId")
private String siteId;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("driveType")
public String getDriveType() {
return driveType;
}

@JsonProperty("driveType")
public void setDriveType(String driveType) {
this.driveType = driveType;
}

@JsonProperty("driveId")
public String getDriveId() {
return driveId;
}

@JsonProperty("driveId")
public void setDriveId(String driveId) {
this.driveId = driveId;
}

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("path")
public String getPath() {
return path;
}

@JsonProperty("path")
public void setPath(String path) {
this.path = path;
}

@JsonProperty("siteId")
public String getSiteId() {
return siteId;
}

@JsonProperty("siteId")
public void setSiteId(String siteId) {
this.siteId = siteId;
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