package com.midas.consulting.controller.v1.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Set;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter

public class TeamRequest {

    private String id;
    private String name;
    private String description;
    private String divisionId; // Reference to CompanyDivision
    private Boolean active;
    private Date dateCreated;
    private Date dateModified;
    private Set<String> userIdList;

}
