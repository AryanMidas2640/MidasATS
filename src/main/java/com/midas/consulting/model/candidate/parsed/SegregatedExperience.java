package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class SegregatedExperience{
    @JsonProperty("Employer")
private Employer employer;
    @JsonProperty("JobProfile") 
private JobProfile jobProfile;
    @JsonProperty("Location") 
private Location location;
    @JsonProperty("JobPeriod") 
private String jobPeriod;
    @JsonProperty("FormattedJobPeriod") 
private String formattedJobPeriod;
    @JsonProperty("StartDate") 
private String startDate;
    @JsonProperty("EndDate") 
private String endDate;
    @JsonProperty("IsCurrentEmployer") 
private String isCurrentEmployer;
    @JsonProperty("JobDescription") 
private String jobDescription;
    @JsonProperty("Projects") 
private ArrayList<Project> projects;
}
