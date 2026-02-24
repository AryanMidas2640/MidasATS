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
public class FoundSections {

   @SerializedName("FirstLineNumber")
   int FirstLineNumber;

   @SerializedName("LastLineNumber")
   int LastLineNumber;

   @SerializedName("SectionType")
   String SectionType;

   @SerializedName("HeaderTextFound")
   String HeaderTextFound;


    public void setFirstLineNumber(int FirstLineNumber) {
        this.FirstLineNumber = FirstLineNumber;
    }
    public int getFirstLineNumber() {
        return FirstLineNumber;
    }
    
    public void setLastLineNumber(int LastLineNumber) {
        this.LastLineNumber = LastLineNumber;
    }
    public int getLastLineNumber() {
        return LastLineNumber;
    }
    
    public void setSectionType(String SectionType) {
        this.SectionType = SectionType;
    }
    public String getSectionType() {
        return SectionType;
    }
    
    public void setHeaderTextFound(String HeaderTextFound) {
        this.HeaderTextFound = HeaderTextFound;
    }
    public String getHeaderTextFound() {
        return HeaderTextFound;
    }
    
}