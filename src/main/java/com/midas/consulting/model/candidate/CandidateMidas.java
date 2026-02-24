package com.midas.consulting.model.candidate;

import com.midas.consulting.controller.v1.response.microsoft.fileuploads.FileHandle;
import com.midas.consulting.model.hrms.ChangeLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)

@Document(collection = "candidateMidas")

public class CandidateMidas {
    @Id
    private String id;
    @Indexed(unique = true)
    private String email;
    @Indexed(unique = true)
    private String phone;
    private String name;
    private String city;
    private String state;
    private String zip;
    @Indexed(unique = true)
    private List<String> skills;
    private List<Object> degree;
    private List<Object> certifications;
    private List<Object> companiesWorkedAt;
    private List<Object> university;
    private List<Object> languages;
    private String linkedin;
    private String github;
    private String website;
    private String summary;
    private List<Object> designation;
    private List<Object> projects;
    private List<Object> experience;
    private List<Object> education;
    private Boolean active;
    private String totalExp;
    private String source;
    private String regions;
    private String municipality;
    private String profession;
    private String primarySpeciality;
    private String desiredShifts;
    private String otherPhone;
    private String dateOfBirth;
    private String workAuthorization;
    private String gender;
    private String currentCTC;
    private List<Object> licenses;
    private List<String> license;
    private String licenseNumber;
    private Date dateIssued;
    private String issuingState;
    private Date expirationDate;
    private Boolean hasLicenseInvestigated;
    private String investigationDetails;
    private String licensedStates;
    private String contactTime;
    private Boolean eligibleToWorkUS;
    private String preferredDestinations;
    private String travelStatus;
    private List<String> preferredCities;
    private String lastName;
    @TextIndexed
    private String fullText;
    private Date date_added;
    private Date last_updated;
    private FileHandle fileHandle;
    @DBRef
    private ChangeLog changeLog;
    private Map<String, Object> resumeSegments = new LinkedHashMap<>();
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();
}
