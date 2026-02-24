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
   
public class CandidateName {

   @SerializedName("FormattedName")
   String FormattedName;

   @SerializedName("GivenName")
   String GivenName;

   @SerializedName("MiddleName")
   String MiddleName;

   @SerializedName("FamilyName")
   String FamilyName;


    public void setFormattedName(String FormattedName) {
        this.FormattedName = FormattedName;
    }
    public String getFormattedName() {
        return FormattedName;
    }
    
    public void setGivenName(String GivenName) {
        this.GivenName = GivenName;
    }
    public String getGivenName() {
        return GivenName;
    }
    
    public void setMiddleName(String MiddleName) {
        this.MiddleName = MiddleName;
    }
    public String getMiddleName() {
        return MiddleName;
    }
    
    public void setFamilyName(String FamilyName) {
        this.FamilyName = FamilyName;
    }
    public String getFamilyName() {
        return FamilyName;
    }
    
}