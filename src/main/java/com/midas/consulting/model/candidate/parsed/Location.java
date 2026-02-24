package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Location{
    @JsonProperty("City")
private String city;
    @JsonProperty("State") 
private String state;
    @JsonProperty("StateIsoCode") 
private String stateIsoCode;
    @JsonProperty("Country") 
private String country;
    @JsonProperty("CountryCode") 
private CountryCode countryCode;
}