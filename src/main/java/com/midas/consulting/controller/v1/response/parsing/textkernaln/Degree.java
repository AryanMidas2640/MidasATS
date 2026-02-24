package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import com.midas.consulting.controller.v1.response.parsing.textkernal.Name;
import com.midas.consulting.controller.v1.response.parsing.textkernal.NormalizedInternational;
import com.midas.consulting.controller.v1.response.parsing.textkernal.NormalizedLocal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Degree {

   @SerializedName("Name")
   com.midas.consulting.controller.v1.response.parsing.textkernal.Name Name;

   @SerializedName("Type")
   String Type;

   @SerializedName("NormalizedLocal")
   com.midas.consulting.controller.v1.response.parsing.textkernal.NormalizedLocal NormalizedLocal;

   @SerializedName("NormalizedInternational")
   com.midas.consulting.controller.v1.response.parsing.textkernal.NormalizedInternational NormalizedInternational;


    public void setName(Name Name) {
        this.Name = Name;
    }
    public Name getName() {
        return Name;
    }
    
    public void setType(String Type) {
        this.Type = Type;
    }
    public String getType() {
        return Type;
    }
    
    public void setNormalizedLocal(NormalizedLocal NormalizedLocal) {
        this.NormalizedLocal = NormalizedLocal;
    }
    public NormalizedLocal getNormalizedLocal() {
        return NormalizedLocal;
    }
    
    public void setNormalizedInternational(NormalizedInternational NormalizedInternational) {
        this.NormalizedInternational = NormalizedInternational;
    }
    public NormalizedInternational getNormalizedInternational() {
        return NormalizedInternational;
    }
    
}