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
public class CustomerDetails{
    @JsonProperty("AccountId")
    public String accountId;
    @JsonProperty("Name") 
    public String name;
    @JsonProperty("IPAddress") 
    public String iPAddress;
    @JsonProperty("Region") 
    public String region;
    @JsonProperty("CreditsRemaining") 
    public int creditsRemaining;
    @JsonProperty("CreditsUsed") 
    public int creditsUsed;
    @JsonProperty("ExpirationDate") 
    public String expirationDate;
    @JsonProperty("MaximumConcurrentRequests") 
    public int maximumConcurrentRequests;
}
