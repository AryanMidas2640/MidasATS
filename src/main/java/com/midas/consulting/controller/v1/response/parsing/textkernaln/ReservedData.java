package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
   
public class ReservedData {

   @SerializedName("Phones")
   List<String> Phones;

   @SerializedName("Names")
   List<String> Names;

   @SerializedName("EmailAddresses")
   List<String> EmailAddresses;


    public void setPhones(List<String> Phones) {
        this.Phones = Phones;
    }
    public List<String> getPhones() {
        return Phones;
    }
    
    public void setNames(List<String> Names) {
        this.Names = Names;
    }
    public List<String> getNames() {
        return Names;
    }
    
    public void setEmailAddresses(List<String> EmailAddresses) {
        this.EmailAddresses = EmailAddresses;
    }
    public List<String> getEmailAddresses() {
        return EmailAddresses;
    }
    
}