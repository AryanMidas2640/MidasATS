package com.midas.consulting.controller.v1.request.hrms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateFacilityRequest {
    
    private String id;
    
    @NotBlank(message = "Parent client ID is required")
    private String parentClientID;
    
    @NotBlank(message = "Facility name is required")
    private String facilityName;
    
    private String facilityAddress;
}