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

public class Value {

   @SerializedName("ParsingResponse")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.ParsingResponse ParsingResponse;

   @SerializedName("ResumeData")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.ResumeData ResumeData;

   @SerializedName("RedactedResumeData")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.RedactedResumeData RedactedResumeData;

   @SerializedName("EducationNormalizationResponse")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.EducationNormalizationResponse EducationNormalizationResponse;

   @SerializedName("ConversionMetadata")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.ConversionMetadata ConversionMetadata;

   @SerializedName("ParsingMetadata")
   ParsingMetadata ParsingMetadata;


    public void setParsingResponse(ParsingResponse ParsingResponse) {
        this.ParsingResponse = ParsingResponse;
    }
    public ParsingResponse getParsingResponse() {
        return ParsingResponse;
    }
    
    public void setResumeData(ResumeData ResumeData) {
        this.ResumeData = ResumeData;
    }
    public ResumeData getResumeData() {
        return ResumeData;
    }
    
    public void setRedactedResumeData(RedactedResumeData RedactedResumeData) {
        this.RedactedResumeData = RedactedResumeData;
    }
    public RedactedResumeData getRedactedResumeData() {
        return RedactedResumeData;
    }
    
    public void setEducationNormalizationResponse(EducationNormalizationResponse EducationNormalizationResponse) {
        this.EducationNormalizationResponse = EducationNormalizationResponse;
    }
    public EducationNormalizationResponse getEducationNormalizationResponse() {
        return EducationNormalizationResponse;
    }
    
    public void setConversionMetadata(ConversionMetadata ConversionMetadata) {
        this.ConversionMetadata = ConversionMetadata;
    }
    public ConversionMetadata getConversionMetadata() {
        return ConversionMetadata;
    }
    
    public void setParsingMetadata(ParsingMetadata ParsingMetadata) {
        this.ParsingMetadata = ParsingMetadata;
    }
    public ParsingMetadata getParsingMetadata() {
        return ParsingMetadata;
    }
    
}