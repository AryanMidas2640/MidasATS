package com.midas.consulting.model.candidate.parsed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties
public class Root{
    @JsonProperty("ResumeParserData")
private ResumeParserData resumeParserData;
}
