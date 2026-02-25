package com.midas.consulting.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ResumeMatchResponse {

    private int score;
    private int matchedSkills;
    private int totalRequiredSkills;
    private String message;


    // Detailed Skill Breakdown
    private List<String> matchedSkillList;
    private List<String> missingSkillList;

    // Metadata
    private String candidateName;
    private String jobTitle;
    private LocalDateTime evaluatedAt;

    // Optional Improvement Suggestions
    private List<String> improvementSuggestions;

    // Status Info
    private boolean success;
    private int statusCode;


}