package com.midas.consulting.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "email_template_mapping")
@CompoundIndexes({
        @CompoundIndex(name = "tenant_template_idx", def = "{'tenantId': 1, 'templateName': 1}", unique = true)
})
public class EmailTemplateMapping {

    @Id
    private String id;

    private String tenantId;

    // Template identifier (e.g., "password-reset", "welcome-user", "interview-invitation")
    private String templateName;

    // The email config to use for this template
    private String emailConfigId;

    // Alternative config name (for lookup by name instead of ID)
    private String emailConfigName;

    // Override settings for this specific template
    private TemplateOverrides overrides;

    // Template-specific settings
    private TemplateSettings settings;

    private boolean active = true;
    private Date dateCreated;
    private Date dateModified;

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class TemplateOverrides {
        private String fromEmail;
        private String fromName;
        private String replyTo;
        private Map<String, String> customHeaders;
        private Integer priority; // Override priority for this template
    }

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class TemplateSettings {
        private String subject; // Default subject if not provided
        private String templatePath; // Path to template file
        private String templateEngine; // THYMELEAF, FREEMARKER, etc.
        private Map<String, Object> defaultVariables;
        private boolean requiresAuth = true;
        private String[] allowedRoles; // Which roles can send this template
    }
}