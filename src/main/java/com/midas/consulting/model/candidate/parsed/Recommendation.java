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
public class Recommendation{
    @JsonProperty("PersonName")
private String personName;
    @JsonProperty("CompanyName") 
private String companyName;
    @JsonProperty("Relation") 
private String relation;
    @JsonProperty("PositionTitle") 
private String positionTitle;
    @JsonProperty("Description") 
private String description;
}