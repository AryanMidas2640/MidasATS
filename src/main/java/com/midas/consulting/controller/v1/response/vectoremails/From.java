
package com.midas.consulting.controller.v1.response.vectoremails;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"emailAddress"
})
@Generated("jsonschema2pojo")
public class From {

@JsonProperty("emailAddress")
private EmailAddress emailAddress;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("emailAddress")
public EmailAddress getEmailAddress() {
return emailAddress;
}

@JsonProperty("emailAddress")
public void setEmailAddress(EmailAddress emailAddress) {
this.emailAddress = emailAddress;
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