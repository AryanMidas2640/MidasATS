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
public class Project{
    @JsonProperty("UsedSkills")
    private String usedSkills;
    @JsonProperty("ProjectName") 
    private String projectName;
    @JsonProperty("TeamSize") 
    private String teamSize;
}