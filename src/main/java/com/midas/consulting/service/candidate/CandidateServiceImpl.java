//package com.midas.consulting.service.candidate;
//
//import com.midas.consulting.controller.v1.request.candidate.CreateCandidateRequest;
//import com.midas.consulting.controller.v1.response.candidate.CandidateResponse;
//import com.midas.consulting.exception.MidasCustomException;
//import com.midas.consulting.model.candidate.Candidate;
//import com.midas.consulting.model.candidate.CandidateMidas;
//import com.midas.consulting.repository.candidate.CandidateRepository;
//import com.midas.consulting.service.UserService;
//import com.midas.consulting.util.FileUtils;
//import com.textkernel.tx.models.resume.skills.ResumeV2Skills;
//import org.modelmapper.ModelMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.List;
//import java.util.Optional;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//@Service
//public class CandidateServiceImpl implements  ICandidateService {
//
//
//    ModelMapper modelMapper = new ModelMapper();
//
//    @Autowired
//    private  CandidateRepository candidateRepository;
//
//    @Autowired
//    private UserService userService;
//
//    /**
//     * @param createCandidateRequest
//     * @return
//     */
//    @Override
//    public CandidateResponse addCandidate(CreateCandidateRequest createCandidateRequest) throws MidasCustomException.ParsingException {
//        Candidate candidate = modelMapper.map(createCandidateRequest, Candidate.class);
//        Candidate candidateSaved =
//                candidateRepository.save(candidate);
//        return   modelMapper.map(candidateSaved, CandidateResponse.class);
//    }
//
//    private CreateCandidateRequest getEmailPhonePopulatedTextKernal(CreateCandidateRequest createCandidateRequest) {
//
//        return  createCandidateRequest;
//    }
//
//    public Optional<Candidate> findByEmailOrPhoneNumber(String email, String phoneNumber) {
//        return candidateRepository.findByEmailOrPhone(email, phoneNumber);
//    }
//
////
////    public boolean compareCandidates(CreateCandidateRequest createCandidateRequest) throws MidasCustomException.ParsingException {
////        if (createCandidateRequest.getResumeParserData() != null) {
////            Optional<Candidate> candidate = findByEmailOrPhoneNumber(createCandidateRequest.getEmail(), createCandidateRequest.getPhone());
////            if (candidate.isPresent()) {
////                return true; // Match found
//////                        throw new MidasCustomException.DuplicateEntityException("Candidates were in "+ candidate.get().getId());
////            }
////        } else {
////            throw new MidasCustomException.ParsingException("There must be some issue with parsing API not getting email or phone to check");
////        }
////        return false; // No match found
////    }
//
//    /**
//     * @param createCandidateRequest
//     * @return
//     */
//    @Override
//    public CandidateResponse updateCandidate(CreateCandidateRequest createCandidateRequest) throws Exception {
//        Optional<Candidate> candidateOptional = candidateRepository.findById(createCandidateRequest.getId());
//
//        if (candidateOptional.isPresent()) {
//            Candidate candidate = candidateOptional.get();
//            // Update candidate fields with values from createCandidateRequest
//            candidate.setId(createCandidateRequest.getId());
//           if ( candidate.getParseResumeResponse().Value.ResumeData.ContactInformation.EmailAddresses.isEmpty()){
//
//                candidate.setEmail(candidate.getEmail());
//            }
//            candidate.setPhone(candidate.getParseResumeResponse().Value.ResumeData.ContactInformation.Telephones.stream().distinct().findFirst().get().Normalized);
//            candidate.setStatus(createCandidateRequest.getStatus());
//            candidate.setParseResumeResponse(createCandidateRequest.getParseResumeResponse());
//            Candidate updatedCandidate = candidateRepository.save(candidate);
//            // Map the updated candidate entity to CandidateResponse
//            return modelMapper.map(updatedCandidate, CandidateResponse.class);
//        } else {
//            throw new Exception("CandidateRequest not found with id: " + createCandidateRequest.getId());
//        }
//    }
//
//    /**
//     * @param id
//     * @return
//     */
//    @Override
//    public CandidateResponse deleteCandidate(String id) throws Exception {
//        Optional<Candidate> candidateOptional = candidateRepository.findById(id);
//
//        if (candidateOptional.isPresent()) {
//            Candidate candidate = candidateOptional.get();
//            candidateRepository.delete(candidate);
//            return modelMapper.map(candidate, CandidateResponse.class);
//        } else {
//            throw new Exception("CandidateRequest not found with id: " + id);
//        }
//    }
//    /**
//     * @return
//     */
//    @Override
//    public List<CandidateResponse> getAllCandidates() {
//        List<Candidate> candidates = candidateRepository.findAll();
//        return candidates.stream()
//                .map(candidate -> modelMapper.map(candidate, CandidateResponse.class))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * @param id
//     * @return
//     */
//    @Override
//    public Optional<Candidate> getCandidateById(String id) {
//        Optional<Candidate> candidateOptional = candidateRepository.findById(id);
//        // Map the candidate entity to CandidateResponse if present
//        return candidateOptional;
//    }
//
//    /**
//     * @param email
//     * @return
//     */
//    @Override
//    public Optional<CandidateResponse> getCandidateByEmail(String email) {
//        return Optional.empty();
//    }
//
//    /**
//     * @param includeSkills
//     * @param excludeSkills
//     * @param emailRegex
//     * @param phoneRegex
//     * @param cityRegex
//     * @param stateRegex
//     * @return
//     */
//    @Override
//    public List<Candidate> searchCandidates(List<String> includeSkills, List<String> excludeSkills, String emailRegex, String phoneRegex, String cityRegex, String stateRegex) {
//        return null;
//    }
//
//    /**
//     * @return
//     */
//    @Override
//    public List<CandidateResponse> getAllCandidateNotOnProject() {
//        List<Candidate> candidateOptional = candidateRepository.findAll();
//        // Map the candidate entity to CandidateResponse if present
//        return candidateOptional.stream().map(candidate -> modelMapper.map(candidate, CandidateResponse.class)).collect(Collectors.toList());
//    }
//
//    /**
//     * @param filePath
//     * @return
//     */
//    @Override
//    public boolean getCandidateByEmailFromDoc(Path filePath) throws IOException {
//        String docText = "";
//        String email="";
//        if (isPDF(String.valueOf(filePath))) {
//            docText = FileUtils.convertPdfToText(String.valueOf(filePath));
//            email = FileUtils.extractEmail(docText);
//        } else if (isWord(String.valueOf(filePath))) {
//            docText = FileUtils.convertWordToText(String.valueOf(filePath));
//            email = FileUtils.extractEmail(docText);
//        } else {
//            System.out.println("The file type is unknown.");
//            return false;
//        }
//        Optional<CandidateMidas> candidate= candidateRepository.findByEmail(email.split(",")[0]);
//        if (candidate.isPresent()){
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * @param includeSkills
//     * @param excludeSkills
//     * @return
//     */
//
//
//
//    /**
////     * @param candidateSearchRequest
//     * @return
//     */
////    @Override
////    public  List<CandidateResponse> searchAllCandidates(CandidateSearchRequest candidateSearchRequest) {
//////        ModelMapper mapper = new ModelMapper();
////        List<Candidate> candidates = candidateRepository.searchCandidates(candidateSearchRequest);
////        return candidates.stream().map(candidate -> {
//////            CandidateResponse response = new CandidateResponse();
////               return   modelMapper.map(candidate,CandidateResponse.class);
//////            return response;
////        }).collect(Collectors.toList());
////    }
//
//    public List<CandidateMidas> searchCandidates(
//                                            String emailRegex, String phoneRegex
//                                           ) {
//
//
////        if (includeSkills == null || includeSkills.isEmpty()) {
////            includedCandidates = candidateRepository.findCandidatesByIncludedSkillsAndRegex(
////                  emailRegex, phoneRegex);
////        } else {
////            includedCandidates = candidateRepository.findCandidatesByIncludedSkillsAndRegex(
////                    includeSkills, emailRegex, phoneRegex, cityRegex, stateRegex);
////        }
////
////        if (excludeSkills == null || excludeSkills.isEmpty()) {
////            return includedCandidates; // No exclusion criteria, return included results directly
////        }
//
//    return candidateRepository.findCandidatesByIncludedSkillsAndRegex(
//                 emailRegex, phoneRegex);
//
////      return excludedCandidates.stream()
////                .collect(Collectors.toList());
//
////        return includedCandidates.stream()
////                .filter(candidate -> !excludedCandidateIds.contains(candidate.getId()))
////                .collect(Collectors.toList());
//    }
////    public List<Candidate> searchCandidates(List<String> includeSkills, List<String> excludeSkills) {
////        List<Candidate> includedCandidates;
////        List<Candidate> excludedCandidates;
////        String includeRegexPattern = "";
////        String excludeRegexPattern="";
////        if (includeSkills!=null){
////includeRegexPattern    = buildRegexPattern(includeSkills);
////}
////       if (excludeSkills!=null){
////        excludeRegexPattern= buildRegexPattern(excludeSkills);
////       }
////
////
////        if (includeSkills == null || includeSkills.isEmpty()) {
////            includedCandidates = candidateRepository.findAll();
////        } else {
////            includedCandidates = candidateRepository.findCandidatesByIncludedSkills(includeSkills,includeRegexPattern);
////        }
////
////        if (excludeSkills == null || excludeSkills.isEmpty()) {
////            return includedCandidates; // No exclusion criteria, return included results directly
////        }
////
////        excludedCandidates = candidateRepository.findCandidatesByIncludedSkills(excludeSkills,excludeRegexPattern);
////
////        List<String> excludedCandidateIds = excludedCandidates.stream()
////                .map(Candidate::getId)
////                .collect(Collectors.toList());
////
////        return includedCandidates.stream()
////                .filter(candidate -> !excludedCandidateIds.contains(candidate.getId()))
////                .collect(Collectors.toList());
////    }
//
//
//    ///  Latest
////
////    public List<Candidate> searchCandidates(List<String> includeSkills, List<String> excludeSkills) {
////        List<Candidate> includedCandidates;
////        List<Candidate> excludedCandidates = Collections.emptyList();
////        String includeRegexPattern = "";
////        String excludeRegexPattern = "";
////
////        if (includeSkills != null && !includeSkills.isEmpty()) {
////            includeRegexPattern = buildRegexPattern(includeSkills);
////            includedCandidates = candidateRepository.findCandidatesByIncludedSkillsR(includeRegexPattern);
////        } else {
////            includedCandidates = candidateRepository.findAll();
////        }
////
////        if (excludeSkills != null && !excludeSkills.isEmpty()) {
////            excludeRegexPattern = buildRegexPattern(excludeSkills);
////            excludedCandidates = candidateRepository.findCandidatesByExcludedSkillsR(excludeRegexPattern);
////        }
////
////        if (excludedCandidates.isEmpty()) {
////            return includedCandidates; // No exclusion criteria, return included results directly
////        }
////
////        List<String> excludedCandidateIds = excludedCandidates.stream()
////                .map(Candidate::getId)
////                .collect(Collectors.toList());
////
////        return includedCandidates.stream()
////                .filter(candidate -> !excludedCandidateIds.contains(candidate.getId()))
////                .collect(Collectors.toList());
////    }
////
//
//
//    public List<Candidate> searchCandidates(List<String> includeSkills, List<String> excludeSkills) {
//        List<Candidate> candidates;
//
//        boolean hasIncludeSkills = includeSkills != null && !includeSkills.isEmpty();
//        boolean hasExcludeSkills = excludeSkills != null && !excludeSkills.isEmpty();
//
//        if (hasIncludeSkills && hasExcludeSkills) {
//            String includeRegexPattern = buildRegexPattern(includeSkills);
//            String excludeRegexPattern = buildRegexPattern(excludeSkills);
//            candidates = candidateRepository.findCandidatesBySkillsIncludeAndExclude(includeRegexPattern, excludeRegexPattern);
//
//
//        } else if (hasIncludeSkills) {
//            String includeRegexPattern = buildRegexPattern(includeSkills);
//            candidates = candidateRepository.findCandidatesByIncludedSkillsR(includeRegexPattern);
//        } else if (hasExcludeSkills) {
//            String excludeRegexPattern = buildRegexPattern(excludeSkills);
//            candidates = candidateRepository.findCandidatesByExcludedSkillsR(excludeRegexPattern);
//        } else {
//            candidates = candidateRepository.findAll();
//        }
//
//        return candidates;
//    }
//
//
//    private String buildRegexPattern(List<String> skills) {
//        return skills.stream()
//                .map(skill -> Pattern.quote(skill.trim()))
//                .collect(Collectors.joining("|"));
//    }
//
//    @Override
//    public List<Candidate> searchAcrossAllFields(String searchText) {
//        return candidateRepository.searchAcrossAllFields(searchText);
//    }
//
////    public List<Candidate> searchCandidates(List<String> includeSkills, List<String> excludeSkills) {
////        List<Candidate> includedCandidates;
////        List<Candidate> excludedCandidates = Collections.emptyList();
////        String includeRegexPattern = "";
////        String excludeRegexPattern = "";
////
////        if (includeSkills != null && !includeSkills.isEmpty()) {
////            includeRegexPattern = buildRegexPattern(includeSkills);
////            includedCandidates = candidateRepository.findCandidatesByIncludedSkills(includeSkills, includeRegexPattern);
////        } else {
////            includedCandidates = candidateRepository.findAll();
////        }
////
////        if (excludeSkills != null && !excludeSkills.isEmpty()) {
////            excludeRegexPattern = buildRegexPattern(excludeSkills);
////            excludedCandidates = candidateRepository.findCandidatesByExcludedSkills(excludeSkills, excludeRegexPattern);
////        }
////
////        if (excludedCandidates.isEmpty()) {
////            return includedCandidates; // No exclusion criteria, return included results directly
////        }
////
////        List<String> excludedCandidateIds = excludedCandidates.stream()
////                .map(Candidate::getId)
////                .collect(Collectors.toList());
////
////        return includedCandidates.stream()
////                .filter(candidate -> !excludedCandidateIds.contains(candidate.getId()))
////                .collect(Collectors.toList());
////    }
////
//////    private String buildRegexPattern(List<String> skills) {
//////        return skills.stream()
//////                .map(skill -> "\\b" + skill.replaceAll("\\W", "") + "\\b")
//////                .collect(Collectors.joining("|"));
//////    }
////
//////    private String buildRegexPattern(List<String> skills) {
//////        return skills.stream()
//////                .map(skill -> "\\b" + Pattern.quote(skill.trim()) + "\\b")
//////                .collect(Collectors.joining("|"));
//////    }
////
////    private String buildRegexPattern(List<String> skills) {
////        return skills.stream()
////                .map(skill -> Pattern.quote(skill.trim()))
////                .collect(Collectors.joining("|"));
////    }
//
//    public List<Candidate> searchCandidates(String city, String email, String phone, String state,
//                                            List<ResumeV2Skills> includeSkills, List<ResumeV2Skills> excludeSkills) {
//        return candidateRepository.searchCandidates(city, email, phone, state, includeSkills, excludeSkills);
//    }
//
//    public static boolean isPDF(String filePath) {
//        return filePath.toLowerCase().endsWith(".pdf");
//    }
//
//    public static boolean isWord(String filePath) {
//        return (filePath.toLowerCase().endsWith(".docx") || filePath.toLowerCase().endsWith(".doc")) ;
//    }
//
//
//}
