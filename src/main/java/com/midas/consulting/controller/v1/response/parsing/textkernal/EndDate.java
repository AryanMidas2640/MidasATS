package com.midas.consulting.controller.v1.response.parsing.textkernal;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class EndDate{
    @JsonProperty("Date")
    public String date;
    @JsonProperty("IsCurrentDate") 
    public boolean isCurrentDate;
    @JsonProperty("HasValue") 
    public boolean hasValue;
    @JsonProperty("FoundYear") 
    public boolean foundYear;
    @JsonProperty("FoundMonth") 
    public boolean foundMonth;
    @JsonProperty("FoundDay") 
    public boolean foundDay;
}