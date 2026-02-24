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
public class CountryCode{
    @JsonProperty("IsoAlpha2")
private String isoAlpha2;
    @JsonProperty("IsoAlpha3") 
private String isoAlpha3;
    @JsonProperty("UNCode") 
private String uNCode;
}
