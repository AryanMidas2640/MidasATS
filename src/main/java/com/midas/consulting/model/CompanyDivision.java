package com.midas.consulting.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "division")

public class CompanyDivision {
    @Id
    private String id;
    @Indexed(unique = false, direction = IndexDirection.ASCENDING)
    private String name;
    private String description;
    private String code;
    private Boolean active;
    private Date dateCreated;
    private Date dateModified;
}
