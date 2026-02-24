package com.midas.consulting.controller.v1.response.parsing.textkernal;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class ConversionMetadata{
    @JsonProperty("DetectedType")
    public String detectedType;
    @JsonProperty("SuggestedFileExtension") 
    public String suggestedFileExtension;
    @JsonProperty("OutputValidityCode") 
    public String outputValidityCode;
    @JsonProperty("ElapsedMilliseconds") 
    public int elapsedMilliseconds;
    @JsonProperty("DocumentHash") 
    public String documentHash;
}