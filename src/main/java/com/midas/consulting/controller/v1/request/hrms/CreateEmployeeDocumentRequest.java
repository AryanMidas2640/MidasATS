package com.midas.consulting.controller.v1.request.hrms;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateEmployeeDocumentRequest {
    private String id;
    private Object uploadDocStructure;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String employeeId;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private Date expiryDate;
    private String docDesc;
    private String docName;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String docType;
    private  String jobId;
}