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
public class Raw {

   @SerializedName("Name")
   String Name;

   @SerializedName("FoundIn")
   List<com.midas.consulting.controller.v1.response.parsing.textkernal.FoundIn> FoundIn;

   @SerializedName("MonthsExperience")
   com.midas.consulting.controller.v1.response.parsing.textkernal.MonthsExperience MonthsExperience;

   @SerializedName("LastUsed")
   com.midas.consulting.controller.v1.response.parsing.textkernal.LastUsed LastUsed;


    public void setName(String Name) {
        this.Name = Name;
    }
    public String getName() {
        return Name;
    }
    
    public void setFoundIn(List<com.midas.consulting.controller.v1.response.parsing.textkernal.FoundIn> FoundIn) {
        this.FoundIn = FoundIn;
    }
    public List<com.midas.consulting.controller.v1.response.parsing.textkernal.FoundIn> getFoundIn() {
        return FoundIn;
    }
    
    public void setMonthsExperience(com.midas.consulting.controller.v1.response.parsing.textkernal.MonthsExperience MonthsExperience) {
        this.MonthsExperience = MonthsExperience;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernal.MonthsExperience getMonthsExperience() {
        return MonthsExperience;
    }
    
    public void setLastUsed(com.midas.consulting.controller.v1.response.parsing.textkernal.LastUsed LastUsed) {
        this.LastUsed = LastUsed;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernal.LastUsed getLastUsed() {
        return LastUsed;
    }
    
}