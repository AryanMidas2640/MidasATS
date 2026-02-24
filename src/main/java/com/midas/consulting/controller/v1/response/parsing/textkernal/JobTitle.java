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
public class JobTitle{
    @JsonProperty("Raw")
    public String raw;
    @JsonProperty("Normalized") 
    public String normalized;
    @JsonProperty("Probability") 
    public String probability;
    @JsonProperty("Variations") 
    public ArrayList<String> variations;
}
