package com.midas.consulting.controller.v1.request.jobs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "isAssigned",
        "amId",
        "tlId",
        "finalUserAssignee",
        "url",
        "durationType",
        "alias",
        "providerJobID",
        "customerID",
        "sourceID",
        "endDate",
        "sourceName",
        "postDate",
        "changeDate",
        "statusString",
        "priority",
        "positions",
        "startDate",
        "duration",
        "jobSpecialty",
        "degree",
        "jobBoardDegree",
        "jobBoardSpecialty",
        "cleanDegree",
        "cleanSpecialty",
        "cleanShift",
        "category",
        "facility",
        "unit",
        "shift",
        "workType",
        "workLevel",
        "note",
        "billRate",
        "association",
        "guaranteedHours",
        "onCallRate",
        "bonus",
        "local",
        "address",
        "city",
        "state",
        "zip",
        "isASAP",
        "hotFl",
        "atsID",
        "profitRank",
        "mealStipendW",
        "lodgingStipendW",
        "totalStipendH",
        "payPerH",
        "grossBillW",
        "title",
        "otRate",
        "otRules",
        "chargeRate",
        "callbackRate",
        "holidayRate",
        "onCallRateDescription",
        "orientation",
        "shiftRateDifferential",
        "buyer",
        "coordinator",
        "minBillRate",
        "minPayRate",
        "maxPayRate",
        "grossPayW",
        "gsaMealW",
        "gsaLodgingW",
        "winterPlanNeed",
        "licenses",
        "minExperienceRequired",
        "industry",
        "standardTitle",
        "abbreviatedTitle",
        "skills",
        "cleanDegreeID",
        "cleanSpecialtyID",
        "jobURL",
        "hold_Fl",
        "family",
        "bRate",
        "bMinRate",
        "bOTRate",
        "bHRate",
        "bCRate",
        "bCBRate",
        "bOCRate",
        "bOrRate",
        "bPRate",
        "bMinPRate",
        "positionRequirements",
        "permFee",
        "otRule",
        "otRate2",
        "otHours",
        "otHours2",
        "externalVMSID",
        "externalVMSName",
        "externalMSPID",
        "externalMSPName",
        "formattedStartDate",
        "formattedEndDate",
        "referenceID",
        "durationWeeks",
        "autoOffer_Fl",
        "customField1",
        "customField2",
        "customField3",
        "customField4",
        "customField5",
        "customField6",
        "customField7",
        "customField8",
        "customField9",
        "customField10",
        "customField11",
        "customField12",
        "customField13",
        "customField14",
        "customField15",
        "customField16",
        "customField17",
        "customField18",
        "customField19",
        "customField20"
})
public class JobsFeeds {
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

    @JsonProperty("durationType")
    private String durationType;

    @JsonProperty("alias")
    private String alias;

    @JsonProperty("providerJobID")
    private String providerJobID;

    @JsonProperty("customerID")
    private String customerID;

    @JsonProperty("sourceID")
    private String sourceID;

    @JsonProperty("endDate")
    private String endDate;

    @JsonProperty("sourceName")
    private String sourceName;

    @JsonProperty("postDate")
    private String postDate;

    @JsonProperty("changeDate")
    private String changeDate;

    @JsonProperty("statusString")
    private String statusString;

    @JsonProperty("priority")
    private String priority;

    @JsonProperty("positions")
    private Integer positions;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("duration")
    private String duration;

    @JsonProperty("jobSpecialty")
    private String jobSpecialty;

    @JsonProperty("degree")
    private String degree;

    @JsonProperty("jobBoardDegree")
    private String jobBoardDegree;

    @JsonProperty("jobBoardSpecialty")
    private String jobBoardSpecialty;

    @JsonProperty("cleanDegree")
    private String cleanDegree;

    @JsonProperty("cleanSpecialty")
    private String cleanSpecialty;

    @JsonProperty("cleanShift")
    private String cleanShift;

    @JsonProperty("category")
    private String category;

    @JsonProperty("facility")
    private String facility;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("shift")
    private String shift;

    @JsonProperty("workType")
    private String workType;

    @JsonProperty("workLevel")
    private String workLevel;

    @JsonProperty("note")
    private String note;

    @JsonProperty("billRate")
    private String billRate;

    @JsonProperty("association")
    private String association;

    @JsonProperty("guaranteedHours")
    private String guaranteedHours;

    @JsonProperty("onCallRate")
    private String onCallRate;

    @JsonProperty("bonus")
    private String bonus;

    @JsonProperty("local")
    private String local;

    @JsonProperty("address")
    private String address;

    @JsonProperty("city")
    private String city;

    @JsonProperty("state")
    private String state;

    @JsonProperty("zip")
    private String zip;

    @JsonProperty("isASAP")
    private Boolean isASAP;

    @JsonProperty("hotFl")
    private Boolean hotFl;

    @JsonProperty("atsID")
    private String atsID;

    @JsonProperty("profitRank")
    private Integer profitRank;

    @JsonProperty("mealStipendW")
    private String mealStipendW;

    @JsonProperty("lodgingStipendW")
    private String lodgingStipendW;

    @JsonProperty("totalStipendH")
    private String totalStipendH;

    @JsonProperty("payPerH")
    private String payPerH;

    @JsonProperty("grossBillW")
    private String grossBillW;

    @JsonProperty("title")
    private String title;

    @JsonProperty("otRate")
    private String otRate;

    @JsonProperty("otRules")
    private String otRules;

    @JsonProperty("chargeRate")
    private String chargeRate;

    @JsonProperty("callbackRate")
    private String callbackRate;

    @JsonProperty("holidayRate")
    private String holidayRate;

    @JsonProperty("onCallRateDescription")
    private String onCallRateDescription;

    @JsonProperty("orientation")
    private String orientation;

    @JsonProperty("shiftRateDifferential")
    private String shiftRateDifferential;

    @JsonProperty("buyer")
    private String buyer;

    @JsonProperty("coordinator")
    private String coordinator;

    @JsonProperty("minBillRate")
    private String minBillRate;

    @JsonProperty("minPayRate")
    private String minPayRate;

    @JsonProperty("maxPayRate")
    private String maxPayRate;

    @JsonProperty("grossPayW")
    private String grossPayW;

    @JsonProperty("gsaMealW")
    private String gsaMealW;

    @JsonProperty("gsaLodgingW")
    private String gsaLodgingW;

    @JsonProperty("winterPlanNeed")
    private String winterPlanNeed;

    @JsonProperty("licenses")
    private String licenses;

    @JsonProperty("minExperienceRequired")
    private String minExperienceRequired;

    @JsonProperty("industry")
    private String industry;

    @JsonProperty("standardTitle")
    private String standardTitle;

    @JsonProperty("abbreviatedTitle")
    private String abbreviatedTitle;

    @JsonProperty("skills")
    private String skills;

    @JsonProperty("cleanDegreeID")
    private String cleanDegreeID;

    @JsonProperty("cleanSpecialtyID")
    private String cleanSpecialtyID;

    @JsonProperty("jobURL")
    private String jobURL;

    @JsonProperty("holdFl")
    private Boolean holdFl;

    @JsonProperty("family")
    private String family;

    @JsonProperty("bRate")
    private String bRate;

    @JsonProperty("bMinRate")
    private String bMinRate;

    @JsonProperty("bOTRate")
    private String bOTRate;

    @JsonProperty("bHRate")
    private String bHRate;

    @JsonProperty("bCRate")
    private String bCRate;

    @JsonProperty("bCBRate")
    private String bCBRate;

    @JsonProperty("bOCRate")
    private String bOCRate;

    @JsonProperty("bOrRate")
    private String bOrRate;

    @JsonProperty("bPRate")
    private String bPRate;

    @JsonProperty("bMinPRate")
    private String bMinPRate;

    @JsonProperty("positionRequirements")
    private String positionRequirements;

    @JsonProperty("permFee")
    private String permFee;

    @JsonProperty("otRule")
    private String otRule;

    @JsonProperty("otRate2")
    private String otRate2;

    @JsonProperty("otHours")
    private String otHours;

    @JsonProperty("otHours2")
    private String otHours2;

    @JsonProperty("externalVMSID")
    private String externalVMSID;

    @JsonProperty("externalVMSName")
    private String externalVMSName;

    @JsonProperty("externalMSPID")
    private String externalMSPID;

    @JsonProperty("externalMSPName")
    private String externalMSPName;

    @JsonProperty("formattedStartDate")
    private String formattedStartDate;

    @JsonProperty("formattedEndDate")
    private String formattedEndDate;

    @JsonProperty("referenceID")
    private String referenceID;

    @JsonProperty("durationWeeks")
    private String durationWeeks;

    @JsonProperty("autoOffer_Fl")
    private Boolean autoOffer_Fl;

    @JsonProperty("customField1")
    private String customField1;

    @JsonProperty("customField2")
    private String customField2;

    @JsonProperty("customField3")
    private String customField3;

    @JsonProperty("customField4")
    private String customField4;

    @JsonProperty("customField5")
    private String customField5;

    @JsonProperty("customField6")
    private String customField6;

    @JsonProperty("customField7")
    private String customField7;

    @JsonProperty("customField8")
    private String customField8;

    @JsonProperty("customField9")
    private String customField9;

    @JsonProperty("customField10")
    private String customField10;

    @JsonProperty("customField11")
    private String customField11;

    @JsonProperty("customField12")
    private String customField12;

    @JsonProperty("customField13")
    private String customField13;

    @JsonProperty("customField14")
    private String customField14;

    @JsonProperty("customField15")
    private String customField15;

    @JsonProperty("customField16")
    private String customField16;

    @JsonProperty("customField17")
    private String customField17;

    @JsonProperty("customField18")
    private String customField18;

    @JsonProperty("customField19")
    private String customField19;

    @JsonProperty("customField20")
    private String customField20;

    public Boolean getAssigned() {
        return isAssigned;
    }

    public void setAssigned(Boolean assigned) {
        isAssigned = assigned;
    }

    public String getAmId() {
        return amId;
    }

    public void setAmId(String amId) {
        this.amId = amId;
    }

    public String getTlId() {
        return tlId;
    }

    public void setTlId(String tlId) {
        this.tlId = tlId;
    }

    public String getFinalUserAssignee() {
        return finalUserAssignee;
    }

    public void setFinalUserAssignee(String finalUserAssignee) {
        this.finalUserAssignee = finalUserAssignee;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDurationType() {
        return durationType;
    }

    public void setDurationType(String durationType) {
        this.durationType = durationType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getProviderJobID() {
        return providerJobID;
    }

    public void setProviderJobID(String providerJobID) {
        this.providerJobID = providerJobID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(String changeDate) {
        this.changeDate = changeDate;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Integer getPositions() {
        return positions;
    }

    public void setPositions(Integer positions) {
        this.positions = positions;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getJobSpecialty() {
        return jobSpecialty;
    }

    public void setJobSpecialty(String jobSpecialty) {
        this.jobSpecialty = jobSpecialty;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getJobBoardDegree() {
        return jobBoardDegree;
    }

    public void setJobBoardDegree(String jobBoardDegree) {
        this.jobBoardDegree = jobBoardDegree;
    }

    public String getJobBoardSpecialty() {
        return jobBoardSpecialty;
    }

    public void setJobBoardSpecialty(String jobBoardSpecialty) {
        this.jobBoardSpecialty = jobBoardSpecialty;
    }

    public String getCleanDegree() {
        return cleanDegree;
    }

    public void setCleanDegree(String cleanDegree) {
        this.cleanDegree = cleanDegree;
    }

    public String getCleanSpecialty() {
        return cleanSpecialty;
    }

    public void setCleanSpecialty(String cleanSpecialty) {
        this.cleanSpecialty = cleanSpecialty;
    }

    public String getCleanShift() {
        return cleanShift;
    }

    public void setCleanShift(String cleanShift) {
        this.cleanShift = cleanShift;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFacility() {
        return facility;
    }

    public void setFacility(String facility) {
        this.facility = facility;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getWorkLevel() {
        return workLevel;
    }

    public void setWorkLevel(String workLevel) {
        this.workLevel = workLevel;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getBillRate() {
        return billRate;
    }

    public void setBillRate(String billRate) {
        this.billRate = billRate;
    }

    public String getAssociation() {
        return association;
    }

    public void setAssociation(String association) {
        this.association = association;
    }

    public String getGuaranteedHours() {
        return guaranteedHours;
    }

    public void setGuaranteedHours(String guaranteedHours) {
        this.guaranteedHours = guaranteedHours;
    }

    public String getOnCallRate() {
        return onCallRate;
    }

    public void setOnCallRate(String onCallRate) {
        this.onCallRate = onCallRate;
    }

    public String getBonus() {
        return bonus;
    }

    public void setBonus(String bonus) {
        this.bonus = bonus;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public Boolean getASAP() {
        return isASAP;
    }

    public void setASAP(Boolean ASAP) {
        isASAP = ASAP;
    }

    public Boolean getHotFl() {
        return hotFl;
    }

    public void setHotFl(Boolean hotFl) {
        this.hotFl = hotFl;
    }

    public String getAtsID() {
        return atsID;
    }

    public void setAtsID(String atsID) {
        this.atsID = atsID;
    }

    public Integer getProfitRank() {
        return profitRank;
    }

    public void setProfitRank(Integer profitRank) {
        this.profitRank = profitRank;
    }

    public String getMealStipendW() {
        return mealStipendW;
    }

    public void setMealStipendW(String mealStipendW) {
        this.mealStipendW = mealStipendW;
    }

    public String getLodgingStipendW() {
        return lodgingStipendW;
    }

    public void setLodgingStipendW(String lodgingStipendW) {
        this.lodgingStipendW = lodgingStipendW;
    }

    public String getTotalStipendH() {
        return totalStipendH;
    }

    public void setTotalStipendH(String totalStipendH) {
        this.totalStipendH = totalStipendH;
    }

    public String getPayPerH() {
        return payPerH;
    }

    public void setPayPerH(String payPerH) {
        this.payPerH = payPerH;
    }

    public String getGrossBillW() {
        return grossBillW;
    }

    public void setGrossBillW(String grossBillW) {
        this.grossBillW = grossBillW;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOtRate() {
        return otRate;
    }

    public void setOtRate(String otRate) {
        this.otRate = otRate;
    }

    public String getOtRules() {
        return otRules;
    }

    public void setOtRules(String otRules) {
        this.otRules = otRules;
    }

    public String getChargeRate() {
        return chargeRate;
    }

    public void setChargeRate(String chargeRate) {
        this.chargeRate = chargeRate;
    }

    public String getCallbackRate() {
        return callbackRate;
    }

    public void setCallbackRate(String callbackRate) {
        this.callbackRate = callbackRate;
    }

    public String getHolidayRate() {
        return holidayRate;
    }

    public void setHolidayRate(String holidayRate) {
        this.holidayRate = holidayRate;
    }

    public String getOnCallRateDescription() {
        return onCallRateDescription;
    }

    public void setOnCallRateDescription(String onCallRateDescription) {
        this.onCallRateDescription = onCallRateDescription;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }

    public String getShiftRateDifferential() {
        return shiftRateDifferential;
    }

    public void setShiftRateDifferential(String shiftRateDifferential) {
        this.shiftRateDifferential = shiftRateDifferential;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(String coordinator) {
        this.coordinator = coordinator;
    }

    public String getMinBillRate() {
        return minBillRate;
    }

    public void setMinBillRate(String minBillRate) {
        this.minBillRate = minBillRate;
    }

    public String getMinPayRate() {
        return minPayRate;
    }

    public void setMinPayRate(String minPayRate) {
        this.minPayRate = minPayRate;
    }

    public String getMaxPayRate() {
        return maxPayRate;
    }

    public void setMaxPayRate(String maxPayRate) {
        this.maxPayRate = maxPayRate;
    }

    public String getGrossPayW() {
        return grossPayW;
    }

    public void setGrossPayW(String grossPayW) {
        this.grossPayW = grossPayW;
    }

    public String getGsaMealW() {
        return gsaMealW;
    }

    public void setGsaMealW(String gsaMealW) {
        this.gsaMealW = gsaMealW;
    }

    public String getGsaLodgingW() {
        return gsaLodgingW;
    }

    public void setGsaLodgingW(String gsaLodgingW) {
        this.gsaLodgingW = gsaLodgingW;
    }

    public String getWinterPlanNeed() {
        return winterPlanNeed;
    }

    public void setWinterPlanNeed(String winterPlanNeed) {
        this.winterPlanNeed = winterPlanNeed;
    }

    public String getLicenses() {
        return licenses;
    }

    public void setLicenses(String licenses) {
        this.licenses = licenses;
    }

    public String getMinExperienceRequired() {
        return minExperienceRequired;
    }

    public void setMinExperienceRequired(String minExperienceRequired) {
        this.minExperienceRequired = minExperienceRequired;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getStandardTitle() {
        return standardTitle;
    }

    public void setStandardTitle(String standardTitle) {
        this.standardTitle = standardTitle;
    }

    public String getAbbreviatedTitle() {
        return abbreviatedTitle;
    }

    public void setAbbreviatedTitle(String abbreviatedTitle) {
        this.abbreviatedTitle = abbreviatedTitle;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getCleanDegreeID() {
        return cleanDegreeID;
    }

    public void setCleanDegreeID(String cleanDegreeID) {
        this.cleanDegreeID = cleanDegreeID;
    }

    public String getCleanSpecialtyID() {
        return cleanSpecialtyID;
    }

    public void setCleanSpecialtyID(String cleanSpecialtyID) {
        this.cleanSpecialtyID = cleanSpecialtyID;
    }

    public String getJobURL() {
        return jobURL;
    }

    public void setJobURL(String jobURL) {
        this.jobURL = jobURL;
    }

    public Boolean getHoldFl() {
        return holdFl;
    }

    public void setHoldFl(Boolean holdFl) {
        this.holdFl = holdFl;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getbRate() {
        return bRate;
    }

    public void setbRate(String bRate) {
        this.bRate = bRate;
    }

    public String getbMinRate() {
        return bMinRate;
    }

    public void setbMinRate(String bMinRate) {
        this.bMinRate = bMinRate;
    }

    public String getbOTRate() {
        return bOTRate;
    }

    public void setbOTRate(String bOTRate) {
        this.bOTRate = bOTRate;
    }

    public String getbHRate() {
        return bHRate;
    }

    public void setbHRate(String bHRate) {
        this.bHRate = bHRate;
    }

    public String getbCRate() {
        return bCRate;
    }

    public void setbCRate(String bCRate) {
        this.bCRate = bCRate;
    }

    public String getbCBRate() {
        return bCBRate;
    }

    public void setbCBRate(String bCBRate) {
        this.bCBRate = bCBRate;
    }

    public String getbOCRate() {
        return bOCRate;
    }

    public void setbOCRate(String bOCRate) {
        this.bOCRate = bOCRate;
    }

    public String getbOrRate() {
        return bOrRate;
    }

    public void setbOrRate(String bOrRate) {
        this.bOrRate = bOrRate;
    }

    public String getbPRate() {
        return bPRate;
    }

    public void setbPRate(String bPRate) {
        this.bPRate = bPRate;
    }

    public String getbMinPRate() {
        return bMinPRate;
    }

    public void setbMinPRate(String bMinPRate) {
        this.bMinPRate = bMinPRate;
    }

    public String getPositionRequirements() {
        return positionRequirements;
    }

    public void setPositionRequirements(String positionRequirements) {
        this.positionRequirements = positionRequirements;
    }

    public String getPermFee() {
        return permFee;
    }

    public void setPermFee(String permFee) {
        this.permFee = permFee;
    }

    public String getOtRule() {
        return otRule;
    }

    public void setOtRule(String otRule) {
        this.otRule = otRule;
    }

    public String getOtRate2() {
        return otRate2;
    }

    public void setOtRate2(String otRate2) {
        this.otRate2 = otRate2;
    }

    public String getOtHours() {
        return otHours;
    }

    public void setOtHours(String otHours) {
        this.otHours = otHours;
    }

    public String getOtHours2() {
        return otHours2;
    }

    public void setOtHours2(String otHours2) {
        this.otHours2 = otHours2;
    }

    public String getExternalVMSID() {
        return externalVMSID;
    }

    public void setExternalVMSID(String externalVMSID) {
        this.externalVMSID = externalVMSID;
    }

    public String getExternalVMSName() {
        return externalVMSName;
    }

    public void setExternalVMSName(String externalVMSName) {
        this.externalVMSName = externalVMSName;
    }

    public String getExternalMSPID() {
        return externalMSPID;
    }

    public void setExternalMSPID(String externalMSPID) {
        this.externalMSPID = externalMSPID;
    }

    public String getExternalMSPName() {
        return externalMSPName;
    }

    public void setExternalMSPName(String externalMSPName) {
        this.externalMSPName = externalMSPName;
    }

    public String getFormattedStartDate() {
        return formattedStartDate;
    }

    public void setFormattedStartDate(String formattedStartDate) {
        this.formattedStartDate = formattedStartDate;
    }

    public String getFormattedEndDate() {
        return formattedEndDate;
    }

    public void setFormattedEndDate(String formattedEndDate) {
        this.formattedEndDate = formattedEndDate;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }

    public String getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(String durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public Boolean getAutoOffer_Fl() {
        return autoOffer_Fl;
    }

    public void setAutoOffer_Fl(Boolean autoOffer_Fl) {
        this.autoOffer_Fl = autoOffer_Fl;
    }

    public String getCustomField1() {
        return customField1;
    }

    public void setCustomField1(String customField1) {
        this.customField1 = customField1;
    }

    public String getCustomField2() {
        return customField2;
    }

    public void setCustomField2(String customField2) {
        this.customField2 = customField2;
    }

    public String getCustomField3() {
        return customField3;
    }

    public void setCustomField3(String customField3) {
        this.customField3 = customField3;
    }

    public String getCustomField4() {
        return customField4;
    }

    public void setCustomField4(String customField4) {
        this.customField4 = customField4;
    }

    public String getCustomField5() {
        return customField5;
    }

    public void setCustomField5(String customField5) {
        this.customField5 = customField5;
    }

    public String getCustomField6() {
        return customField6;
    }

    public void setCustomField6(String customField6) {
        this.customField6 = customField6;
    }

    public String getCustomField7() {
        return customField7;
    }

    public void setCustomField7(String customField7) {
        this.customField7 = customField7;
    }

    public String getCustomField8() {
        return customField8;
    }

    public void setCustomField8(String customField8) {
        this.customField8 = customField8;
    }

    public String getCustomField9() {
        return customField9;
    }

    public void setCustomField9(String customField9) {
        this.customField9 = customField9;
    }

    public String getCustomField10() {
        return customField10;
    }

    public void setCustomField10(String customField10) {
        this.customField10 = customField10;
    }

    public String getCustomField11() {
        return customField11;
    }

    public void setCustomField11(String customField11) {
        this.customField11 = customField11;
    }

    public String getCustomField12() {
        return customField12;
    }

    public void setCustomField12(String customField12) {
        this.customField12 = customField12;
    }

    public String getCustomField13() {
        return customField13;
    }

    public void setCustomField13(String customField13) {
        this.customField13 = customField13;
    }

    public String getCustomField14() {
        return customField14;
    }

    public void setCustomField14(String customField14) {
        this.customField14 = customField14;
    }

    public String getCustomField15() {
        return customField15;
    }

    public void setCustomField15(String customField15) {
        this.customField15 = customField15;
    }

    public String getCustomField16() {
        return customField16;
    }

    public void setCustomField16(String customField16) {
        this.customField16 = customField16;
    }

    public String getCustomField17() {
        return customField17;
    }

    public void setCustomField17(String customField17) {
        this.customField17 = customField17;
    }

    public String getCustomField18() {
        return customField18;
    }

    public void setCustomField18(String customField18) {
        this.customField18 = customField18;
    }

    public String getCustomField19() {
        return customField19;
    }

    public void setCustomField19(String customField19) {
        this.customField19 = customField19;
    }

    public String getCustomField20() {
        return customField20;
    }

    public void setCustomField20(String customField20) {
        this.customField20 = customField20;
    }

    // Getters and Setters
}
