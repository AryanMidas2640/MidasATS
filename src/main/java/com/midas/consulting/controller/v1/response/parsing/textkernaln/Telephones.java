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
   
public class Telephones {

   @SerializedName("Raw")
   String Raw;

   @SerializedName("Normalized")
   String Normalized;

   @SerializedName("InternationalCountryCode")
   String InternationalCountryCode;

   @SerializedName("AreaCityCode")
   String AreaCityCode;

   @SerializedName("SubscriberNumber")
   String SubscriberNumber;


    public void setRaw(String Raw) {
        this.Raw = Raw;
    }
    public String getRaw() {
        return Raw;
    }
    
    public void setNormalized(String Normalized) {
        this.Normalized = Normalized;
    }
    public String getNormalized() {
        return Normalized;
    }
    
    public void setInternationalCountryCode(String InternationalCountryCode) {
        this.InternationalCountryCode = InternationalCountryCode;
    }
    public String getInternationalCountryCode() {
        return InternationalCountryCode;
    }
    
    public void setAreaCityCode(String AreaCityCode) {
        this.AreaCityCode = AreaCityCode;
    }
    public String getAreaCityCode() {
        return AreaCityCode;
    }
    
    public void setSubscriberNumber(String SubscriberNumber) {
        this.SubscriberNumber = SubscriberNumber;
    }
    public String getSubscriberNumber() {
        return SubscriberNumber;
    }
    
}