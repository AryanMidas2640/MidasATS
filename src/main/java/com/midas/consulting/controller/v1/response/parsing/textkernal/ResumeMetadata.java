package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class ResumeMetadata{
    @JsonProperty("FoundSections")
    public ArrayList<FoundSection> foundSections;
    @JsonProperty("ResumeQuality") 
    public ArrayList<ResumeQuality> resumeQuality;
    @JsonProperty("ReservedData") 
    public ReservedData reservedData;
    @JsonProperty("PlainText") 
    public String plainText;
    @JsonProperty("DocumentLanguage") 
    public String documentLanguage;
    @JsonProperty("DocumentCulture") 
    public String documentCulture;
    @JsonProperty("ParserSettings") 
    public String parserSettings;
    @JsonProperty("DocumentLastModified") 
    public String documentLastModified;
}