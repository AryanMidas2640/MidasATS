package com.midas.consulting.controller.v1.response.parsing.textkernal;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class PersonalAttributes{
    @JsonProperty("Availability")
    public String availability;
    @JsonProperty("Birthplace") 
    public String birthplace;
    @JsonProperty("CurrentLocation") 
    public String currentLocation;
    @JsonProperty("CurrentSalary") 
    public CurrentSalary currentSalary;
    @JsonProperty("DateOfBirth") 
    public DateOfBirth dateOfBirth;
    @JsonProperty("DrivingLicense") 
    public String drivingLicense;
    @JsonProperty("FamilyComposition") 
    public String familyComposition;
    @JsonProperty("FathersName") 
    public String fathersName;
    @JsonProperty("Gender") 
    public String gender;
    @JsonProperty("HukouCity") 
    public String hukouCity;
    @JsonProperty("HukouArea") 
    public String hukouArea;
    @JsonProperty("MaritalStatus") 
    public String maritalStatus;
    @JsonProperty("MothersMaidenName") 
    public String mothersMaidenName;
    @JsonProperty("MotherTongue") 
    public String motherTongue;
    @JsonProperty("NationalIdentities") 
    public ArrayList<NationalIdentity> nationalIdentities;
    @JsonProperty("Nationality") 
    public String nationality;
    @JsonProperty("PassportNumber") 
    public String passportNumber;
    @JsonProperty("PreferredLocation") 
    public String preferredLocation;
    @JsonProperty("RequiredSalary") 
    public RequiredSalary requiredSalary;
    @JsonProperty("VisaStatus") 
    public String visaStatus;
    @JsonProperty("WillingToRelocate") 
    public String willingToRelocate;
}
