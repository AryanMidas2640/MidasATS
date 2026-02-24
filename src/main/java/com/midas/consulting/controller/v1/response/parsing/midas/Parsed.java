package com.midas.consulting.controller.v1.response.parsing.midas;


import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"Companies worked at",
"degree",
"designition",
"email",
"name",
"phone",
"skills",
"total_exp",
"university",
        "fullText"
})
@Generated("jsonschema2pojo")
public class Parsed {

@JsonProperty("Companies worked at")
public List<Object> companiesworkedat;
@JsonProperty("degree")
public List<Object> degree;
@JsonProperty("designition")
public List<String> designition;
@JsonProperty("email")
public String email;
@JsonProperty("name")
public String name;
@JsonProperty("phone")
public String phone;
    @JsonProperty("fullText")
public  String fullText;
@JsonProperty("skills")
public List<String> skills;
@JsonProperty("total_exp")
public Integer totalExp;
@JsonProperty("university")
public List<Object> university;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}


