package com.midas.consulting.controller.v1.request;

import com.fasterxml.jackson.annotation.*;
import com.midas.consulting.model.user.User;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"sourceID",
"providerJobID",
"activityNote",
"candidateID",
"activityType",
"dateCreated",
"userID"
})
@Generated("jsonschema2pojo")
public class ActivityRequest {

@JsonProperty("id")
private String id;
@JsonProperty("sourceID")
private String sourceID;
@JsonProperty("providerJobID")
private Integer providerJobID;
@JsonProperty("activityNote")
private String activityNote;
@JsonProperty("candidateID")
private String candidateID;
@JsonProperty("activityType")
private String activityType;
@JsonProperty("dateCreated")
private String dateCreated;
@JsonProperty("userID")
private User userID;
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

@JsonProperty("sourceID")
public String getSourceID() {
return sourceID;
}

@JsonProperty("sourceID")
public void setSourceID(String sourceID) {
this.sourceID = sourceID;
}

@JsonProperty("providerJobID")
public Integer getProviderJobID() {
return providerJobID;
}

@JsonProperty("providerJobID")
public void setProviderJobID(Integer providerJobID) {
this.providerJobID = providerJobID;
}

@JsonProperty("activityNote")
public String getActivityNote() {
return activityNote;
}

@JsonProperty("activityNote")
public void setActivityNote(String activityNote) {
this.activityNote = activityNote;
}

@JsonProperty("candidateID")
public String getCandidateID() {
return candidateID;
}

@JsonProperty("candidateID")
public void setCandidateID(String candidateID) {
this.candidateID = candidateID;
}

@JsonProperty("activityType")
public String getActivityType() {
return activityType;
}

@JsonProperty("activityType")
public void setActivityType(String activityType) {
this.activityType = activityType;
}

@JsonProperty("dateCreated")
public String getDateCreated() {
return dateCreated;
}

@JsonProperty("dateCreated")
public void setDateCreated(String dateCreated) {
this.dateCreated = dateCreated;
}

@JsonProperty("userID")
public User getUserID() {
return userID;
}

@JsonProperty("userID")
public void setUserID(User userID) {
this.userID = userID;
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