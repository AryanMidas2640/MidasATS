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
public class JobProfile{
    @JsonProperty("Title")
private String title;
    @JsonProperty("FormattedName") 
private String formattedName;
    @JsonProperty("Alias") 
private String alias;
    @JsonProperty("RelatedSkills") 
private ArrayList<RelatedSkill> relatedSkills;
    @JsonProperty("ConfidenceScore") 
private int confidenceScore;
}
