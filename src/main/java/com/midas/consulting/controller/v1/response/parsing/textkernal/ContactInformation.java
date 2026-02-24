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
public class ContactInformation{
    @JsonProperty("CandidateName")
    public CandidateName candidateName;
    @JsonProperty("Telephones") 
    public ArrayList<Telephone> telephones;
    @JsonProperty("EmailAddresses") 
    public ArrayList<String> emailAddresses;
    @JsonProperty("Location") 
    public Location location;
    @JsonProperty("WebAddresses") 
    public ArrayList<WebAddress> webAddresses;
}