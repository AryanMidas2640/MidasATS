package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmploymentHistory {

   @SerializedName("ExperienceSummary")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.ExperienceSummary ExperienceSummary;

   @SerializedName("Positions")
   List<com.midas.consulting.controller.v1.response.parsing.textkernaln.Positions> Positions;


    public void setExperienceSummary(ExperienceSummary ExperienceSummary) {
        this.ExperienceSummary = ExperienceSummary;
    }
    public ExperienceSummary getExperienceSummary() {
        return ExperienceSummary;
    }
    
    public void setPositions(List<Positions> Positions) {
        this.Positions = Positions;
    }
    public List<Positions> getPositions() {
        return Positions;
    }
    
}