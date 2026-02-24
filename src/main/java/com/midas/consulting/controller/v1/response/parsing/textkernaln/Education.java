package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Education {

   @SerializedName("HighestDegree")
HighestDegree HighestDegree;

   @SerializedName("EducationDetails")
   List<EducationDetails> EducationDetails;


    public void setHighestDegree(HighestDegree HighestDegree) {
        this.HighestDegree = HighestDegree;
    }
    public HighestDegree getHighestDegree() {
        return HighestDegree;
    }
    
    public void setEducationDetails(List<EducationDetails> EducationDetails) {
        this.EducationDetails = EducationDetails;
    }
    public List<EducationDetails> getEducationDetails() {
        return EducationDetails;
    }
    
}