package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import com.midas.consulting.controller.v1.response.parsing.textkernal.Location;
import com.midas.consulting.controller.v1.response.parsing.textkernal.Name;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employer {

   @SerializedName("Name")
   com.midas.consulting.controller.v1.response.parsing.textkernal.Name Name;

   @SerializedName("Location")
   com.midas.consulting.controller.v1.response.parsing.textkernal.Location Location;


    public void setName(Name Name) {
        this.Name = Name;
    }
    public Name getName() {
        return Name;
    }
    
    public void setLocation(Location Location) {
        this.Location = Location;
    }
    public Location getLocation() {
        return Location;
    }
    
}