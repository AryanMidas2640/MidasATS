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
public class SkillsDatum{
    @JsonProperty("TextKernelParsingRoot")
    public String root;
    @JsonProperty("Taxonomies") 
    public ArrayList<Taxonomy> taxonomies;
}
