package com.midas.consulting.controller.v1.request.candidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Education{
    public String degree;
    public String institution;
    public String start_date;
    public String end_date;
    public String location;
}