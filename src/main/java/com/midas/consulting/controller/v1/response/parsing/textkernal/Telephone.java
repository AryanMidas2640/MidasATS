package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Telephone{
    @JsonProperty("Raw")
    public String raw;
    @JsonProperty("Normalized") 
    public String normalized;
    @JsonProperty("InternationalCountryCode") 
    public String internationalCountryCode;
    @JsonProperty("AreaCityCode") 
    public String areaCityCode;
    @JsonProperty("SubscriberNumber") 
    public String subscriberNumber;
}

