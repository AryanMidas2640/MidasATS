package com.midas.consulting.controller.v1.request.candidate;

import com.midas.consulting.controller.v1.response.microsoft.fileuploads.FileHandle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class CreateCandidateRequestMidas {
    private String id;
    public List<Object> companiesWorkedAt;
    public List<Object> degree;
    public List<Designation> designation;
    public String email;
    public String name;
    public String phone;
    public List<String> skills;
    public String totalExp;
    public List<Object> university;
    public String regions;
    public String municipality;
    public String profession;
    public String primarySpeciality;
    public String desiredShifts;
    public String otherPhone;
    public String dateOfBirth;
    public String workAuthorization;
    public String gender;
    public String currentCTC;
    public List<Object> licenses;
    public List<Object> certifications;
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();
    private Date date_added;
    private Date last_updated;
    private FileHandle fileHandle;
//    private String fullText;
    private boolean isActive;
    private String source;
    private String city;
    private String zip;
    private String state;
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


    private String summary;
    private List<Object> languages;
    private String linkedin;
    private String github;
    private String website;
    private List<Object> projects;
    private List<Object> experience;
    private List<Object> education;
    private Map<String, Object> resumeSegments = new LinkedHashMap<>();
    // Usually managed by backend, but if needed:
//    private ChangeLog changeLog;


}

enum Source{
    CHECKLIST,RTR,UPLOAD,LINKEDIN,DICE
}