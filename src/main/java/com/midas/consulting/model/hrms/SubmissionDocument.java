package com.midas.consulting.model.hrms;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "submissionDoc")
public class SubmissionDocument {
    @Id
    private String id;
    private Object uploadDocStructure;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    private String expiryDate;
    private String docDesc;
    private String docName;
    private String docType;
    private String attachmentType;
    private String achieveDate;
    private String vmsName;
    private String email;
    private String employeeId;
private  String srcId;
    private String status;
    private String comment;
    private String completeDate;
    private String meetRequirements;
    private String passFailResponse;
    private String certificateNumber;
}