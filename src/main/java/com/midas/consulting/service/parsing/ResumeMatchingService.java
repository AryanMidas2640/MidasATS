package com.midas.consulting.service.parsing;

import com.midas.consulting.dto.ResumeMatchResponse;
import com.midas.consulting.util.PdfUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResumeMatchingService {

    public ResumeMatchResponse matchResume(
            MultipartFile file,
            String requirement,
            String candidateName,
            String jobTitle
    ) {

        String resumeText = PdfUtil.extractText(file).toLowerCase();
        String requirementText = requirement.toLowerCase();

        // Extract required skills (remove small words)
        List<String> requiredSkills = Arrays.stream(requirementText.split("\\s+"))
                .filter(word -> word.length() > 2)
                .distinct()
                .collect(Collectors.toList());

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();

        for (String skill : requiredSkills) {
            if (resumeText.contains(skill)) {
                matchedSkills.add(skill);
            } else {
                missingSkills.add(skill);
            }
        }

        return getResumeMatchResponse(
                requiredSkills.size(),
                matchedSkills,
                missingSkills,
                candidateName,
                jobTitle
        );
    }

    private static @NotNull ResumeMatchResponse getResumeMatchResponse(
            int totalRequiredSkills,
            List<String> matchedSkills,
            List<String> missingSkills,
            String candidateName,
            String jobTitle
    ) {

        int matchCount = matchedSkills.size();
        int score = 0;

        if (totalRequiredSkills > 0) {
            score = (matchCount * 100) / totalRequiredSkills;
        }

        ResumeMatchResponse response = new ResumeMatchResponse();

        response.setScore(score);
        response.setMatchedSkills(matchCount);
        response.setTotalRequiredSkills(totalRequiredSkills);
        response.setMatchedSkillList(matchedSkills);
        response.setMissingSkillList(missingSkills);
        response.setSuccess(score >= 60);
        response.setEvaluatedAt(LocalDateTime.now());

        // ✅ NEW FIELDS
        response.setCandidateName(candidateName);
        response.setJobTitle(jobTitle);
        response.setStatusCode(200);

        if (!missingSkills.isEmpty()) {
            response.setImprovementSuggestions(Collections.singletonList("Improve your resume by adding: " + String.join(", ", missingSkills)));
        }

        if (score >= 80)
            response.setMessage("Excellent Match");
        else if (score >= 60)
            response.setMessage("Good Match");
        else
            response.setMessage("Low Match");

        return response;
    }
}