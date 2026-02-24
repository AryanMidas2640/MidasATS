package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Value{
    @JsonProperty("ParsingResponse")
    public ParsingResponse parsingResponse;
    @JsonProperty("GeocodeResponse") 
    public GeocodeResponse geocodeResponse;
    @JsonProperty("IndexingResponse") 
    public IndexingResponse indexingResponse;
    @JsonProperty("ProfessionNormalizationResponse") 
    public ProfessionNormalizationResponse professionNormalizationResponse;
    @JsonProperty("FlexResponse") 
    public FlexResponse flexResponse;
    @JsonProperty("ResumeData") 
    public ResumeData resumeData;
    @JsonProperty("RedactedResumeData") 
    public RedactedResumeData redactedResumeData;


    @JsonProperty("ConversionMetadata") 
    public ConversionMetadata conversionMetadata;



    @JsonProperty("Conversions") 
    public Conversions conversions;


    @JsonProperty("ParsingMetadata") 
    public ParsingMetadata parsingMetadata;
}



