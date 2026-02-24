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
public class WorkedPeriod{
    @JsonProperty("TotalExperienceInMonths")
private String totalExperienceInMonths;
    @JsonProperty("TotalExperienceInYear") 
private String totalExperienceInYear;
    @JsonProperty("TotalExperienceRange") 
private String totalExperienceRange;
}
