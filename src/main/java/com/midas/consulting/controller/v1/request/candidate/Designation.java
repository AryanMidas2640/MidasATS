package com.midas.consulting.controller.v1.request.candidate;


import java.util.LinkedHashMap;
import java.util.Map;


public class Designation {

public String countryCode;
public String postalCode;
public String state;
public String country;
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
