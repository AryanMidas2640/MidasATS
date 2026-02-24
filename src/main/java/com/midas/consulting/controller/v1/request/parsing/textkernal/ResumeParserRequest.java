package com.midas.consulting.controller.v1.request.parsing.textkernal;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeParserRequest {
    @JsonProperty("DocumentAsBase64String") 
    public String documentAsBase64String;
    @JsonProperty("SkillsSettings") 
    public SkillsSettings skillsSettings;
    @JsonProperty("ProfessionsSettings") 
    public ProfessionsSettings professionsSettings;
    @JsonProperty("DocumentLastModified") 
    public String documentLastModified;
    @JsonProperty("OutputHtml") 
    public boolean outputHtml;
    @JsonProperty("OutputRtf") 
    public boolean outputRtf;
    @JsonProperty("OutputPdf") 
    public boolean outputPdf;
    @JsonProperty("UseLLMParser") 
    public boolean useLLMParser;
    @JsonProperty("OutputCandidateImage") 
    public boolean outputCandidateImage;
    @JsonProperty("Configuration") 
    public String configuration;
    @JsonProperty("GeocodeOptions") 
    public GeocodeOptions geocodeOptions;
    @JsonProperty("IndexingOptions") 
    public IndexingOptions indexingOptions;
    @JsonProperty("FlexRequests") 
    public ArrayList<FlexRequest> flexRequests;
    @JsonProperty("SkillsData")
    public ArrayList<String> skillsData;
    @JsonProperty("NormalizerData") 
    public String normalizerData;
}

