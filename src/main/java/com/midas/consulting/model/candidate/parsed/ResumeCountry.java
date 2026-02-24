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
public class ResumeCountry{
    @JsonProperty("Country")
private String country;
    @JsonProperty("Evidence") 
private String evidence;
    @JsonProperty("CountryCode") 
private CountryCode countryCode;
}