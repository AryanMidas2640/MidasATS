package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerDetails {

   @SerializedName("AccountId")
   String AccountId;

   @SerializedName("Name")
   String Name;

   @SerializedName("IPAddress")
   String IPAddress;

   @SerializedName("Region")
   Date Region;

   @SerializedName("CreditsRemaining")
   int CreditsRemaining;

   @SerializedName("CreditsUsed")
   int CreditsUsed;

   @SerializedName("ExpirationDate")
   Date ExpirationDate;

   @SerializedName("MaximumConcurrentRequests")
   int MaximumConcurrentRequests;


    public void setAccountId(String AccountId) {
        this.AccountId = AccountId;
    }
    public String getAccountId() {
        return AccountId;
    }
    
    public void setName(String Name) {
        this.Name = Name;
    }
    public String getName() {
        return Name;
    }
    
    public void setIPAddress(String IPAddress) {
        this.IPAddress = IPAddress;
    }
    public String getIPAddress() {
        return IPAddress;
    }
    
    public void setRegion(Date Region) {
        this.Region = Region;
    }
    public Date getRegion() {
        return Region;
    }
    
    public void setCreditsRemaining(int CreditsRemaining) {
        this.CreditsRemaining = CreditsRemaining;
    }
    public int getCreditsRemaining() {
        return CreditsRemaining;
    }
    
    public void setCreditsUsed(int CreditsUsed) {
        this.CreditsUsed = CreditsUsed;
    }
    public int getCreditsUsed() {
        return CreditsUsed;
    }
    
    public void setExpirationDate(Date ExpirationDate) {
        this.ExpirationDate = ExpirationDate;
    }
    public Date getExpirationDate() {
        return ExpirationDate;
    }
    
    public void setMaximumConcurrentRequests(int MaximumConcurrentRequests) {
        this.MaximumConcurrentRequests = MaximumConcurrentRequests;
    }
    public int getMaximumConcurrentRequests() {
        return MaximumConcurrentRequests;
    }
    
}