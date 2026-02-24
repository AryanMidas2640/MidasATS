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
public class JobTitle {

   @SerializedName("Raw")
   String Raw;

   @SerializedName("Normalized")
   String Normalized;

   @SerializedName("Probability")
   String Probability;

   @SerializedName("Variations")
   List<String> Variations;


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
    
    public void setProbability(String Probability) {
        this.Probability = Probability;
    }
    public String getProbability() {
        return Probability;
    }
    
    public void setVariations(List<String> Variations) {
        this.Variations = Variations;
    }
    public List<String> getVariations() {
        return Variations;
    }
    
}