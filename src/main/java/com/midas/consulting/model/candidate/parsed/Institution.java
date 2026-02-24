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
public class Institution{
    @JsonProperty("Name")
private String name;
    @JsonProperty("Type") 
private String type;
    @JsonProperty("Location") 
private Location location;
    @JsonProperty("ConfidenceScore") 
private int confidenceScore;
}