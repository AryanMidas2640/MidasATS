package com.midas.consulting.controller.v1.request.hrms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateClientRequest {
    
    private String id;
    
    @NotBlank(message = "Client name is required")
    private String clientName;
    
    private String clientAddress;
    
    private String clientPOC;
    
    private String clientPOCPhone;
    
    @Email(message = "Invalid email format")
    private String clientPOCEmail;
    
    private String paymentTerms = "Net 30";
    
    private String clientWebsite;
}