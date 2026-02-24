package com.midas.consulting.controller.v1.request.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodeOptions{
    @JsonProperty("IncludeGeocoding")
    public boolean includeGeocoding;
    @JsonProperty("Provider") 
    public String provider;
    @JsonProperty("ProviderKey") 
    public String providerKey;
    @JsonProperty("PostalAddress") 
    public PostalAddress postalAddress;
    @JsonProperty("GeoCoordinates") 
    public GeoCoordinates geoCoordinates;
}
