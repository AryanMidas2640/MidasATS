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

public class Skills {

   @SerializedName("Raw")
   List<com.midas.consulting.controller.v1.response.parsing.textkernal.Raw> Raw;


    public void setRaw(List<com.midas.consulting.controller.v1.response.parsing.textkernal.Raw> Raw) {
        this.Raw = Raw;
    }
    public List<com.midas.consulting.controller.v1.response.parsing.textkernal.Raw> getRaw() {
        return Raw;
    }
    
}