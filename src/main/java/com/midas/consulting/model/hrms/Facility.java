package com.midas.consulting.model.hrms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Document(collection = "facilities")
public class Facility {

    @Id
    private String id;

    @DBRef
    private Client parentClient; // Reference to parent client

    private String facilityName; // Required

    private String facilityAddress;

    private String createdBy;

    private LocalDateTime dateModified;

    private LocalDateTime dateCreated;
}