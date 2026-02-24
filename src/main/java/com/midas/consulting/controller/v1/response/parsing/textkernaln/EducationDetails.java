package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EducationDetails {

   @SerializedName("Id")
   Date Id;

   @SerializedName("Text")
   String Text;

   @SerializedName("SchoolName")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.SchoolName SchoolName;

   @SerializedName("SchoolType")
   String SchoolType;

   @SerializedName("Location")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.Location Location;

   @SerializedName("Degree")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.Degree Degree;

   @SerializedName("Majors")
   List<String> Majors;

   @SerializedName("LastEducationDate")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.LastEducationDate LastEducationDate;

   @SerializedName("StartDate")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.StartDate StartDate;

   @SerializedName("EndDate")
   com.midas.consulting.controller.v1.response.parsing.textkernaln.EndDate EndDate;


    public void setId(Date Id) {
        this.Id = Id;
    }
    public Date getId() {
        return Id;
    }
    
    public void setText(String Text) {
        this.Text = Text;
    }
    public String getText() {
        return Text;
    }
    
    public void setSchoolName(SchoolName SchoolName) {
        this.SchoolName = SchoolName;
    }
    public SchoolName getSchoolName() {
        return SchoolName;
    }
    
    public void setSchoolType(String SchoolType) {
        this.SchoolType = SchoolType;
    }
    public String getSchoolType() {
        return SchoolType;
    }
    
    public void setLocation(Location Location) {
        this.Location = Location;
    }
    public Location getLocation() {
        return Location;
    }
    
    public void setDegree(com.midas.consulting.controller.v1.response.parsing.textkernaln.Degree Degree) {
        this.Degree = Degree;
    }
    public com.midas.consulting.controller.v1.response.parsing.textkernaln.Degree getDegree() {
        return Degree;
    }
    
    public void setMajors(List<String> Majors) {
        this.Majors = Majors;
    }
    public List<String> getMajors() {
        return Majors;
    }
    
    public void setLastEducationDate(LastEducationDate LastEducationDate) {
        this.LastEducationDate = LastEducationDate;
    }
    public LastEducationDate getLastEducationDate() {
        return LastEducationDate;
    }
    
    public void setStartDate(StartDate StartDate) {
        this.StartDate = StartDate;
    }
    public StartDate getStartDate() {
        return StartDate;
    }
    
    public void setEndDate(EndDate EndDate) {
        this.EndDate = EndDate;
    }
    public EndDate getEndDate() {
        return EndDate;
    }
    
}