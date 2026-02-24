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
public class NormalizedProfession{
    @JsonProperty("Profession")
    public Profession profession;
    @JsonProperty("Group") 
    public Group group;
    @JsonProperty("Class") 
    public Class classs;
    @JsonProperty("ISCO") 
    public ISCO iSCO;
    @JsonProperty("ONET") 
    public ONET oNET;
    @JsonProperty("Confidence") 
    public double confidence;
}