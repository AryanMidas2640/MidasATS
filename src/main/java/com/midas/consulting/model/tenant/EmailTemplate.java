package com.midas.consulting.model.tenant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Map;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "email_templates")
public class EmailTemplate {

    @Id
    private String id;

    @Indexed
    private String tenantId;

    @NotBlank(message = "Template name is required")
    @Indexed
    private String templateName;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Template content is required")
    private String htmlContent;

    private String textContent; // Plain text version (optional)

    private String description;

    @NotNull
    private TemplateType templateType;

    private TemplateCategory category;

    // Variables that can be used in this template
    private Map<String, TemplateVariable> variables;

    // Default values for variables
    private Map<String, Object> defaultValues;

    // Template metadata
    private TemplateMetadata metadata;

    private boolean active = true;
    private boolean isSystem = false; // System templates cannot be deleted

    private Date dateCreated;
    private Date dateModified;
    private String createdBy;
    private String modifiedBy;

    // Version control
    private int version = 1;
    private String parentTemplateId; // For versioning

    // Usage statistics
    private long usageCount = 0;
    private Date lastUsed;

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class TemplateVariable {
        private String name;
        private String displayName;
        private String description;
        private VariableType type;
        private boolean required;
        private Object defaultValue;
        private String validation; // Regex for validation
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class TemplateMetadata {
        private String previewText;
        private String[] tags;
        private String thumbnailUrl;
        private Map<String, Object> customProperties;
    }

    public enum TemplateType {
        HTML("html"),
        TEXT("text"),
        MIXED("mixed");

        private final String value;

        TemplateType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum TemplateCategory {
        AUTHENTICATION("authentication"),
        NOTIFICATION("notification"),
        MARKETING("marketing"),
        SYSTEM("system"),
        WORKFLOW("workflow"),
        RECRUITMENT("recruitment"),
        ONBOARDING("onboarding"),
        CUSTOM("custom");

        private final String value;

        TemplateCategory(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum VariableType {
        STRING,
        NUMBER,
        BOOLEAN,
        DATE,
        URL,
        EMAIL,
        PHONE,
        OBJECT,
        ARRAY
    }
}