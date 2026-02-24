package com.midas.consulting.controller.v1.request.hrms;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateEmployeeRequest {

    private String id;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String address;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String name;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String userId;


    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String organizationId;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String city;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String contactDetails;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Date createDate;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String dob;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String email;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Date modifyDate;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String ssn;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String state;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Boolean status;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String zipCode;
    private List<String> projects;
}