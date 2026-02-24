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
public class NormalizedLocal {

   @SerializedName("Code")
   String Code;

   @SerializedName("Description")
   String Description;


    public void setCode(String Code) {
        this.Code = Code;
    }
    public String getCode() {
        return Code;
    }
    
    public void setDescription(String Description) {
        this.Description = Description;
    }
    public String getDescription() {
        return Description;
    }
    
}