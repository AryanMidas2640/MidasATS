package com.midas.consulting.controller.v1.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@ApiModel(description = "Bulk delete request for IP whitelist entries")
public class BulkDeleteIpRequest {

    @ApiModelProperty(value = "List of IP whitelist entry IDs to delete", required = true)
    @NotEmpty(message = "At least one ID must be provided")
    @Size(max = 100, message = "Cannot delete more than 100 entries at once")
    private List<String> ids;

    @ApiModelProperty(value = "Delete confirmation flag", required = true)
    private boolean confirmed = false;

    @ApiModelProperty(value = "Reason for bulk deletion")
    private String reason;

    @ApiModelProperty(value = "Force delete even if entries are currently active")
    private boolean forceDelete = false;

    @ApiModelProperty(value = "Skip validation for critical IP ranges")
    private boolean skipValidation = false;

    // Constructors
    public BulkDeleteIpRequest() {
    }

    public BulkDeleteIpRequest(List<String> ids, boolean confirmed, String reason) {
        this.ids = ids;
        this.confirmed = confirmed;
        this.reason = reason;
    }

    // Getters and Setters
    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isForceDelete() {
        return forceDelete;
    }

    public void setForceDelete(boolean forceDelete) {
        this.forceDelete = forceDelete;
    }

    public boolean isSkipValidation() {
        return skipValidation;
    }

    public void setSkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }
}
