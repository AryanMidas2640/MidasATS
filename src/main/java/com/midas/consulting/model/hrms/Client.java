package com.midas.consulting.model.hrms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "clients")
public class Client {

    @Id
    private String id;

    @Indexed(unique = true)
    private String clientName; // Required and unique

    private String clientAddress;

    private String clientPOC;

    private String clientPOCPhone;

    private String clientPOCEmail;

    private String paymentTerms = "Net 30"; // Default value

    private String clientWebsite;

    private String createdBy;

    private LocalDateTime dateModified;

    private LocalDateTime dateCreated;
}