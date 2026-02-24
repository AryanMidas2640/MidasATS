// ============================================================================
// USER SPECIFIC IP REQUEST DTO
// ============================================================================

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
@ApiModel(description = "Request for creating user-specific IP whitelist entries")
public class UserSpecificIpRequest {

    @ApiModelProperty(value = "IP address or CIDR range", required = true, example = "192.168.1.100")
    @NotBlank(message = "IP address is required")
    private String ipAddress;

    @ApiModelProperty(value = "Description of the IP whitelist entry", example = "Home office access for development team")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @ApiModelProperty(value = "Set of user IDs that this IP applies to", required = true)
    @NotEmpty(message = "At least one user ID is required")
    private Set<String> userIds;

    @ApiModelProperty(value = "Priority level (higher number = higher priority)", example = "5")
    private Integer priority = 1;

    @ApiModelProperty(value = "Additional notes", example = "Temporary access for Q4 project")
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    private  String userSpecString;
}

// ============================================================================
// ROLE SPECIFIC IP REQUEST DTO
// ============================================================================

