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
public class Reference{
    @JsonProperty("ReferenceName")
    public ReferenceName referenceName;
    @JsonProperty("Title") 
    public String title;
    @JsonProperty("Company") 
    public String company;
    @JsonProperty("Location") 
    public Location location;
    @JsonProperty("Telephones") 
    public ArrayList<Telephone> telephones;
    @JsonProperty("EmailAddresses") 
    public ArrayList<String> emailAddresses;
    @JsonProperty("WebAddresses") 
    public ArrayList<WebAddress> webAddresses;
}