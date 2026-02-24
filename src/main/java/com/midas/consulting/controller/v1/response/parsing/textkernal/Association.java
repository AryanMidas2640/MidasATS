package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
TextKernelParsingRoot root = om.readValue(myJsonString, TextKernelParsingRoot.class); */
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Association{
    @JsonProperty("Organization")
    public String organization;
    @JsonProperty("Role") 
    public String role;
    @JsonProperty("FoundInContext") 
    public String foundInContext;
}














































































