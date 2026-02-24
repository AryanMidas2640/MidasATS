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

public class TextKernelParsingRoot {

   @SerializedName("Info")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.Info Info;

   @SerializedName("Value")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.Value Value;


    public void setInfo(Info Info) {
        this.Info = Info;
    }
    public Info getInfo() {
        return Info;
    }
    
    public void setValue(Value Value) {
        this.Value = Value;
    }
    public Value getValue() {
        return Value;
    }
    
}