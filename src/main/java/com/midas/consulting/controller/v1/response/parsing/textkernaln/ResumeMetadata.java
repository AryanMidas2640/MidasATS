package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import com.midas.consulting.controller.v1.response.parsing.textkernal.ResumeQuality;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResumeMetadata {

   @SerializedName("FoundSections")
   List<FoundSections> FoundSections;

   @SerializedName("ResumeQuality")
   List<com.midas.consulting.controller.v1.response.parsing.textkernal.ResumeQuality> ResumeQuality;

   @SerializedName("PlainText")
   String PlainText;

   @SerializedName("DocumentLanguage")
   String DocumentLanguage;

   @SerializedName("DocumentCulture")
   String DocumentCulture;

   @SerializedName("ParserSettings")
   String ParserSettings;

   @SerializedName("DocumentLastModified")
   Date DocumentLastModified;


    public void setFoundSections(List<FoundSections> FoundSections) {
        this.FoundSections = FoundSections;
    }
    public List<FoundSections> getFoundSections() {
        return FoundSections;
    }
    
    public void setResumeQuality(List<ResumeQuality> ResumeQuality) {
        this.ResumeQuality = ResumeQuality;
    }
    public List<ResumeQuality> getResumeQuality() {
        return ResumeQuality;
    }
    
    public void setPlainText(String PlainText) {
        this.PlainText = PlainText;
    }
    public String getPlainText() {
        return PlainText;
    }
    
    public void setDocumentLanguage(String DocumentLanguage) {
        this.DocumentLanguage = DocumentLanguage;
    }
    public String getDocumentLanguage() {
        return DocumentLanguage;
    }
    
    public void setDocumentCulture(String DocumentCulture) {
        this.DocumentCulture = DocumentCulture;
    }
    public String getDocumentCulture() {
        return DocumentCulture;
    }
    
    public void setParserSettings(String ParserSettings) {
        this.ParserSettings = ParserSettings;
    }
    public String getParserSettings() {
        return ParserSettings;
    }
    
    public void setDocumentLastModified(Date DocumentLastModified) {
        this.DocumentLastModified = DocumentLastModified;
    }
    public Date getDocumentLastModified() {
        return DocumentLastModified;
    }
    
}