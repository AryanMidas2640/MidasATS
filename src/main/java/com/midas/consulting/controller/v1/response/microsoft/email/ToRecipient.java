package com.midas.consulting.controller.v1.response.microsoft.email;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ToRecipient {
    private EmailAddress emailAddress;
    public EmailAddress getEmailAddress() {
        return emailAddress;
    }
    public void setEmailAddress(EmailAddress emailAddress) {
        this.emailAddress = emailAddress;
    }
}