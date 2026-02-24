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
public class Training2{
    @JsonProperty("Text")
    public String text;
    @JsonProperty("Entity") 
    public String entity;
    @JsonProperty("Qualifications") 
    public ArrayList<String> qualifications;
    @JsonProperty("StartDate") 
    public StartDate startDate;
    @JsonProperty("EndDate") 
    public EndDate endDate;
}