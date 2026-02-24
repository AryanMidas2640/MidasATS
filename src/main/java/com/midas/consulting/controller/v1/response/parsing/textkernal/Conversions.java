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
public class Conversions{
    @JsonProperty("PDF")
    public String pDF;
    @JsonProperty("HTML") 
    public String hTML;
    @JsonProperty("RTF") 
    public String rTF;
    @JsonProperty("CandidateImage") 
    public String candidateImage;
    @JsonProperty("CandidateImageExtension") 
    public String candidateImageExtension;
}