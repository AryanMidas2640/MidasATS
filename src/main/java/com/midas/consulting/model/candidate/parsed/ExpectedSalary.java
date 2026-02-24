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
public class ExpectedSalary{
    @JsonProperty("Amount")
private String amount;
    @JsonProperty("Symbol") 
private String symbol;
    @JsonProperty("Currency") 
private String currency;
    @JsonProperty("Unit") 
private String unit;
    @JsonProperty("Text") 
private String text;
}