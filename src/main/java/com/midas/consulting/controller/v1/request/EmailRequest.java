package com.midas.consulting.controller.v1.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// 5. Email Request Model
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
    private String to;
    private String subject;
    private String content;
    private boolean html;
    private String templateName;
    private Map<String, Object> templateVariables;
    private List<EmailAttachment> attachments;
private List<String> ccList;
private List<String> bccList;
    private String configName;
}

