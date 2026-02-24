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
public class Info{
    @JsonProperty("Code")
    public String code;
    @JsonProperty("Message") 
    public String message;
    @JsonProperty("TransactionId") 
    public String transactionId;
    @JsonProperty("EngineVersion") 
    public String engineVersion;
    @JsonProperty("ApiVersion") 
    public String apiVersion;
    @JsonProperty("TotalElapsedMilliseconds") 
    public int totalElapsedMilliseconds;
    @JsonProperty("TransactionCost") 
    public int transactionCost;
    @JsonProperty("CustomerDetails") 
    public CustomerDetails customerDetails;
}