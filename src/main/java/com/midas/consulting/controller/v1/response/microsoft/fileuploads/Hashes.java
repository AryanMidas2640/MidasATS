package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"quickXorHash"
})
@Generated("jsonschema2pojo")
public class Hashes {

@JsonProperty("quickXorHash")
private String quickXorHash;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("quickXorHash")
public String getQuickXorHash() {
return quickXorHash;
}

@JsonProperty("quickXorHash")
public void setQuickXorHash(String quickXorHash) {
this.quickXorHash = quickXorHash;
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