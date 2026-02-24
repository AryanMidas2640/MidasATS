package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/* ObjectMapper om = new ObjectMapper();
ResumeParserRequest root = om.readValue(myJsonString, ResumeParserRequest.class); */
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Address{
    @JsonProperty("Street") 
private String street;
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
    @JsonProperty("ZipCode") 
private String zipCode;
    @JsonProperty("FormattedAddress") 
private String formattedAddress;
    @JsonProperty("Type") 
private String type;
    @JsonProperty("ConfidenceScore") 
private int confidenceScore;
}
