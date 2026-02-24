package com.midas.consulting.controller.v1.response.hwl.allmails;

import com.fasterxml.jackson.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"@odata.context",
"value",
"@odata.nextLink"
})
public class AllMailsRoot {

@JsonProperty("@odata.context")
private String odataContext;
@JsonProperty("value")
private List<Value> value;
@JsonProperty("@odata.nextLink")
private String odataNextLink;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("@odata.context")
public String getOdataContext() {
return odataContext;
}

@JsonProperty("@odata.context")
public void setOdataContext(String odataContext) {
this.odataContext = odataContext;
}

@JsonProperty("value")
public List<Value> getValue() {
return value;
}

@JsonProperty("value")
public void setValue(List<Value> value) {
this.value = value;
}

@JsonProperty("@odata.nextLink")
public String getOdataNextLink() {
return odataNextLink;
}

@JsonProperty("@odata.nextLink")
public void setOdataNextLink(String odataNextLink) {
this.odataNextLink = odataNextLink;
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