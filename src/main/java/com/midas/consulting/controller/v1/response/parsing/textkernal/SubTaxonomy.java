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
public class SubTaxonomy{
    @JsonProperty("PercentOfOverall")
    public int percentOfOverall;
    @JsonProperty("PercentOfParent") 
    public int percentOfParent;
    @JsonProperty("SubTaxonomyId") 
    public String subTaxonomyId;
    @JsonProperty("SubTaxonomyName") 
    public String subTaxonomyName;
    @JsonProperty("Skills") 
    public ArrayList<Skill> skills;
}
