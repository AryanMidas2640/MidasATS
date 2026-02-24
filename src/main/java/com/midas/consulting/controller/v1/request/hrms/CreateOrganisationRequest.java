package com.midas.consulting.controller.v1.request.hrms;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateOrganisationRequest {


    //    @NotEmpty(message = "{constraints.NotEmpty.message}")
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
//    @NotNull(message = "{constraints.NotEmpty.message}")
//    @Temporal(TemporalType.DATE)
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String organizationName;
    private String id;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String website;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private  String userId;

}