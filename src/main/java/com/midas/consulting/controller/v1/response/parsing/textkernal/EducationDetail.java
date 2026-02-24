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
public class EducationDetail{
    @JsonProperty("Id")
    public String id;
    @JsonProperty("Text") 
    public String text;
    @JsonProperty("SchoolName") 
    public SchoolName schoolName;
    @JsonProperty("SchoolType") 
    public String schoolType;
    @JsonProperty("Location") 
    public Location location;
    @JsonProperty("Degree") 
    public Degree degree;
    @JsonProperty("Majors") 
    public ArrayList<String> majors;
    @JsonProperty("Minors") 
    public ArrayList<String> minors;
    @JsonProperty("GPA") 
    public GPA gPA;
    @JsonProperty("LastEducationDate") 
    public LastEducationDate lastEducationDate;
    @JsonProperty("StartDate") 
    public StartDate startDate;
    @JsonProperty("EndDate") 
    public EndDate endDate;
    @JsonProperty("Graduated") 
    public Graduated graduated;
}