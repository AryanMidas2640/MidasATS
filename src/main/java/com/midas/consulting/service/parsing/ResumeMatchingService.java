package com.midas.consulting.service.parsing;

import com.midas.consulting.dto.ResumeMatchResponse;
import com.midas.consulting.util.PdfUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeMatchingService {

    public ResumeMatchResponse matchResume(MultipartFile file, String requirement) {

        String resumeText = PdfUtil.extractText(file).toLowerCase();
        String requirementText = requirement.toLowerCase();

        // Split requirement words and remove small words
        List<String> requiredSkills = Arrays.stream(requirementText.split("\\s+"))
                .filter(word -> word.length() > 2)
                .distinct()
                .collect(Collectors.toList());

        int matchCount = 0;

        for (String skill : requiredSkills) {
            if (resumeText.contains(skill)) {
                matchCount++;
            }
        }

        int score = 0;
        if (!requiredSkills.isEmpty()) {
            score = (matchCount * 100) / requiredSkills.size();
        }

        ResumeMatchResponse response = new ResumeMatchResponse();
        response.setScore(score);
        response.setMatchedSkills(matchCount);
        response.setTotalRequiredSkills(requiredSkills.size());

        if (score >= 80) response.setMessage("Excellent Match");
        else if (score >= 60) response.setMessage("Good Match");
        else response.setMessage("Low Match");

        return response;
    }
}