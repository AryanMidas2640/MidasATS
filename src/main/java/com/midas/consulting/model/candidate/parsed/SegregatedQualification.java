package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SegregatedQualification{
    @JsonProperty("Institution")
private Institution institution;
    @JsonProperty("Degree") 
private Degree degree;
    @JsonProperty("FormattedDegreePeriod") 
private String formattedDegreePeriod;
    @JsonProperty("StartDate") 
private String startDate;
    @JsonProperty("EndDate") 
private String endDate;
    @JsonProperty("Aggregate") 
private Aggregate aggregate;
}