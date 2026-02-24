package com.midas.consulting.controller.v1.response.microsoft.attachment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Value {
    private String odataType;
    private String odataMediaContentType;
    private String id;
    private String lastModifiedDateTime;
    private String name;
    private String contentType;
    private Integer size;
    private Boolean isInline;
    private String contentId;
    private Object contentLocation;
    private String contentBytes;
    public String getOdataType() {
        return odataType;
    }
    public void setOdataType(String odataType) {
        this.odataType = odataType;
    }
    public String getOdataMediaContentType() {
        return odataMediaContentType;
    }
    public void setOdataMediaContentType(String odataMediaContentType) {
        this.odataMediaContentType = odataMediaContentType;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }
    public void setLastModifiedDateTime(String lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public Boolean getIsInline() {
        return isInline;
    }
    public void setIsInline(Boolean isInline) {
        this.isInline = isInline;
    }
    public String getContentId() {
        return contentId;
    }
    public void setContentId(String contentId) {
        this.contentId = contentId;
    }
    public Object getContentLocation() {
        return contentLocation;
    }
    public void setContentLocation(Object contentLocation) {
        this.contentLocation = contentLocation;
    }
    public String getContentBytes() {
        return contentBytes;
    }
    public void setContentBytes(String contentBytes) {
        this.contentBytes = contentBytes;
    }
}