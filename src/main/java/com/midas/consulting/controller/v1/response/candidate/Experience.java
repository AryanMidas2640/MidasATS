package com.midas.consulting.controller.v1.response.candidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class Experience{
    public String job_title;
    public String company;
    public String start_date;
    public String end_date;
    public ArrayList<String> responsibilities;
    public String location;
}