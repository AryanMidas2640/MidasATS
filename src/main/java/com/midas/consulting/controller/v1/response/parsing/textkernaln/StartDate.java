package com.midas.consulting.controller.v1.response.parsing.textkernaln;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Date;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartDate {

   @SerializedName("Date")
   Date Date;

   @SerializedName("IsCurrentDate")
   boolean IsCurrentDate;

   @SerializedName("FoundYear")
   boolean FoundYear;

   @SerializedName("FoundMonth")
   boolean FoundMonth;

   @SerializedName("FoundDay")
   boolean FoundDay;


    public void setDate(Date Date) {
        this.Date = Date;
    }
    public Date getDate() {
        return Date;
    }
    
    public void setIsCurrentDate(boolean IsCurrentDate) {
        this.IsCurrentDate = IsCurrentDate;
    }
    public boolean getIsCurrentDate() {
        return IsCurrentDate;
    }
    
    public void setFoundYear(boolean FoundYear) {
        this.FoundYear = FoundYear;
    }
    public boolean getFoundYear() {
        return FoundYear;
    }
    
    public void setFoundMonth(boolean FoundMonth) {
        this.FoundMonth = FoundMonth;
    }
    public boolean getFoundMonth() {
        return FoundMonth;
    }
    
    public void setFoundDay(boolean FoundDay) {
        this.FoundDay = FoundDay;
    }
    public boolean getFoundDay() {
        return FoundDay;
    }
    
}