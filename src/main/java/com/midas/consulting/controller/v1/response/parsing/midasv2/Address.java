package com.midas.consulting.controller.v1.response.parsing.midasv2;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"city",
"country",
"state",
"street",
"zip"
})
@Generated("jsonschema2pojo")
public class Address {

@JsonProperty("city")
private String city;
@JsonProperty("country")
private String country;
@JsonProperty("state")
private String state;
@JsonProperty("street")
private String street;
@JsonProperty("zip")
private String zip;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("city")
public String getCity() {
return city;
}

@JsonProperty("city")
public void setCity(String city) {
this.city = city;
}

@JsonProperty("country")
public String getCountry() {
return country;
}

@JsonProperty("country")
public void setCountry(String country) {
this.country = country;
}

@JsonProperty("state")
public String getState() {
return state;
}

@JsonProperty("state")
public void setState(String state) {
this.state = state;
}

@JsonProperty("street")
public String getStreet() {
return street;
}

@JsonProperty("street")
public void setStreet(String street) {
this.street = street;
}

@JsonProperty("zip")
public String getZip() {
return zip;
}

@JsonProperty("zip")
public void setZip(String zip) {
this.zip = zip;
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
