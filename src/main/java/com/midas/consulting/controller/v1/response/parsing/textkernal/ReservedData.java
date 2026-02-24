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
public class ReservedData{
    @JsonProperty("Phones")
    public ArrayList<String> phones;
    @JsonProperty("Names") 
    public ArrayList<String> names;
    @JsonProperty("EmailAddresses") 
    public ArrayList<String> emailAddresses;
    @JsonProperty("Urls") 
    public ArrayList<String> urls;
    @JsonProperty("OtherData") 
    public ArrayList<String> otherData;
}

