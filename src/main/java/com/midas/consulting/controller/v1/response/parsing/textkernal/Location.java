package com.midas.consulting.controller.v1.response.parsing.textkernal;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Location{
    @JsonProperty("CountryCode")
    public String countryCode;
    @JsonProperty("PostalCode") 
    public String postalCode;
    @JsonProperty("Regions") 
    public ArrayList<String> regions;
    @JsonProperty("Municipality") 
    public String municipality;
    @JsonProperty("StreetAddressLines") 
    public ArrayList<String> streetAddressLines;
    @JsonProperty("GeoCoordinates") 
    public GeoCoordinates geoCoordinates;
}