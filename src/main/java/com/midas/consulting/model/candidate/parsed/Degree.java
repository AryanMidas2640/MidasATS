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
public class Degree {
    @JsonProperty("DegreeName")
private String degreeName;
    @JsonProperty("NormalizeDegree")
private String normalizeDegree;
    @JsonProperty("Specialization")
private ArrayList<String> specialization;
    @JsonProperty("ConfidenceScore")
private int confidenceScore;
}
