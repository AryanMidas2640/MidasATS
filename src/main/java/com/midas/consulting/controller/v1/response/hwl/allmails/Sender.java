package com.midas.consulting.controller.v1.response.hwl.allmails;


import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"emailAddress"
})
public class Sender {

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