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
public class LanguageCompetencies {

   @SerializedName("Language")
   String Language;

   @SerializedName("LanguageCode")
   String LanguageCode;

   @SerializedName("FoundInContext")
   String FoundInContext;


    public void setLanguage(String Language) {
        this.Language = Language;
    }
    public String getLanguage() {
        return Language;
    }
    
    public void setLanguageCode(String LanguageCode) {
        this.LanguageCode = LanguageCode;
    }
    public String getLanguageCode() {
        return LanguageCode;
    }
    
    public void setFoundInContext(String FoundInContext) {
        this.FoundInContext = FoundInContext;
    }
    public String getFoundInContext() {
        return FoundInContext;
    }
    
}