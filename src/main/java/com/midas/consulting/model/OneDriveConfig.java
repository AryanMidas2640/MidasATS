package com.midas.consulting.model;
import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "onedriveConfigurations")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class OneDriveConfig {

    @Indexed(unique = true, direction = IndexDirection.ASCENDING)
    private String name;
    @Id
    private String id;
    @Indexed(unique = true, direction = IndexDirection.ASCENDING)
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String versionName;
    private String email;
    private String resourceId;
    private String grantType ;
    private String tokenEndpoint ;
}