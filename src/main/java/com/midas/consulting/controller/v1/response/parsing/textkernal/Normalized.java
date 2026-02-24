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
public class Normalized{
    @JsonProperty("FoundIn")
    public ArrayList<FoundIn> foundIn;
    @JsonProperty("MonthsExperience") 
    public MonthsExperience monthsExperience;
    @JsonProperty("LastUsed") 
    public LastUsed lastUsed;
    @JsonProperty("Name") 
    public String name;
    @JsonProperty("Type") 
    public String type;
    @JsonProperty("Id") 
    public String id;
    @JsonProperty("RawSkills") 
    public ArrayList<String> rawSkills;
}