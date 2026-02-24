//package com.midas.consulting.service.candidate;
//
//import com.midas.consulting.controller.v1.request.jobs.JobsFeeds;
//import com.midas.consulting.controller.v1.response.candidate.CandidateResponse;
//import com.textkernel.tx.models.resume.skills.ResumeV2Skills;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class CandidateSearchService {
//
//    @Autowired
//    private IMidasCandidateService candidateService;
//
//    // Define weights for each criterion
//    private static final double SPECIALTY_WEIGHT = 0.3;
//    private static final double DEGREE_WEIGHT = 0.2;
//    private static final double WORK_TYPE_WEIGHT = 0.1;
//    private static final double BILL_RATE_WEIGHT = 0.2;
//    private static final double SKILLS_WEIGHT = 0.2;
//
//    public List<CandidateResponse> searchCandidates(JobsFeeds jobPayload) {
//        List<CandidateResponse> candidates = candidateService.getAllCandidates();
//        Map<CandidateResponse, Double> matchScores = new HashMap<>();
//
//        for (CandidateResponse candidate : candidates) {
//            double matchScore = calculateMatchScore(jobPayload, candidate);
//            matchScores.put(candidate, matchScore);
//        }
//
//        return matchScores.entrySet().stream()
//                .sorted(Map.Entry.<CandidateResponse, Double>comparingByValue().reversed())
//                .map(Map.Entry::getKey)
//                .collect(Collectors.toList());
//    }
//
//    private double calculateMatchScore(JobsFeeds jobPayload, CandidateResponse candidate) {
//        double score = 0;
//
//        // Match Job Specialtycandidate.getJobSpecialty()
//        if (jobPayload.getJobSpecialty().equalsIgnoreCase("")) {
//            score += SPECIALTY_WEIGHT * 100;
//        }
//
//        // Match Degree candidate.getDegree()
//        if (jobPayload.getDegree().equalsIgnoreCase("")) {
//            score += DEGREE_WEIGHT * 100;
//        }
//
//        // Match Work Type candidate.getWorkType()
//        if (jobPayload.getWorkType().equalsIgnoreCase("")) {
//            score += WORK_TYPE_WEIGHT * 100;
//        }
//
//        // Match Bill Rate
////        if (candidate.getExpectedBillRate() <= jobPayload.getBillRate()) {
////            score += BILL_RATE_WEIGHT * 100;
////        }
//
//        // Match Skills
//        List<String> jobSkills = Arrays.asList(jobPayload.getSkills().split(","));
//        ResumeV2Skills candidateSkills = candidate.getParseResumeResponse().Value.ResumeData.Skills;
//        double skillMatchPercentage = calculateSkillMatchPercentage(candidateSkills, jobSkills);
//        score += SKILLS_WEIGHT * skillMatchPercentage;
//
//        return score;
//    }
//
//    private double calculateSkillMatchPercentage(ResumeV2Skills candidateSkills, List<String> jobSkills) {
//        int matchCount = 0;
//        for (String jobSkill : jobSkills) {
//            if (candidateSkills.Normalized.stream().anyMatch(x -> {
//                return x.RawSkills.contains(jobSkill.trim().toLowerCase());
//            })) {
//                matchCount++;
//            }
//        }
//        return (matchCount / (double) jobSkills.size()) * 100;
//    }
//}
//
//// Assuming JobPayload and CandidateResponse classes are defined similarly to the given payload and candidate structures
