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
public class ExperienceSummary{
    @JsonProperty("Description")
    public String description;
    @JsonProperty("MonthsOfWorkExperience") 
    public int monthsOfWorkExperience;
    @JsonProperty("MonthsOfManagementExperience") 
    public int monthsOfManagementExperience;
    @JsonProperty("ExecutiveType") 
    public String executiveType;
    @JsonProperty("AverageMonthsPerEmployer") 
    public int averageMonthsPerEmployer;
    @JsonProperty("FulltimeDirectHirePredictiveIndex") 
    public int fulltimeDirectHirePredictiveIndex;
    @JsonProperty("ManagementStory") 
    public String managementStory;
    @JsonProperty("CurrentManagementLevel") 
    public String currentManagementLevel;
    @JsonProperty("ManagementScore") 
    public int managementScore;
    @JsonProperty("AttentionNeeded") 
    public String attentionNeeded;
}
