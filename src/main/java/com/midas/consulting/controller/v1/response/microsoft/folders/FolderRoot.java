package com.midas.consulting.controller.v1.response.microsoft.folders;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class FolderRoot {
    private String odataContext;
    private List<Value> value = new ArrayList<Value>();
    private String odataNextLink;
    public String getOdataContext() {
        return odataContext;
    }
    public void setOdataContext(String odataContext) {
        this.odataContext = odataContext;
    }
    public List<Value> getValue() {
        return value;
    }
    public void setValue(List<Value> value) {
        this.value = value;
    }
    public String getOdataNextLink() {
        return odataNextLink;
    }
    public void setOdataNextLink(String odataNextLink) {
        this.odataNextLink = odataNextLink;
    }
}