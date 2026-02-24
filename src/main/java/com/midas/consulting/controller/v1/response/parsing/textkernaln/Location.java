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
public class Location {

   @SerializedName("CountryCode")
   String CountryCode;

   @SerializedName("Regions")
   List<String> Regions;

   @SerializedName("Municipality")
   String Municipality;


    public void setCountryCode(String CountryCode) {
        this.CountryCode = CountryCode;
    }
    public String getCountryCode() {
        return CountryCode;
    }
    
    public void setRegions(List<String> Regions) {
        this.Regions = Regions;
    }
    public List<String> getRegions() {
        return Regions;
    }
    
    public void setMunicipality(String Municipality) {
        this.Municipality = Municipality;
    }
    public String getMunicipality() {
        return Municipality;
    }
    
}