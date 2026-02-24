package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
   
public class Certifications {

   @SerializedName("Name")
   String Name;

   @SerializedName("MatchedFromList")
   boolean MatchedFromList;

   @SerializedName("IsVariation")
   boolean IsVariation;


    public void setName(String Name) {
        this.Name = Name;
    }
    public String getName() {
        return Name;
    }
    
    public void setMatchedFromList(boolean MatchedFromList) {
        this.MatchedFromList = MatchedFromList;
    }
    public boolean getMatchedFromList() {
        return MatchedFromList;
    }
    
    public void setIsVariation(boolean IsVariation) {
        this.IsVariation = IsVariation;
    }
    public boolean getIsVariation() {
        return IsVariation;
    }
    
}