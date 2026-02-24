package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SectionIdentifier{
    @JsonProperty("SectionType")
    public String sectionType;
    @JsonProperty("Id") 
    public String id;
}



