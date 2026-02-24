package com.midas.consulting.model.candidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "interviews")
public class Interview {

    @Id
    private String id;
    private String userID;
    private String jobID;
    private String candidateID;
    private String positionType;
    private String interviewDate;
    private String quotedBillRates;
    private String agreedBillRates;
    private String agreedPayRates;
    private String arrangedBy;
    private String primarySales;
    private String timeZone;
    private String comment;
    private boolean sendEmail;
    private Date dateCreated;
}