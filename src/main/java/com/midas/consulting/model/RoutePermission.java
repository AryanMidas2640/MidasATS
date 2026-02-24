package com.midas.consulting.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "routes")
public class RoutePermission {
    @Id
    private String id; // MongoDB automatically generates a unique ID
    @Indexed
    private String route;
    private Map<String, Boolean> permissions;
}