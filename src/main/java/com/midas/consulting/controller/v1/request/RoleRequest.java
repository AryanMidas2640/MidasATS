package com.midas.consulting.controller.v1.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Request DTO for creating or updating a Role
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "Request object for role creation and update operations")
public class RoleRequest {
    
    @ApiModelProperty(
        value = "Role name (uppercase alphanumeric with underscores)",
        required = true,
        example = "SENIOR_RECRUITER",
        notes = "Must be unique and follow naming convention"
    )
    @NotBlank(message = "Role name is required")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Pattern(
        regexp = "^[A-Z][A-Z0-9_]*$",
        message = "Role name must start with uppercase letter and contain only uppercase letters, numbers, and underscores"
    )
    private String role;
    
    @ApiModelProperty(
        value = "Optional description of the role",
        example = "Senior-level recruiter with team management responsibilities"
    )
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}