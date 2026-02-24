package com.midas.consulting.dto;

import lombok.Data;

@Data
public class ResumeMatchResponse {

    private int score;
    private int matchedSkills;
    private int totalRequiredSkills;
    private String message;
}