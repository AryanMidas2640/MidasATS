package com.midas.consulting.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.midas.consulting.model.IpWhitelistScope;
import com.midas.consulting.model.IpWhitelistType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IpWhitelistResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("ipAddress")
    private String ipAddress;

    @JsonProperty("description")
    private String description;

    @JsonProperty("scope")
    private IpWhitelistScope scope;

    @JsonProperty("type")
    private IpWhitelistType type;

    @JsonProperty("allowedUserIds")
    private Set<String> allowedUserIds;

    @JsonProperty("allowedRoleIds")
    private Set<String> allowedRoleIds;

    @JsonProperty("priority")
    private Integer priority;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("updatedBy")
    private String updatedBy;

    @JsonProperty("createdAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    @JsonProperty("lastAccessedAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastAccessedAt;

    @JsonProperty("lastAccessedBy")
    private String lastAccessedBy;

    @JsonProperty("lastAccessedIp")
    private String lastAccessedIp;

    @JsonProperty("accessCount")
    private Long accessCount;
}