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
public class EmploymentHistory{
    @JsonProperty("ExperienceSummary")
    public ExperienceSummary experienceSummary;
    @JsonProperty("Positions") 
    public ArrayList<Position> positions;
}