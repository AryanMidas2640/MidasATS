package com.midas.consulting.model.candidate;

import lombok.Data;

@Data
public class Education{
    public String degree;
    public String institution;
    public String start_date;
    public String end_date;
    public String location;
}