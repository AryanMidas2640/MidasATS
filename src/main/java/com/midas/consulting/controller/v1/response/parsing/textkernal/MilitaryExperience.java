package com.midas.consulting.controller.v1.response.parsing.textkernal;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class MilitaryExperience{
    @JsonProperty("Country")
    public String country;
    @JsonProperty("Service") 
    public Service service;
    @JsonProperty("StartDate") 
    public StartDate startDate;
    @JsonProperty("EndDate") 
    public EndDate endDate;
    @JsonProperty("FoundInContext") 
    public String foundInContext;
}