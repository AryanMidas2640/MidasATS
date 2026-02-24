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
public class Employer{
    @JsonProperty("EmployerName") 
private String employerName;
    @JsonProperty("FormattedName")
private String formattedName;
    @JsonProperty("ConfidenceScore") 
private int confidenceScore;
}