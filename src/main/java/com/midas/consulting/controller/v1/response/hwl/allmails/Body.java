package com.midas.consulting.controller.v1.response.hwl.allmails;


import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"contentType",
"content"
})

public class Body {

@JsonProperty("contentType")
private String contentType;
@JsonProperty("content")
private String content;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("contentType")
public String getContentType() {
return contentType;
}

@JsonProperty("contentType")
public void setContentType(String contentType) {
this.contentType = contentType;
}

@JsonProperty("content")
public String getContent() {
return content;
}

@JsonProperty("content")
public void setContent(String content) {
this.content = content;
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
