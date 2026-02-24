package com.midas.consulting.util.testpojos;


import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"isAssigned",
"amId",
"tlId",
"finalUserAssignee",
"url",
"DurationType",
"Alias",
"ProviderJobID",
"CustomerID",
"SourceID",
"EndDate",
"SourceName",
"PostDate",
"ChangeDate",
"StatusString",
"Priority",
"Positions",
"StartDate",
"Duration",
"JobSpecialty",
"Degree",
"JobBoardDegree",
"JobBoardSpecialty",
"CleanDegree",
"CleanSpecialty",
"CleanShift",
"Category",
"Facility",
"Unit",
"Shift",
"WorkType",
"WorkLevel",
"BillRate",
"Association",
"GuaranteedHours",
"OnCallRate",
"Bonus",
"Local",
"Address",
"City",
"State",
"Zip",
"IsASAP",
"HotFl",
"ATSID",
"ProfitRank",
"MealStipendW",
"LodgingStipendW",
"TotalStipendH",
"PayPerH",
"GrossBillW",
"Title",
"OTRate",
"OTRules",
"ChargeRate",
"CallbackRate",
"HolidayRate",
"OnCallRateDescription",
"Orientation",
"ShiftRateDifferencial",
"Buyer",
"Coordinator",
"minBillRate",
"minPayRate",
"maxPayRate",
"GrossPayW",
"GSAMealW",
"GSALodginW",
"WinterPlanNeed",
"Licenses",
"MinExperienceRequired",
"Industry",
"StandardTitle",
"AbbreviatedTitle",
"Skills",
"CleanDegreeID",
"CleanSpecialtyID",
"jobURL",
"Hold_Fl",
"BRate",
"BMinRate",
"BOTRate",
"BHRate",
"BCRate",
"BCBRate",
"BOCRate",
"BOrRate",
"BPRate",
"BMinPRate",
"PositionRequirements",
"PermFee",
"OTRule",
"OTRate2",
"OTHours",
"OTHours2",
"ExternalVMSID",
"ExternalVMSName",
"ExternalMSPID",
"ExternalMSPName",
"FormattedStartDate",
"FormattedEndDate",
"ReferenceID",
"DurationWeeks",
"AutoOffer_Fl",
"CustomField1",
"CustomField2",
"CustomField3",
"CustomField4",
"CustomField6",
"CustomField7",
"CustomField8",
"CustomField9",
"CustomField10",
"CustomField11",
"CustomField12",
"CustomField13",
"CustomField14",
"CustomField15",
"CustomField16",
"CustomField17",
"CustomField18",
"CustomField19",
"CustomField20",
"assigned"
})
@Generated("jsonschema2pojo")
public class JobFeed {

@JsonProperty("isAssigned")
private Boolean isAssigned;
@JsonProperty("amId")
private String amId;
@JsonProperty("tlId")
private String tlId;
@JsonProperty("finalUserAssignee")
private String finalUserAssignee;
@JsonProperty("url")
private String url;
@JsonProperty("DurationType")
private String durationType;
@JsonProperty("Alias")
private String alias;
@JsonProperty("ProviderJobID")
private Integer providerJobID;
@JsonProperty("CustomerID")
private Integer customerID;
@JsonProperty("SourceID")
private String sourceID;
@JsonProperty("EndDate")
private String endDate;
@JsonProperty("SourceName")
private String sourceName;
@JsonProperty("PostDate")
private String postDate;
@JsonProperty("ChangeDate")
private String changeDate;
@JsonProperty("StatusString")
private String statusString;
@JsonProperty("Priority")
private String priority;
@JsonProperty("Positions")
private Integer positions;
@JsonProperty("StartDate")
private String startDate;
@JsonProperty("Duration")
private String duration;
@JsonProperty("JobSpecialty")
private String jobSpecialty;
@JsonProperty("Degree")
private String degree;
@JsonProperty("JobBoardDegree")
private String jobBoardDegree;
@JsonProperty("JobBoardSpecialty")
private String jobBoardSpecialty;
@JsonProperty("CleanDegree")
private String cleanDegree;
@JsonProperty("CleanSpecialty")
private String cleanSpecialty;
@JsonProperty("CleanShift")
private String cleanShift;
@JsonProperty("Category")
private String category;
@JsonProperty("Facility")
private String facility;
@JsonProperty("Unit")
private String unit;
@JsonProperty("Shift")
private String shift;
@JsonProperty("WorkType")
private String workType;
@JsonProperty("WorkLevel")
private String workLevel;
@JsonProperty("BillRate")
private Double billRate;
@JsonProperty("Association")
private String association;
@JsonProperty("GuaranteedHours")
private String guaranteedHours;
@JsonProperty("OnCallRate")
private Double onCallRate;
@JsonProperty("Bonus")
private Double bonus;
@JsonProperty("Local")
private Boolean local;
@JsonProperty("Address")
private String address;
@JsonProperty("City")
private String city;
@JsonProperty("State")
private String state;
@JsonProperty("Zip")
private String zip;
@JsonProperty("IsASAP")
private Boolean isASAP;
@JsonProperty("HotFl")
private Boolean hotFl;
@JsonProperty("ATSID")
private String atsid;
@JsonProperty("ProfitRank")
private Double profitRank;
@JsonProperty("MealStipendW")
private Double mealStipendW;
@JsonProperty("LodgingStipendW")
private Double lodgingStipendW;
@JsonProperty("TotalStipendH")
private Double totalStipendH;
@JsonProperty("PayPerH")
private Double payPerH;
@JsonProperty("GrossBillW")
private Double grossBillW;
@JsonProperty("Title")
private String title;
@JsonProperty("OTRate")
private String oTRate;
@JsonProperty("OTRules")
private String oTRules;
@JsonProperty("ChargeRate")
private String chargeRate;
@JsonProperty("CallbackRate")
private String callbackRate;
@JsonProperty("HolidayRate")
private String holidayRate;
@JsonProperty("OnCallRateDescription")
private String onCallRateDescription;
@JsonProperty("Orientation")
private String orientation;
@JsonProperty("ShiftRateDifferencial")
private String shiftRateDifferencial;
@JsonProperty("Buyer")
private String buyer;
@JsonProperty("Coordinator")
private String coordinator;
@JsonProperty("minBillRate")
private Double minBillRate;
@JsonProperty("minPayRate")
private Double minPayRate;
@JsonProperty("maxPayRate")
private Double maxPayRate;
@JsonProperty("GrossPayW")
private Double grossPayW;
@JsonProperty("GSAMealW")
private Double gSAMealW;
@JsonProperty("GSALodginW")
private Double gSALodginW;
@JsonProperty("WinterPlanNeed")
private String winterPlanNeed;
@JsonProperty("Licenses")
private String licenses;
@JsonProperty("MinExperienceRequired")
private String minExperienceRequired;
@JsonProperty("Industry")
private String industry;
@JsonProperty("StandardTitle")
private String standardTitle;
@JsonProperty("AbbreviatedTitle")
private String abbreviatedTitle;
@JsonProperty("Skills")
private String skills;
@JsonProperty("CleanDegreeID")
private Integer cleanDegreeID;
@JsonProperty("CleanSpecialtyID")
private Integer cleanSpecialtyID;
@JsonProperty("jobURL")
private String jobURL;
@JsonProperty("Hold_Fl")
private Boolean holdFl;
@JsonProperty("BRate")
private Double bRate;
@JsonProperty("BMinRate")
private Double bMinRate;
@JsonProperty("BOTRate")
private Double bOTRate;
@JsonProperty("BHRate")
private Double bHRate;
@JsonProperty("BCRate")
private Double bCRate;
@JsonProperty("BCBRate")
private Double bCBRate;
@JsonProperty("BOCRate")
private Double bOCRate;
@JsonProperty("BOrRate")
private Double bOrRate;
@JsonProperty("BPRate")
private Double bPRate;
@JsonProperty("BMinPRate")
private Double bMinPRate;
@JsonProperty("PositionRequirements")
private String positionRequirements;
@JsonProperty("PermFee")
private Double permFee;
@JsonProperty("OTRule")
private String oTRule;
@JsonProperty("OTRate2")
private Double oTRate2;
@JsonProperty("OTHours")
private Integer oTHours;
@JsonProperty("OTHours2")
private Integer oTHours2;
@JsonProperty("ExternalVMSID")
private String externalVMSID;
@JsonProperty("ExternalVMSName")
private String externalVMSName;
@JsonProperty("ExternalMSPID")
private String externalMSPID;
@JsonProperty("ExternalMSPName")
private String externalMSPName;
@JsonProperty("FormattedStartDate")
private String formattedStartDate;
@JsonProperty("FormattedEndDate")
private String formattedEndDate;
@JsonProperty("ReferenceID")
private String referenceID;
@JsonProperty("DurationWeeks")
private Integer durationWeeks;
@JsonProperty("AutoOffer_Fl")
private Boolean autoOfferFl;
@JsonProperty("CustomField1")
private String customField1;
@JsonProperty("CustomField2")
private String customField2;
@JsonProperty("CustomField3")
private String customField3;
@JsonProperty("CustomField4")
private String customField4;
@JsonProperty("CustomField6")
private String customField6;
@JsonProperty("CustomField7")
private String customField7;
@JsonProperty("CustomField8")
private String customField8;
@JsonProperty("CustomField9")
private String customField9;
@JsonProperty("CustomField10")
private String customField10;
@JsonProperty("CustomField11")
private String customField11;
@JsonProperty("CustomField12")
private String customField12;
@JsonProperty("CustomField13")
private String customField13;
@JsonProperty("CustomField14")
private String customField14;
@JsonProperty("CustomField15")
private String customField15;
@JsonProperty("CustomField16")
private String customField16;
@JsonProperty("CustomField17")
private String customField17;
@JsonProperty("CustomField18")
private String customField18;
@JsonProperty("CustomField19")
private String customField19;
@JsonProperty("CustomField20")
private String customField20;
@JsonProperty("assigned")
private Boolean assigned;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("isAssigned")
public Boolean getIsAssigned() {
return isAssigned;
}

@JsonProperty("isAssigned")
public void setIsAssigned(Boolean isAssigned) {
this.isAssigned = isAssigned;
}

@JsonProperty("amId")
public String getAmId() {
return amId;
}

@JsonProperty("amId")
public void setAmId(String amId) {
this.amId = amId;
}

@JsonProperty("tlId")
public String getTlId() {
return tlId;
}

@JsonProperty("tlId")
public void setTlId(String tlId) {
this.tlId = tlId;
}

@JsonProperty("finalUserAssignee")
public String getFinalUserAssignee() {
return finalUserAssignee;
}

@JsonProperty("finalUserAssignee")
public void setFinalUserAssignee(String finalUserAssignee) {
this.finalUserAssignee = finalUserAssignee;
}

@JsonProperty("url")
public String getUrl() {
return url;
}

@JsonProperty("url")
public void setUrl(String url) {
this.url = url;
}

@JsonProperty("DurationType")
public String getDurationType() {
return durationType;
}

@JsonProperty("DurationType")
public void setDurationType(String durationType) {
this.durationType = durationType;
}

@JsonProperty("Alias")
public String getAlias() {
return alias;
}

@JsonProperty("Alias")
public void setAlias(String alias) {
this.alias = alias;
}

@JsonProperty("ProviderJobID")
public Integer getProviderJobID() {
return providerJobID;
}

@JsonProperty("ProviderJobID")
public void setProviderJobID(Integer providerJobID) {
this.providerJobID = providerJobID;
}

@JsonProperty("CustomerID")
public Integer getCustomerID() {
return customerID;
}

@JsonProperty("CustomerID")
public void setCustomerID(Integer customerID) {
this.customerID = customerID;
}

@JsonProperty("SourceID")
public String getSourceID() {
return sourceID;
}

@JsonProperty("SourceID")
public void setSourceID(String sourceID) {
this.sourceID = sourceID;
}

@JsonProperty("EndDate")
public String getEndDate() {
return endDate;
}

@JsonProperty("EndDate")
public void setEndDate(String endDate) {
this.endDate = endDate;
}

@JsonProperty("SourceName")
public String getSourceName() {
return sourceName;
}

@JsonProperty("SourceName")
public void setSourceName(String sourceName) {
this.sourceName = sourceName;
}

@JsonProperty("PostDate")
public String getPostDate() {
return postDate;
}

@JsonProperty("PostDate")
public void setPostDate(String postDate) {
this.postDate = postDate;
}

@JsonProperty("ChangeDate")
public String getChangeDate() {
return changeDate;
}

@JsonProperty("ChangeDate")
public void setChangeDate(String changeDate) {
this.changeDate = changeDate;
}

@JsonProperty("StatusString")
public String getStatusString() {
return statusString;
}

@JsonProperty("StatusString")
public void setStatusString(String statusString) {
this.statusString = statusString;
}

@JsonProperty("Priority")
public String getPriority() {
return priority;
}

@JsonProperty("Priority")
public void setPriority(String priority) {
this.priority = priority;
}

@JsonProperty("Positions")
public Integer getPositions() {
return positions;
}

@JsonProperty("Positions")
public void setPositions(Integer positions) {
this.positions = positions;
}

@JsonProperty("StartDate")
public String getStartDate() {
return startDate;
}

@JsonProperty("StartDate")
public void setStartDate(String startDate) {
this.startDate = startDate;
}

@JsonProperty("Duration")
public String getDuration() {
return duration;
}

@JsonProperty("Duration")
public void setDuration(String duration) {
this.duration = duration;
}

@JsonProperty("JobSpecialty")
public String getJobSpecialty() {
return jobSpecialty;
}

@JsonProperty("JobSpecialty")
public void setJobSpecialty(String jobSpecialty) {
this.jobSpecialty = jobSpecialty;
}

@JsonProperty("Degree")
public String getDegree() {
return degree;
}

@JsonProperty("Degree")
public void setDegree(String degree) {
this.degree = degree;
}

@JsonProperty("JobBoardDegree")
public String getJobBoardDegree() {
return jobBoardDegree;
}

@JsonProperty("JobBoardDegree")
public void setJobBoardDegree(String jobBoardDegree) {
this.jobBoardDegree = jobBoardDegree;
}

@JsonProperty("JobBoardSpecialty")
public String getJobBoardSpecialty() {
return jobBoardSpecialty;
}

@JsonProperty("JobBoardSpecialty")
public void setJobBoardSpecialty(String jobBoardSpecialty) {
this.jobBoardSpecialty = jobBoardSpecialty;
}

@JsonProperty("CleanDegree")
public String getCleanDegree() {
return cleanDegree;
}

@JsonProperty("CleanDegree")
public void setCleanDegree(String cleanDegree) {
this.cleanDegree = cleanDegree;
}

@JsonProperty("CleanSpecialty")
public String getCleanSpecialty() {
return cleanSpecialty;
}

@JsonProperty("CleanSpecialty")
public void setCleanSpecialty(String cleanSpecialty) {
this.cleanSpecialty = cleanSpecialty;
}

@JsonProperty("CleanShift")
public String getCleanShift() {
return cleanShift;
}

@JsonProperty("CleanShift")
public void setCleanShift(String cleanShift) {
this.cleanShift = cleanShift;
}

@JsonProperty("Category")
public String getCategory() {
return category;
}

@JsonProperty("Category")
public void setCategory(String category) {
this.category = category;
}

@JsonProperty("Facility")
public String getFacility() {
return facility;
}

@JsonProperty("Facility")
public void setFacility(String facility) {
this.facility = facility;
}

@JsonProperty("Unit")
public String getUnit() {
return unit;
}

@JsonProperty("Unit")
public void setUnit(String unit) {
this.unit = unit;
}

@JsonProperty("Shift")
public String getShift() {
return shift;
}

@JsonProperty("Shift")
public void setShift(String shift) {
this.shift = shift;
}

@JsonProperty("WorkType")
public String getWorkType() {
return workType;
}

@JsonProperty("WorkType")
public void setWorkType(String workType) {
this.workType = workType;
}

@JsonProperty("WorkLevel")
public String getWorkLevel() {
return workLevel;
}

@JsonProperty("WorkLevel")
public void setWorkLevel(String workLevel) {
this.workLevel = workLevel;
}

@JsonProperty("BillRate")
public Double getBillRate() {
return billRate;
}

@JsonProperty("BillRate")
public void setBillRate(Double billRate) {
this.billRate = billRate;
}

@JsonProperty("Association")
public String getAssociation() {
return association;
}

@JsonProperty("Association")
public void setAssociation(String association) {
this.association = association;
}

@JsonProperty("GuaranteedHours")
public String getGuaranteedHours() {
return guaranteedHours;
}

@JsonProperty("GuaranteedHours")
public void setGuaranteedHours(String guaranteedHours) {
this.guaranteedHours = guaranteedHours;
}

@JsonProperty("OnCallRate")
public Double getOnCallRate() {
return onCallRate;
}

@JsonProperty("OnCallRate")
public void setOnCallRate(Double onCallRate) {
this.onCallRate = onCallRate;
}

@JsonProperty("Bonus")
public Double getBonus() {
return bonus;
}

@JsonProperty("Bonus")
public void setBonus(Double bonus) {
this.bonus = bonus;
}

@JsonProperty("Local")
public Boolean getLocal() {
return local;
}

@JsonProperty("Local")
public void setLocal(Boolean local) {
this.local = local;
}

@JsonProperty("Address")
public String getAddress() {
return address;
}

@JsonProperty("Address")
public void setAddress(String address) {
this.address = address;
}

@JsonProperty("City")
public String getCity() {
return city;
}

@JsonProperty("City")
public void setCity(String city) {
this.city = city;
}

@JsonProperty("State")
public String getState() {
return state;
}

@JsonProperty("State")
public void setState(String state) {
this.state = state;
}

@JsonProperty("Zip")
public String getZip() {
return zip;
}

@JsonProperty("Zip")
public void setZip(String zip) {
this.zip = zip;
}

@JsonProperty("IsASAP")
public Boolean getIsASAP() {
return isASAP;
}

@JsonProperty("IsASAP")
public void setIsASAP(Boolean isASAP) {
this.isASAP = isASAP;
}

@JsonProperty("HotFl")
public Boolean getHotFl() {
return hotFl;
}

@JsonProperty("HotFl")
public void setHotFl(Boolean hotFl) {
this.hotFl = hotFl;
}

@JsonProperty("ATSID")
public String getAtsid() {
return atsid;
}

@JsonProperty("ATSID")
public void setAtsid(String atsid) {
this.atsid = atsid;
}

@JsonProperty("ProfitRank")
public Double getProfitRank() {
return profitRank;
}

@JsonProperty("ProfitRank")
public void setProfitRank(Double profitRank) {
this.profitRank = profitRank;
}

@JsonProperty("MealStipendW")
public Double getMealStipendW() {
return mealStipendW;
}

@JsonProperty("MealStipendW")
public void setMealStipendW(Double mealStipendW) {
this.mealStipendW = mealStipendW;
}

@JsonProperty("LodgingStipendW")
public Double getLodgingStipendW() {
return lodgingStipendW;
}

@JsonProperty("LodgingStipendW")
public void setLodgingStipendW(Double lodgingStipendW) {
this.lodgingStipendW = lodgingStipendW;
}

@JsonProperty("TotalStipendH")
public Double getTotalStipendH() {
return totalStipendH;
}

@JsonProperty("TotalStipendH")
public void setTotalStipendH(Double totalStipendH) {
this.totalStipendH = totalStipendH;
}

@JsonProperty("PayPerH")
public Double getPayPerH() {
return payPerH;
}

@JsonProperty("PayPerH")
public void setPayPerH(Double payPerH) {
this.payPerH = payPerH;
}

@JsonProperty("GrossBillW")
public Double getGrossBillW() {
return grossBillW;
}

@JsonProperty("GrossBillW")
public void setGrossBillW(Double grossBillW) {
this.grossBillW = grossBillW;
}

@JsonProperty("Title")
public String getTitle() {
return title;
}

@JsonProperty("Title")
public void setTitle(String title) {
this.title = title;
}

@JsonProperty("OTRate")
public String getOTRate() {
return oTRate;
}

@JsonProperty("OTRate")
public void setOTRate(String oTRate) {
this.oTRate = oTRate;
}

@JsonProperty("OTRules")
public String getOTRules() {
return oTRules;
}

@JsonProperty("OTRules")
public void setOTRules(String oTRules) {
this.oTRules = oTRules;
}

@JsonProperty("ChargeRate")
public String getChargeRate() {
return chargeRate;
}

@JsonProperty("ChargeRate")
public void setChargeRate(String chargeRate) {
this.chargeRate = chargeRate;
}

@JsonProperty("CallbackRate")
public String getCallbackRate() {
return callbackRate;
}

@JsonProperty("CallbackRate")
public void setCallbackRate(String callbackRate) {
this.callbackRate = callbackRate;
}

@JsonProperty("HolidayRate")
public String getHolidayRate() {
return holidayRate;
}

@JsonProperty("HolidayRate")
public void setHolidayRate(String holidayRate) {
this.holidayRate = holidayRate;
}

@JsonProperty("OnCallRateDescription")
public String getOnCallRateDescription() {
return onCallRateDescription;
}

@JsonProperty("OnCallRateDescription")
public void setOnCallRateDescription(String onCallRateDescription) {
this.onCallRateDescription = onCallRateDescription;
}

@JsonProperty("Orientation")
public String getOrientation() {
return orientation;
}

@JsonProperty("Orientation")
public void setOrientation(String orientation) {
this.orientation = orientation;
}

@JsonProperty("ShiftRateDifferencial")
public String getShiftRateDifferencial() {
return shiftRateDifferencial;
}

@JsonProperty("ShiftRateDifferencial")
public void setShiftRateDifferencial(String shiftRateDifferencial) {
this.shiftRateDifferencial = shiftRateDifferencial;
}

@JsonProperty("Buyer")
public String getBuyer() {
return buyer;
}

@JsonProperty("Buyer")
public void setBuyer(String buyer) {
this.buyer = buyer;
}

@JsonProperty("Coordinator")
public String getCoordinator() {
return coordinator;
}

@JsonProperty("Coordinator")
public void setCoordinator(String coordinator) {
this.coordinator = coordinator;
}

@JsonProperty("minBillRate")
public Double getMinBillRate() {
return minBillRate;
}

@JsonProperty("minBillRate")
public void setMinBillRate(Double minBillRate) {
this.minBillRate = minBillRate;
}

@JsonProperty("minPayRate")
public Double getMinPayRate() {
return minPayRate;
}

@JsonProperty("minPayRate")
public void setMinPayRate(Double minPayRate) {
this.minPayRate = minPayRate;
}

@JsonProperty("maxPayRate")
public Double getMaxPayRate() {
return maxPayRate;
}

@JsonProperty("maxPayRate")
public void setMaxPayRate(Double maxPayRate) {
this.maxPayRate = maxPayRate;
}

@JsonProperty("GrossPayW")
public Double getGrossPayW() {
return grossPayW;
}

@JsonProperty("GrossPayW")
public void setGrossPayW(Double grossPayW) {
this.grossPayW = grossPayW;
}

@JsonProperty("GSAMealW")
public Double getGSAMealW() {
return gSAMealW;
}

@JsonProperty("GSAMealW")
public void setGSAMealW(Double gSAMealW) {
this.gSAMealW = gSAMealW;
}

@JsonProperty("GSALodginW")
public Double getGSALodginW() {
return gSALodginW;
}

@JsonProperty("GSALodginW")
public void setGSALodginW(Double gSALodginW) {
this.gSALodginW = gSALodginW;
}

@JsonProperty("WinterPlanNeed")
public String getWinterPlanNeed() {
return winterPlanNeed;
}

@JsonProperty("WinterPlanNeed")
public void setWinterPlanNeed(String winterPlanNeed) {
this.winterPlanNeed = winterPlanNeed;
}

@JsonProperty("Licenses")
public String getLicenses() {
return licenses;
}

@JsonProperty("Licenses")
public void setLicenses(String licenses) {
this.licenses = licenses;
}

@JsonProperty("MinExperienceRequired")
public String getMinExperienceRequired() {
return minExperienceRequired;
}

@JsonProperty("MinExperienceRequired")
public void setMinExperienceRequired(String minExperienceRequired) {
this.minExperienceRequired = minExperienceRequired;
}

@JsonProperty("Industry")
public String getIndustry() {
return industry;
}

@JsonProperty("Industry")
public void setIndustry(String industry) {
this.industry = industry;
}

@JsonProperty("StandardTitle")
public String getStandardTitle() {
return standardTitle;
}

@JsonProperty("StandardTitle")
public void setStandardTitle(String standardTitle) {
this.standardTitle = standardTitle;
}

@JsonProperty("AbbreviatedTitle")
public String getAbbreviatedTitle() {
return abbreviatedTitle;
}

@JsonProperty("AbbreviatedTitle")
public void setAbbreviatedTitle(String abbreviatedTitle) {
this.abbreviatedTitle = abbreviatedTitle;
}

@JsonProperty("Skills")
public String getSkills() {
return skills;
}

@JsonProperty("Skills")
public void setSkills(String skills) {
this.skills = skills;
}

@JsonProperty("CleanDegreeID")
public Integer getCleanDegreeID() {
return cleanDegreeID;
}

@JsonProperty("CleanDegreeID")
public void setCleanDegreeID(Integer cleanDegreeID) {
this.cleanDegreeID = cleanDegreeID;
}

@JsonProperty("CleanSpecialtyID")
public Integer getCleanSpecialtyID() {
return cleanSpecialtyID;
}

@JsonProperty("CleanSpecialtyID")
public void setCleanSpecialtyID(Integer cleanSpecialtyID) {
this.cleanSpecialtyID = cleanSpecialtyID;
}

@JsonProperty("jobURL")
public String getJobURL() {
return jobURL;
}

@JsonProperty("jobURL")
public void setJobURL(String jobURL) {
this.jobURL = jobURL;
}

@JsonProperty("Hold_Fl")
public Boolean getHoldFl() {
return holdFl;
}

@JsonProperty("Hold_Fl")
public void setHoldFl(Boolean holdFl) {
this.holdFl = holdFl;
}

@JsonProperty("BRate")
public Double getBRate() {
return bRate;
}

@JsonProperty("BRate")
public void setBRate(Double bRate) {
this.bRate = bRate;
}

@JsonProperty("BMinRate")
public Double getBMinRate() {
return bMinRate;
}

@JsonProperty("BMinRate")
public void setBMinRate(Double bMinRate) {
this.bMinRate = bMinRate;
}

@JsonProperty("BOTRate")
public Double getBOTRate() {
return bOTRate;
}

@JsonProperty("BOTRate")
public void setBOTRate(Double bOTRate) {
this.bOTRate = bOTRate;
}

@JsonProperty("BHRate")
public Double getBHRate() {
return bHRate;
}

@JsonProperty("BHRate")
public void setBHRate(Double bHRate) {
this.bHRate = bHRate;
}

@JsonProperty("BCRate")
public Double getBCRate() {
return bCRate;
}

@JsonProperty("BCRate")
public void setBCRate(Double bCRate) {
this.bCRate = bCRate;
}

@JsonProperty("BCBRate")
public Double getBCBRate() {
return bCBRate;
}

@JsonProperty("BCBRate")
public void setBCBRate(Double bCBRate) {
this.bCBRate = bCBRate;
}

@JsonProperty("BOCRate")
public Double getBOCRate() {
return bOCRate;
}

@JsonProperty("BOCRate")
public void setBOCRate(Double bOCRate) {
this.bOCRate = bOCRate;
}

@JsonProperty("BOrRate")
public Double getBOrRate() {
return bOrRate;
}

@JsonProperty("BOrRate")
public void setBOrRate(Double bOrRate) {
this.bOrRate = bOrRate;
}

@JsonProperty("BPRate")
public Double getBPRate() {
return bPRate;
}

@JsonProperty("BPRate")
public void setBPRate(Double bPRate) {
this.bPRate = bPRate;
}

@JsonProperty("BMinPRate")
public Double getBMinPRate() {
return bMinPRate;
}

@JsonProperty("BMinPRate")
public void setBMinPRate(Double bMinPRate) {
this.bMinPRate = bMinPRate;
}

@JsonProperty("PositionRequirements")
public String getPositionRequirements() {
return positionRequirements;
}

@JsonProperty("PositionRequirements")
public void setPositionRequirements(String positionRequirements) {
this.positionRequirements = positionRequirements;
}

@JsonProperty("PermFee")
public Double getPermFee() {
return permFee;
}

@JsonProperty("PermFee")
public void setPermFee(Double permFee) {
this.permFee = permFee;
}

@JsonProperty("OTRule")
public String getOTRule() {
return oTRule;
}

@JsonProperty("OTRule")
public void setOTRule(String oTRule) {
this.oTRule = oTRule;
}

@JsonProperty("OTRate2")
public Double getOTRate2() {
return oTRate2;
}

@JsonProperty("OTRate2")
public void setOTRate2(Double oTRate2) {
this.oTRate2 = oTRate2;
}

@JsonProperty("OTHours")
public Integer getOTHours() {
return oTHours;
}

@JsonProperty("OTHours")
public void setOTHours(Integer oTHours) {
this.oTHours = oTHours;
}

@JsonProperty("OTHours2")
public Integer getOTHours2() {
return oTHours2;
}

@JsonProperty("OTHours2")
public void setOTHours2(Integer oTHours2) {
this.oTHours2 = oTHours2;
}

@JsonProperty("ExternalVMSID")
public String getExternalVMSID() {
return externalVMSID;
}

@JsonProperty("ExternalVMSID")
public void setExternalVMSID(String externalVMSID) {
this.externalVMSID = externalVMSID;
}

@JsonProperty("ExternalVMSName")
public String getExternalVMSName() {
return externalVMSName;
}

@JsonProperty("ExternalVMSName")
public void setExternalVMSName(String externalVMSName) {
this.externalVMSName = externalVMSName;
}

@JsonProperty("ExternalMSPID")
public String getExternalMSPID() {
return externalMSPID;
}

@JsonProperty("ExternalMSPID")
public void setExternalMSPID(String externalMSPID) {
this.externalMSPID = externalMSPID;
}

@JsonProperty("ExternalMSPName")
public String getExternalMSPName() {
return externalMSPName;
}

@JsonProperty("ExternalMSPName")
public void setExternalMSPName(String externalMSPName) {
this.externalMSPName = externalMSPName;
}

@JsonProperty("FormattedStartDate")
public String getFormattedStartDate() {
return formattedStartDate;
}

@JsonProperty("FormattedStartDate")
public void setFormattedStartDate(String formattedStartDate) {
this.formattedStartDate = formattedStartDate;
}

@JsonProperty("FormattedEndDate")
public String getFormattedEndDate() {
return formattedEndDate;
}

@JsonProperty("FormattedEndDate")
public void setFormattedEndDate(String formattedEndDate) {
this.formattedEndDate = formattedEndDate;
}

@JsonProperty("ReferenceID")
public String getReferenceID() {
return referenceID;
}

@JsonProperty("ReferenceID")
public void setReferenceID(String referenceID) {
this.referenceID = referenceID;
}

@JsonProperty("DurationWeeks")
public Integer getDurationWeeks() {
return durationWeeks;
}

@JsonProperty("DurationWeeks")
public void setDurationWeeks(Integer durationWeeks) {
this.durationWeeks = durationWeeks;
}

@JsonProperty("AutoOffer_Fl")
public Boolean getAutoOfferFl() {
return autoOfferFl;
}

@JsonProperty("AutoOffer_Fl")
public void setAutoOfferFl(Boolean autoOfferFl) {
this.autoOfferFl = autoOfferFl;
}

@JsonProperty("CustomField1")
public String getCustomField1() {
return customField1;
}

@JsonProperty("CustomField1")
public void setCustomField1(String customField1) {
this.customField1 = customField1;
}

@JsonProperty("CustomField2")
public String getCustomField2() {
return customField2;
}

@JsonProperty("CustomField2")
public void setCustomField2(String customField2) {
this.customField2 = customField2;
}

@JsonProperty("CustomField3")
public String getCustomField3() {
return customField3;
}

@JsonProperty("CustomField3")
public void setCustomField3(String customField3) {
this.customField3 = customField3;
}

@JsonProperty("CustomField4")
public String getCustomField4() {
return customField4;
}

@JsonProperty("CustomField4")
public void setCustomField4(String customField4) {
this.customField4 = customField4;
}

@JsonProperty("CustomField6")
public String getCustomField6() {
return customField6;
}

@JsonProperty("CustomField6")
public void setCustomField6(String customField6) {
this.customField6 = customField6;
}

@JsonProperty("CustomField7")
public String getCustomField7() {
return customField7;
}

@JsonProperty("CustomField7")
public void setCustomField7(String customField7) {
this.customField7 = customField7;
}

@JsonProperty("CustomField8")
public String getCustomField8() {
return customField8;
}

@JsonProperty("CustomField8")
public void setCustomField8(String customField8) {
this.customField8 = customField8;
}

@JsonProperty("CustomField9")
public String getCustomField9() {
return customField9;
}

@JsonProperty("CustomField9")
public void setCustomField9(String customField9) {
this.customField9 = customField9;
}

@JsonProperty("CustomField10")
public String getCustomField10() {
return customField10;
}

@JsonProperty("CustomField10")
public void setCustomField10(String customField10) {
this.customField10 = customField10;
}

@JsonProperty("CustomField11")
public String getCustomField11() {
return customField11;
}

@JsonProperty("CustomField11")
public void setCustomField11(String customField11) {
this.customField11 = customField11;
}

@JsonProperty("CustomField12")
public String getCustomField12() {
return customField12;
}

@JsonProperty("CustomField12")
public void setCustomField12(String customField12) {
this.customField12 = customField12;
}

@JsonProperty("CustomField13")
public String getCustomField13() {
return customField13;
}

@JsonProperty("CustomField13")
public void setCustomField13(String customField13) {
this.customField13 = customField13;
}

@JsonProperty("CustomField14")
public String getCustomField14() {
return customField14;
}

@JsonProperty("CustomField14")
public void setCustomField14(String customField14) {
this.customField14 = customField14;
}

@JsonProperty("CustomField15")
public String getCustomField15() {
return customField15;
}

@JsonProperty("CustomField15")
public void setCustomField15(String customField15) {
this.customField15 = customField15;
}

@JsonProperty("CustomField16")
public String getCustomField16() {
return customField16;
}

@JsonProperty("CustomField16")
public void setCustomField16(String customField16) {
this.customField16 = customField16;
}

@JsonProperty("CustomField17")
public String getCustomField17() {
return customField17;
}

@JsonProperty("CustomField17")
public void setCustomField17(String customField17) {
this.customField17 = customField17;
}

@JsonProperty("CustomField18")
public String getCustomField18() {
return customField18;
}

@JsonProperty("CustomField18")
public void setCustomField18(String customField18) {
this.customField18 = customField18;
}

@JsonProperty("CustomField19")
public String getCustomField19() {
return customField19;
}

@JsonProperty("CustomField19")
public void setCustomField19(String customField19) {
this.customField19 = customField19;
}

@JsonProperty("CustomField20")
public String getCustomField20() {
return customField20;
}

@JsonProperty("CustomField20")
public void setCustomField20(String customField20) {
this.customField20 = customField20;
}

@JsonProperty("assigned")
public Boolean getAssigned() {
return assigned;
}

@JsonProperty("assigned")
public void setAssigned(Boolean assigned) {
this.assigned = assigned;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}