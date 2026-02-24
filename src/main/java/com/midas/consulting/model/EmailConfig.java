package com.midas.consulting.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "email_config")
@CompoundIndexes({
        @CompoundIndex(name = "tenant_config_idx", def = "{'tenantId': 1, 'configName': 1}", unique = true),
        @CompoundIndex(name = "tenant_template_idx", def = "{'tenantId': 1, 'templateMappings': 1}")
})
public class EmailConfig {


    @Id
    private String id;



    // Unique configuration name within tenant
    @Indexed
    private String configName; // e.g., "hr-notifications", "candidate-emails", "system-alerts"

    // Description for this configuration
    private String description;

    // Email account settings
    private String emailAddress; // The actual email address for this config
    private String displayName;  // Display name for this email

    // Template mappings - which templates use this config
    private List<String> templateMappings; // e.g., ["password-reset", "welcome-user"]

    // Priority for fallback (lower number = higher priority)
    private int priority = 100;

    // SMTP Configuration
    private SmtpConfig smtpConfig;

    // Email Service Provider Configuration
    private EmailServiceProvider provider;
    private SendGridConfig sendGridConfig;
    private AwsSesConfig awsSesConfig;
    private OutlookConfig outlookConfig;
    private GmailConfig gmailConfig;

    private List<String> ccList;
    private  List<String> bccList;

    // Rate limiting
    private RateLimitConfig rateLimitConfig;



    // General Settings
    private String defaultReplyTo;
    private boolean enableBounceTracking;
    private boolean enableClickTracking;
    private boolean enableOpenTracking;

    // Custom headers for this config
    private Map<String, String> customHeaders;

    // Audit fields
    private Date dateCreated;
    private Date dateModified;
    private boolean active;
    private String createdBy;
    private String modifiedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class SmtpConfig {
        private String host;
        private int port = 587;
        private String username;
        private String password;
        private boolean enableTls = true;
        private boolean enableSsl = false;
        private String authMethod = "LOGIN";
        private int connectionTimeout = 30000;
        private int socketTimeout = 30000;
        private Map<String, String> additionalProperties;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class RateLimitConfig {
        private boolean enabled = true;
        private int maxPerHour = 100;
        private int maxPerDay = 1000;
        private int maxPerMonth = 10000;
        private Date resetDate; // For manual reset
    }


    // Template Configuration
    private EmailTemplateConfig templateConfig;

    // General Settings
    private String defaultFromEmail;
    private String defaultFromName;

    private int dailyLimit;
    private int monthlyLimit;

    // Branding
    private String companyLogo;
    private String companyName;
    private String footerText;


    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class SendGridConfig {
        private String apiKey;
        private String fromEmail;
        private String fromName;
        private boolean enableTracking = true;
        private String webhookUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class AwsSesConfig {
        private String accessKey;
        private String secretKey;
        private String region;
        private String fromEmail;
        private String fromName;
        private String configurationSet;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class OutlookConfig {
        private String clientId;
        private String clientSecret;
        private String tenantId;
        private String fromEmail;
        private String accessToken;
        private String refreshToken;
        private Date tokenExpiry;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class GmailConfig {
        private String clientId;
        private String clientSecret;
        private String fromEmail;
        private String accessToken;
        private String refreshToken;
        private Date tokenExpiry;
        private String credentialsPath;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class EmailTemplateConfig {
        private String templateEngine = "THYMELEAF"; // THYMELEAF, FREEMARKER, VELOCITY
        private String templatePath = "/email-templates/";
        private String defaultTemplate = "default";
        private Map<String, String> customTemplates;
        private boolean enableCustomCss = true;
        private String customCssUrl;
    }

    public enum EmailServiceProvider {
        SMTP("smtp"),
        SENDGRID("sendgrid"),
        AWS_SES("aws_ses"),
        OUTLOOK("outlook"),
        GMAIL("gmail");

        private final String value;

        EmailServiceProvider(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}