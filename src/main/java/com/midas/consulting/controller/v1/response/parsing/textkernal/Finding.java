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
public class Finding{
    @JsonProperty("QualityCode")
    public String qualityCode;
    @JsonProperty("SectionIdentifiers") 
    public ArrayList<SectionIdentifier> sectionIdentifiers;
    @JsonProperty("Message") 
    public String message;
}