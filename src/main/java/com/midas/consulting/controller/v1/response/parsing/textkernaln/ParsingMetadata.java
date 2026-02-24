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
   
public class ParsingMetadata {

   @SerializedName("ElapsedMilliseconds")
   int ElapsedMilliseconds;

   @SerializedName("TimedOut")
   boolean TimedOut;


    public void setElapsedMilliseconds(int ElapsedMilliseconds) {
        this.ElapsedMilliseconds = ElapsedMilliseconds;
    }
    public int getElapsedMilliseconds() {
        return ElapsedMilliseconds;
    }
    
    public void setTimedOut(boolean TimedOut) {
        this.TimedOut = TimedOut;
    }
    public boolean getTimedOut() {
        return TimedOut;
    }
    
}