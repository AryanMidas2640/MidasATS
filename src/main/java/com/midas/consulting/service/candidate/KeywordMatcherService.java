//package com.midas.consulting.service.candidate;
//
//import com.midas.consulting.controller.v1.request.jobs.JobsFeeds;
//import com.midas.consulting.controller.v1.response.candidate.CandidateResponse;
//import com.midas.consulting.model.candidate.Candidate;
//import com.textkernel.tx.models.resume.skills.ResumeRawSkill;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class KeywordMatcherService {
//
//    ICandidateService iCandidateService;
//
//    @Autowired
//    public  KeywordMatcherService(ICandidateService iCandidateService){
//        this.iCandidateService =iCandidateService;
//    }
//
//
//    // Define weights for each criterion
//    private static final double SKILL_WEIGHT = 0.4;
//    private static final double TITLE_WEIGHT = 0.2;
//    private static final double TYPE_WEIGHT = 0.1;
//    private static final double DESCRIPTION_WEIGHT = 0.3;
//
//    public double calculateMatchScore(JobsFeeds job, Candidate candidate) {
//        double score = 0;
//
////        // Match Job Type
////        if (job.getJobType().equalsIgnoreCase(candidate.getPreferredJobType())) {
////            score += TYPE_WEIGHT * 100;
////        }
////
////        // Match Job Title
////        if (job.getTitle().equalsIgnoreCase(candidate.getParseResumeResponse().Value.ResumeData.J)) {
////            score += TITLE_WEIGHT * 100;
////        }
//
//        // Match Skills
//        List<String> jobSkills = extractSkillsFromDescription(job.getNote());
//        List<ResumeRawSkill> candidateSkills = null;//candidate.getParseResumeResponse().Value.ResumeData.Skills.Raw;
//        double skillMatchPercentage = calculateMatchPercentage(candidateSkills, jobSkills);
//        score += SKILL_WEIGHT * skillMatchPercentage;
//
//        // Match Job Description
////        double descriptionMatchPercentage = calculateDescriptionMatchPercentage(job.getNote(), candidate.getParseResumeResponse().Value.ResumeData.EmploymentHistory.ExperienceSummary.Description);
////        score += DESCRIPTION_WEIGHT * descriptionMatchPercentage;
//
//        return score;
//    }
//
//    private List<String> extractSkillsFromDescription(String description) {
//        // Implement skill extraction logic
//        return new ArrayList<>();
//    }
//
//    private double calculateMatchPercentage(List<ResumeRawSkill> candidateSkills, List<String> jobSkills) {
//        // Implement keyword matching logic
//        return 0;
//    }
//
//    private double calculateDescriptionMatchPercentage(String jobDescription, String candidateExperienceDescription) {
//        // Implement description matching logic
//        return 0;
//    }
//
//
//    // Function to calculate the match percentage of keywords in the text
//    public double calculateMatchPercentage(String text, List<String> keywords) {
//        int matchCount = 0;
//        for (String keyword : keywords) {
//            if (text.toLowerCase().contains(keyword.toLowerCase())) {
//                matchCount++;
//            }
//        }
//        return (matchCount / (double) keywords.size()) * 100;
//    }
//
//    public Map<String, Double> matchCandidates(JobsFeeds jobsFeeds) {
//        Map<String, Double> matchPercentages = new HashMap<>();
//        List<CandidateResponse> candidateResponses = iCandidateService.getAllCandidateNotOnProject();
//
//        // Extract keywords from the JobsFeeds object
//        List<String> jobKeywords = extractKeywordsFromJobsFeeds(jobsFeeds);
//
//        // Loop through each candidate response and calculate match percentage
//        for (CandidateResponse candidateResponse : candidateResponses) {
//            List<ResumeRawSkill> candidateSkills = candidateResponse.getParseResumeResponse().Value.ResumeData.Skills.Raw;
////            double matchPercentage = calculateMatchPercentage(String.join(" ", candidateSkills.stream().forEach(x->{
////                return x.Name;
////            });), jobKeywords);
//            double matchPercentage = calculateMatchPercentage(
//                    String.join(" ", candidateSkills.stream()
//                            .map(skill -> skill.Name) // Replace with appropriate method to get the skill name
//                            .toArray(String[]::new)),
//                    jobKeywords
//            );
//            matchPercentages.put(candidateResponse.getId(), matchPercentage);
//        }
//
//        return matchPercentages;
//    }
//    // Function to extract keywords from the JobsFeeds object
//    private List<String> extractKeywordsFromJobsFeeds(JobsFeeds jobsFeeds) {
//        List<String> keywords = new ArrayList<>();
//        // Assuming JobsFeeds contains a field for keywords; adjust this method as necessary
//        if (jobsFeeds.getNote()!=null){
//            for (String keyword : jobsFeeds.getNote().split(" ")) {
//                keywords.add(keyword);
//            }
//        }else {
//
//        }
//
//        // For example purposes, returning an empty list
//        return keywords;
//    }
//}