package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Info {

   @SerializedName("Code")
   String Code;

   @SerializedName("Message")
   String Message;

   @SerializedName("TransactionId")
   String TransactionId;

   @SerializedName("EngineVersion")
   String EngineVersion;

   @SerializedName("ApiVersion")
   String ApiVersion;

   @SerializedName("TotalElapsedMilliseconds")
   int TotalElapsedMilliseconds;

   @SerializedName("TransactionCost")
   double TransactionCost;

   @SerializedName("CustomerDetails")
   com.midas.consulting.controller.v1.response.parsing.textkernal.CustomerDetails CustomerDetails;


    public void setCode(String Code) {
        this.Code = Code;
    }
    public String getCode() {
        return Code;
    }
    
    public void setMessage(String Message) {
        this.Message = Message;
    }
    public String getMessage() {
        return Message;
    }
    
    public void setTransactionId(String TransactionId) {
        this.TransactionId = TransactionId;
    }
    public String getTransactionId() {
        return TransactionId;
    }
    
    public void setEngineVersion(String EngineVersion) {
        this.EngineVersion = EngineVersion;
    }
    public String getEngineVersion() {
        return EngineVersion;
    }
    
    public void setApiVersion(String ApiVersion) {
        this.ApiVersion = ApiVersion;
    }
    public String getApiVersion() {
        return ApiVersion;
    }
    
    public void setTotalElapsedMilliseconds(int TotalElapsedMilliseconds) {
        this.TotalElapsedMilliseconds = TotalElapsedMilliseconds;
    }
    public int getTotalElapsedMilliseconds() {
        return TotalElapsedMilliseconds;
    }
    
    public void setTransactionCost(double TransactionCost) {
        this.TransactionCost = TransactionCost;
    }
    public double getTransactionCost() {
        return TransactionCost;
    }
    
    public void setCustomerDetails(com.midas.consulting.controller.v1.response.parsing.textkernal.CustomerDetails CustomerDetails) {
        this.CustomerDetails = CustomerDetails;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernal.CustomerDetails getCustomerDetails() {
        return CustomerDetails;
    }
    
}