package com.midas.consulting.controller.v1.request.hrms;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateVMSRequest {


    private String id;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String name;

    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String url;

//    @NotEmpty(message = "{constraints.NotEmpty.message}")
//    private String orgCode;
//    @NotEmpty(message = "{constraints.NotEmpty.message}")
//    private  String userId="";
//    @NotEmpty(message = "{constraints.NotEmpty.message}")
//    private  String password="";
//    @NotEmpty(message = "{constraints.NotEmpty.message}")
//    private  String parentOrganization;
}