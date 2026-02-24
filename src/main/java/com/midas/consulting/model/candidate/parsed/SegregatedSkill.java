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
public class SegregatedSkill{
    @JsonProperty("Type")
private String type;
    @JsonProperty("Skill") 
private String skill;
    @JsonProperty("Ontology") 
private String ontology;
    @JsonProperty("Alias") 
private String alias;
    @JsonProperty("FormattedName") 
private String formattedName;
    @JsonProperty("Evidence") 
private String evidence;
    @JsonProperty("LastUsed") 
private String lastUsed;
    @JsonProperty("ExperienceInMonths") 
private int experienceInMonths;
}
