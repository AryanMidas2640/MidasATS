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
public class Variation{
    @JsonProperty("MonthsExperience")
    public MonthsExperience monthsExperience;
    @JsonProperty("LastUsed") 
    public LastUsed lastUsed;
    @JsonProperty("Id") 
    public String id;
    @JsonProperty("Name") 
    public String name;
    @JsonProperty("FoundIn") 
    public FoundIn foundIn;
    @JsonProperty("ExistsInText") 
    public boolean existsInText;
}
