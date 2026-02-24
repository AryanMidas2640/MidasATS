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
   
public class EducationNormalizationResponse {

   @SerializedName("Code")
   String Code;

   @SerializedName("Message")
   String Message;


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
    
}