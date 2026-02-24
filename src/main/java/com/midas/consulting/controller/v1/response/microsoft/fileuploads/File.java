package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"mimeType",
"hashes"
})
@Generated("jsonschema2pojo")
public class File {

@JsonProperty("mimeType")
private String mimeType;
@JsonProperty("hashes")
private Hashes hashes;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("mimeType")
public String getMimeType() {
return mimeType;
}

@JsonProperty("mimeType")
public void setMimeType(String mimeType) {
this.mimeType = mimeType;
}

@JsonProperty("hashes")
public Hashes getHashes() {
return hashes;
}

@JsonProperty("hashes")
public void setHashes(Hashes hashes) {
this.hashes = hashes;
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