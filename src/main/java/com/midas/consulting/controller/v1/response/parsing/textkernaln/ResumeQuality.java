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
public class ResumeQuality {

   @SerializedName("Level")
   String Level;

   @SerializedName("Findings")
   List<Findings> Findings;


    public void setLevel(String Level) {
        this.Level = Level;
    }
    public String getLevel() {
        return Level;
    }
    
    public void setFindings(List<Findings> Findings) {
        this.Findings = Findings;
    }
    public List<Findings> getFindings() {
        return Findings;
    }
    
}