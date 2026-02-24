package com.midas.consulting.controller.v1.request.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
ResumeParserRequest root = om.readValue(myJsonString, ResumeParserRequest.class); */

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlexRequest{
    @JsonProperty("Prompt")
    public String prompt;
    @JsonProperty("Identifier")
    public String identifier;
    @JsonProperty("DataType")
    public String dataType;
    @JsonProperty("EnumerationValues")
    public ArrayList<String> enumerationValues;
}




