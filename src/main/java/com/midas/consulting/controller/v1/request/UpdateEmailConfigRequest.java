package com.midas.consulting.controller.v1.request;

import com.midas.consulting.model.EmailConfig;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import java.util.List;

@Data
@NoArgsConstructor
public class UpdateEmailConfigRequest {
    private String description;
    
    @Email
    private String emailAddress;
    
    private String displayName;
    private List<String> templateMappings;
    private List<String> ccList;
    private List<String> bccList;
    private Integer priority;
    private Boolean active;
    
    // Provider configuration updates
    private EmailConfig.EmailServiceProvider provider;
    private EmailConfig.SmtpConfig smtpConfig;
    private EmailConfig.SendGridConfig sendGridConfig;
    private EmailConfig.AwsSesConfig awsSesConfig;
}