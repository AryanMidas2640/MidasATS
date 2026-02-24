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
public class FoundSection{
    @JsonProperty("FirstLineNumber")
    public int firstLineNumber;
    @JsonProperty("LastLineNumber") 
    public int lastLineNumber;
    @JsonProperty("SectionType") 
    public String sectionType;
    @JsonProperty("HeaderTextFound") 
    public String headerTextFound;
}