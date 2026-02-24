package com.midas.consulting.controller.v1.request.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndexingOptions{
    @JsonProperty("IndexId")
    public String indexId;
    @JsonProperty("DocumentId") 
    public String documentId;
    @JsonProperty("UserDefinedTags") 
    public ArrayList<String> userDefinedTags;
}