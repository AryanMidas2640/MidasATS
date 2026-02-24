package com.midas.consulting.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "Request for creating role-specific IP whitelist entries")
public class RoleSpecificIpRequest {

    @ApiModelProperty(value = "IP address or CIDR range", required = true, example = "10.0.0.0/24")
    @NotBlank(message = "IP address is required")
    private String ipAddress;

    @ApiModelProperty(value = "Description of the IP whitelist entry", example = "VPN access for admin roles")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @ApiModelProperty(value = "Set of role IDs that this IP applies to", required = true)
    @NotEmpty(message = "At least one role ID is required")
    private Set<String> roleIds;

    @ApiModelProperty(value = "Priority level (higher number = higher priority)", example = "10")
    private Integer priority = 1;

    @ApiModelProperty(value = "Additional notes", example = "Corporate network access for administrators")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private String roleSpecString;
}

