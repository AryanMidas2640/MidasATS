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
public class Name{
    @JsonProperty("FullName")
private String fullName;
    @JsonProperty("TitleName") 
private String titleName;
    @JsonProperty("FirstName") 
private String firstName;
    @JsonProperty("MiddleName") 
private String middleName;
    @JsonProperty("LastName") 
private String lastName;
    @JsonProperty("FormattedName") 
private String formattedName;
    @JsonProperty("ConfidenceScore") 
private int confidenceScore;
}