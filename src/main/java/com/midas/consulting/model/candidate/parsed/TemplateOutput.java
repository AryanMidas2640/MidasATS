package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class TemplateOutput{
    @JsonProperty("TemplateOutputData")
private String templateOutputData;
    @JsonProperty("TemplateOutputFileName") 
private String templateOutputFileName;
}
