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
public class PhoneNumber{
    @JsonProperty("Number")
private String number;
    @JsonProperty("ISDCode") 
private String iSDCode;
    @JsonProperty("OriginalNumber") 
private String originalNumber;
    @JsonProperty("FormattedNumber") 
private String formattedNumber;
    @JsonProperty("Type") 
private String type;
    @JsonProperty("ConfidenceScore") 
private int confidenceScore;
}