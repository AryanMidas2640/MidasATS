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
public class ConversionMetadata {

   @SerializedName("DetectedType")
   String DetectedType;

   @SerializedName("SuggestedFileExtension")
   String SuggestedFileExtension;

   @SerializedName("OutputValidityCode")
   String OutputValidityCode;

   @SerializedName("ElapsedMilliseconds")
   int ElapsedMilliseconds;

   @SerializedName("DocumentHash")
   String DocumentHash;


    public void setDetectedType(String DetectedType) {
        this.DetectedType = DetectedType;
    }
    public String getDetectedType() {
        return DetectedType;
    }
    
    public void setSuggestedFileExtension(String SuggestedFileExtension) {
        this.SuggestedFileExtension = SuggestedFileExtension;
    }
    public String getSuggestedFileExtension() {
        return SuggestedFileExtension;
    }
    
    public void setOutputValidityCode(String OutputValidityCode) {
        this.OutputValidityCode = OutputValidityCode;
    }
    public String getOutputValidityCode() {
        return OutputValidityCode;
    }
    
    public void setElapsedMilliseconds(int ElapsedMilliseconds) {
        this.ElapsedMilliseconds = ElapsedMilliseconds;
    }
    public int getElapsedMilliseconds() {
        return ElapsedMilliseconds;
    }
    
    public void setDocumentHash(String DocumentHash) {
        this.DocumentHash = DocumentHash;
    }
    public String getDocumentHash() {
        return DocumentHash;
    }
    
}