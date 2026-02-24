package com.midas.consulting.controller.v1.response.microsoft.fileuploads;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"@odata.context",
"@microsoft.graph.downloadUrl",
"createdDateTime",
"eTag",
"id",
"lastModifiedDateTime",
"name",
"webUrl",
"cTag",
"size",
"createdBy",
"lastModifiedBy",
"parentReference",
"file",
"fileSystemInfo",
"shared"
})
@Generated("jsonschema2pojo")
public class FileHandle {

@JsonProperty("@odata.context")
private String odataContext;
@JsonProperty("@microsoft.graph.downloadUrl")
private String microsoftGraphDownloadUrl;
@JsonProperty("createdDateTime")
private String createdDateTime;
@JsonProperty("eTag")
private String eTag;
@JsonProperty("id")
private String id;
@JsonProperty("lastModifiedDateTime")
private String lastModifiedDateTime;
@JsonProperty("name")
private String name;
@JsonProperty("webUrl")
private String webUrl;
@JsonProperty("cTag")
private String cTag;
@JsonProperty("size")
private Integer size;
@JsonProperty("createdBy")
private CreatedBy createdBy;
@JsonProperty("lastModifiedBy")
private LastModifiedBy lastModifiedBy;
@JsonProperty("parentReference")
private ParentReference parentReference;
@JsonProperty("file")
private File file;
@JsonProperty("fileSystemInfo")
private FileSystemInfo fileSystemInfo;
@JsonProperty("shared")
private Shared shared;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("@odata.context")
public String getOdataContext() {
return odataContext;
}

@JsonProperty("@odata.context")
public void setOdataContext(String odataContext) {
this.odataContext = odataContext;
}

@JsonProperty("@microsoft.graph.downloadUrl")
public String getMicrosoftGraphDownloadUrl() {
return microsoftGraphDownloadUrl;
}

@JsonProperty("@microsoft.graph.downloadUrl")
public void setMicrosoftGraphDownloadUrl(String microsoftGraphDownloadUrl) {
this.microsoftGraphDownloadUrl = microsoftGraphDownloadUrl;
}

@JsonProperty("createdDateTime")
public String getCreatedDateTime() {
return createdDateTime;
}

@JsonProperty("createdDateTime")
public void setCreatedDateTime(String createdDateTime) {
this.createdDateTime = createdDateTime;
}

@JsonProperty("eTag")
public String geteTag() {
return eTag;
}

@JsonProperty("eTag")
public void seteTag(String eTag) {
this.eTag = eTag;
}

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("lastModifiedDateTime")
public String getLastModifiedDateTime() {
return lastModifiedDateTime;
}

@JsonProperty("lastModifiedDateTime")
public void setLastModifiedDateTime(String lastModifiedDateTime) {
this.lastModifiedDateTime = lastModifiedDateTime;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("webUrl")
public String getWebUrl() {
return webUrl;
}

@JsonProperty("webUrl")
public void setWebUrl(String webUrl) {
this.webUrl = webUrl;
}

@JsonProperty("cTag")
public String getcTag() {
return cTag;
}

@JsonProperty("cTag")
public void setcTag(String cTag) {
this.cTag = cTag;
}

@JsonProperty("size")
public Integer getSize() {
return size;
}

@JsonProperty("size")
public void setSize(Integer size) {
this.size = size;
}

@JsonProperty("createdBy")
public CreatedBy getCreatedBy() {
return createdBy;
}

@JsonProperty("createdBy")
public void setCreatedBy(CreatedBy createdBy) {
this.createdBy = createdBy;
}

@JsonProperty("lastModifiedBy")
public LastModifiedBy getLastModifiedBy() {
return lastModifiedBy;
}

@JsonProperty("lastModifiedBy")
public void setLastModifiedBy(LastModifiedBy lastModifiedBy) {
this.lastModifiedBy = lastModifiedBy;
}

@JsonProperty("parentReference")
public ParentReference getParentReference() {
return parentReference;
}

@JsonProperty("parentReference")
public void setParentReference(ParentReference parentReference) {
this.parentReference = parentReference;
}

@JsonProperty("file")
public File getFile() {
return file;
}

@JsonProperty("file")
public void setFile(File file) {
this.file = file;
}

@JsonProperty("fileSystemInfo")
public FileSystemInfo getFileSystemInfo() {
return fileSystemInfo;
}

@JsonProperty("fileSystemInfo")
public void setFileSystemInfo(FileSystemInfo fileSystemInfo) {
this.fileSystemInfo = fileSystemInfo;
}

@JsonProperty("shared")
public Shared getShared() {
return shared;
}

@JsonProperty("shared")
public void setShared(Shared shared) {
this.shared = shared;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}
