package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Service{
    @JsonProperty("Name")
    public String name;
    @JsonProperty("Branch") 
    public String branch;
    @JsonProperty("Rank") 
    public String rank;
}
