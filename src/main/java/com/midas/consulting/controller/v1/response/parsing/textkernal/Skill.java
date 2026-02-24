package com.midas.consulting.controller.v1.response.parsing.textkernal;

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
public class Skill{
    @JsonProperty("Id")
    public String id;
    @JsonProperty("Name") 
    public String name;
    @JsonProperty("FoundIn") 
    public FoundIn foundIn;
    @JsonProperty("ExistsInText") 
    public boolean existsInText;
    @JsonProperty("Variations") 
    public ArrayList<Variation> variations;
    @JsonProperty("MonthsExperience") 
    public MonthsExperience monthsExperience;
    @JsonProperty("LastUsed") 
    public LastUsed lastUsed;
    @JsonProperty("ChildrenMonthsExperience") 
    public ChildrenMonthsExperience childrenMonthsExperience;
    @JsonProperty("ChildrenLastUsed") 
    public ChildrenLastUsed childrenLastUsed;
    @JsonProperty("Raw") 
    public ArrayList<Raw> raw;
    @JsonProperty("Normalized") 
    public ArrayList<Normalized> normalized;
    @JsonProperty("RelatedProfessionClasses") 
    public ArrayList<RelatedProfessionClass> relatedProfessionClasses;
}

