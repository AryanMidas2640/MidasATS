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
public class GPA{
    @JsonProperty("Score")
    public String score;
    @JsonProperty("ScoringSystem") 
    public String scoringSystem;
    @JsonProperty("MaxScore") 
    public String maxScore;
    @JsonProperty("MinimumScore") 
    public String minimumScore;
    @JsonProperty("NormalizedScore") 
    public int normalizedScore;
}