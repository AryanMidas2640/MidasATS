package com.midas.consulting.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

// 6. Email Audit Model
@Document(collection = "email_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailAudit {
    @Id
    private String id;
    private String tenantId;
    private String recipient;
    private String subject;
    private String templateName;
    private String status;
    private String errorMessage;
    private Instant sentAt;

    @Indexed(expireAfterSeconds = 7776000) // 90 days TTL
    private Instant createdAt = Instant.now();
    public Map metadata;
    private String emailConfigId;
    private String configName;
}