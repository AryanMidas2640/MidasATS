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
public class Position{
    @JsonProperty("Id")
    public String id;
    @JsonProperty("Employer") 
    public Employer employer;
    @JsonProperty("RelatedToByDates") 
    public ArrayList<String> relatedToByDates;
    @JsonProperty("RelatedToByCompanyName") 
    public ArrayList<String> relatedToByCompanyName;
    @JsonProperty("IsSelfEmployed") 
    public boolean isSelfEmployed;
    @JsonProperty("IsCurrent") 
    public boolean isCurrent;
    @JsonProperty("JobTitle") 
    public JobTitle jobTitle;
    @JsonProperty("StartDate") 
    public StartDate startDate;
    @JsonProperty("EndDate") 
    public EndDate endDate;
    @JsonProperty("NumberEmployeesSupervised") 
    public NumberEmployeesSupervised numberEmployeesSupervised;
    @JsonProperty("JobType") 
    public String jobType;
    @JsonProperty("TaxonomyName") 
    public String taxonomyName;
    @JsonProperty("SubTaxonomyName") 
    public String subTaxonomyName;
    @JsonProperty("JobLevel") 
    public String jobLevel;
    @JsonProperty("TaxonomyPercentage") 
    public String taxonomyPercentage;
    @JsonProperty("Description") 
    public String description;
    @JsonProperty("Bullets") 
    public ArrayList<Bullet> bullets;
    @JsonProperty("NormalizedProfession") 
    public NormalizedProfession normalizedProfession;
}
