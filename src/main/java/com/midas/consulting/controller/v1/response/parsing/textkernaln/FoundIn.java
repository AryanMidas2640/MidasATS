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
public class FoundIn {

   @SerializedName("SectionType")
   String SectionType;

   @SerializedName("Id")
   Date Id;


    public void setSectionType(String SectionType) {
        this.SectionType = SectionType;
    }
    public String getSectionType() {
        return SectionType;
    }
    
    public void setId(Date Id) {
        this.Id = Id;
    }
    public Date getId() {
        return Id;
    }
    
}