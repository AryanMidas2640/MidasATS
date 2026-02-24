package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"application",
"user"
})
@Generated("jsonschema2pojo")
public class LastModifiedBy {

@JsonProperty("application")
private Application application;
@JsonProperty("user")
private User user;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("application")
public Application getApplication() {
return application;
}

@JsonProperty("application")
public void setApplication(Application application) {
this.application = application;
}

@JsonProperty("user")
public User getUser() {
return user;
}

@JsonProperty("user")
public void setUser(User user) {
this.user = user;
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