package com.midas.consulting.controller.v1.response.parsing.midas;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"message",
"parsed"
})
@Generated("jsonschema2pojo")
public class ResumeParsingRespone {

@JsonProperty("message")
public String message;
@JsonProperty("parsed")
public Parsed parsed;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}