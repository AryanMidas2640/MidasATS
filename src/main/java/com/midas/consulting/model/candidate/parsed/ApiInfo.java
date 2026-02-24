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
public class ApiInfo {
    @JsonProperty("Metered")
    public String metered;
    @JsonProperty("CreditLeft")
    public String creditLeft;
    @JsonProperty("AccountExpiryDate")
    public String accountExpiryDate;
    @JsonProperty("BuildVersion")
    public String buildVersion;
}
   