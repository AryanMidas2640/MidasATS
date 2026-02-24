// ============================================================================
// HYBRID IP REQUEST DTO
// ============================================================================

package com.midas.consulting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Request for creating hybrid IP whitelist entries (both user and role specific)")
public class HybridIpRequest {

    @ApiModelProperty(value = "IP address or CIDR range", required = true, example = "172.16.0.0/16")
    @NotBlank(message = "IP address is required")
    private String ipAddress;

    @ApiModelProperty(value = "Description of the IP whitelist entry", example = "Mixed access for project team and managers")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @ApiModelProperty(value = "Set of user IDs that this IP applies to")
    private Set<String> userIds;

    @ApiModelProperty(value = "Set of role IDs that this IP applies to")
    private Set<String> roleIds;

    @ApiModelProperty(value = "Priority level (higher number = higher priority)", example = "7")
    private Integer priority = 1;

    @ApiModelProperty(value = "Additional notes", example = "Flexible access for cross-functional teams")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Custom validation method (can be used with @AssertTrue annotation)
    public boolean isValid() {
        return (userIds != null && !userIds.isEmpty()) || (roleIds != null && !roleIds.isEmpty());
    }
}