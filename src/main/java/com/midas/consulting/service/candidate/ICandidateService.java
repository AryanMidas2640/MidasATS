package com.midas.consulting.service.candidate;

import com.midas.consulting.controller.v1.request.candidate.CreateCandidateRequest;
import com.midas.consulting.controller.v1.response.candidate.CandidateResponse;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.candidate.Candidate;
import com.textkernel.tx.models.resume.skills.ResumeV2Skills;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ICandidateService {
    List<Candidate> searchAcrossAllFields(String searchText);
    CandidateResponse addCandidate(CreateCandidateRequest createCandidateRequest) throws MidasCustomException.ParsingException;
    CandidateResponse updateCandidate(CreateCandidateRequest createCandidateRequest) throws Exception;
    CandidateResponse deleteCandidate(String id) throws Exception;
    List<CandidateResponse> getAllCandidates();
    Optional<Candidate> getCandidateById(String id);
    Optional<CandidateResponse> getCandidateByEmail(String email);
    List<Candidate> searchCandidates(List<String> includeSkills, List<String> excludeSkills,
                                     String emailRegex, String phoneRegex,
                                     String cityRegex, String stateRegex);

    List<CandidateResponse> getAllCandidateNotOnProject();

    boolean getCandidateByEmailFromDoc(Path filePath) throws IOException;

//    Object searchAllCandidates(CandidateSearchRequest candidateSearchRequest);
    List<Candidate>     searchCandidates(List<String >includeSkills,List<String> excludeSkills);
    List<Candidate> searchCandidates(String city, String email, String phone, String state,
                                     List<ResumeV2Skills>  includeSkills, List<ResumeV2Skills> excludeSkills);
    List<Candidate> searchCandidates( String email, String phone);
}
