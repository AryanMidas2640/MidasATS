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
public class PassportDetail{
    @JsonProperty("PassportNumber")
private String passportNumber;
    @JsonProperty("DateOfExpiry") 
private String dateOfExpiry;
    @JsonProperty("DateOfIssue") 
private String dateOfIssue;
    @JsonProperty("PlaceOfIssue") 
private String placeOfIssue;
}