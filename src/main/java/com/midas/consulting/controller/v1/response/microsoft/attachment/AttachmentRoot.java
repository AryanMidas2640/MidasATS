package com.midas.consulting.controller.v1.response.microsoft.attachment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class AttachmentRoot {
    private String odataContext;
    private List<Value> value = new ArrayList<Value>();
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
}

