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
public class ReferenceName{
    @JsonProperty("FormattedName")
    public String formattedName;
    @JsonProperty("Prefix") 
    public String prefix;
    @JsonProperty("GivenName") 
    public String givenName;
    @JsonProperty("Moniker") 
    public String moniker;
    @JsonProperty("MiddleName") 
    public String middleName;
    @JsonProperty("FamilyName") 
    public String familyName;
    @JsonProperty("Suffix") 
    public String suffix;
}
