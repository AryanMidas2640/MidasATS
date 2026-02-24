package com.midas.consulting.controller.v1.response.microsoft.folders;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Value {
    private String id;
    private String displayName;
    private String parentFolderId;
    private Integer childFolderCount;
    private Integer unreadItemCount;
    private Integer totalItemCount;
    private Integer sizeInBytes;
    private Boolean isHidden;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getParentFolderId() {
        return parentFolderId;
    }
    public void setParentFolderId(String parentFolderId) {
        this.parentFolderId = parentFolderId;
    }
    public Integer getChildFolderCount() {
        return childFolderCount;
    }
    public void setChildFolderCount(Integer childFolderCount) {
        this.childFolderCount = childFolderCount;
    }
    public Integer getUnreadItemCount() {
        return unreadItemCount;
    }
    public void setUnreadItemCount(Integer unreadItemCount) {
        this.unreadItemCount = unreadItemCount;
    }
    public Integer getTotalItemCount() {
        return totalItemCount;
    }
    public void setTotalItemCount(Integer totalItemCount) {
        this.totalItemCount = totalItemCount;
    }
    public Integer getSizeInBytes() {
        return sizeInBytes;
    }
    public void setSizeInBytes(Integer sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
    public Boolean getIsHidden() {
        return isHidden;
    }
    public void setIsHidden(Boolean isHidden) {
        this.isHidden = isHidden;
    }
}