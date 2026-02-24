package com.midas.consulting.model;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "tenant")
public class Tenant {
    @Id
    private String id;
    private String connectionString;
    private String connectionStringNode;
    private String connectionStringJobSync;
    private String tenantName;

    private String host;
    private String subdomain;
    private String email;
    private String webUrl;
    private String logo;
    private String checklistUrl;
    private Map<String, Object> connectionPoolConfig;
//    private EmailConfig emailConfig;
//private  EmailConfiguration emailConfig;

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class EmailConfiguration {
//        private String smtpHost;
//        private int smtpPort;
//        private String username;
//        private String password;
//        private boolean enableTls;
//        private boolean enableSsl;
//        private String fromAddress;
//        private String fromName;
//        private Map<String, String> customProperties;
//
//        // Email template settings
//        private String templatePath;
//        private String logoUrl;
//        private String brandColor;
//    }

}

