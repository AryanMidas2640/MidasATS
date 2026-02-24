package com.midas.consulting.controller.v1.response.microsoft.email;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Flag {
    private String flagStatus;
    public String getFlagStatus() {
        return flagStatus;
    }
    public void setFlagStatus(String flagStatus) {
        this.flagStatus = flagStatus;
    }
}