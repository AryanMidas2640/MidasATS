package com.midas.consulting.service.candidate;

import com.midas.consulting.controller.v1.request.candidate.CreateCandidateRequestMidas;
import com.midas.consulting.controller.v1.response.candidate.CandidateChangeLogResponse;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.candidate.CandidateMidas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public interface IMidasCandidateService {
    Page<CandidateMidas> searchCandidateResumesPaged(
            List<String> plusKeywords,
            List<String> minusKeywords,
            List<String> orPlusKeywords,
            Pageable pageable) ;

    List<CandidateMidas> searchAcrossAllFieldsSafelyExact(String searchText);
    List<CandidateMidas> searchCandidateResumes(List<String> plusKeywords, List<String> minusKeywords, List<String> orPlusKeywords);
    List<CandidateMidas> findCandidatesBySkills(List<String> includeSkills, List<String> excludeSkills, List<String> orSkills);
    List<CandidateMidas> searchAcrossAllFields(String searchText);
    List<CandidateMidas> searchAcrossAllFieldsN(String searchText);
    Page<CandidateMidas> searchCandidateResumesCityStatePaged(String booleanExp, Pageable pageable);
    Page<CandidateMidas> searchCandidateResumesCityStatePaged(
            List<String> city, List<String> state, List<String> zip, String booleanExpression, Pageable pageable) ;
    CandidateMidas addCandidate(CreateCandidateRequestMidas createCandidateRequest) throws MidasCustomException.ParsingException;
    CandidateMidas updateCandidate(CreateCandidateRequestMidas createCandidateRequest) throws Exception;
    CandidateMidas deleteCandidate(String id) throws Exception;
    List<CandidateMidas> getAllCandidates();

     Page<CandidateMidas> getAllCandidates(Pageable pageable)
             ;
    Optional<CandidateMidas> getCandidateById(String id);
    Optional<CandidateMidas> getCandidateByEmail(String email);
    List<CandidateMidas> searchCandidates(List<String> includeSkills, List<String> excludeSkills,
                                     String emailRegex, String phoneRegex,
                                     String cityRegex, String stateRegex);






    List<CandidateMidas> findCandidates(Map<String, List<String>> andCriteria, Map<String, List<String>> orCriteria, Map<String, List<String>> notCriteria) ;



    List<CandidateMidas> getAllCandidateNotOnProject();

    boolean getCandidateByEmailFromDoc(Path filePath) throws IOException;

    String getCandidateEmailFromDoc(Path filePath) throws IOException;

//    Object searchAllCandidates(CandidateSearchRequest candidateSearchRequest);
//    List<CandidateMidas>     searchCandidates(List<String >includeSkills,List<String> excludeSkills);
//    List<CandidateMidas> searchCandidates(String city, String email, String phone, String state,
//                                     List<ResumeV2Skills>  includeSkills, List<ResumeV2Skills> excludeSkills);
    List<CandidateMidas> searchCandidates( String email, String phone);

    Set<String> getDistinctSkills();


    // Method to get autocomplete suggestions based on partial input
     Set<String> getSkillSuggestions(String query) ;
    // Example query using custom MongoDB logic and pagination
    @Query("{ '$and': ["
            + "{'resumeKeywords': { $all: ?0 }}, " // Match all plusKeywords
            + "{'resumeKeywords': { $nin: ?1 }}, " // Exclude minusKeywords
            + "{'resumeKeywords': { $in: ?2 }}" // Match any orPlusKeywords
            + "] }")
    Page<CandidateMidas> findCandidatesByKeywords(
            List<String> plusKeywords,
            List<String> minusKeywords,
            List<String> orPlusKeywords,
            Pageable pageable);

    @Query("{ '$and': ["
            + "{'resumeKeywords': { $all: ?0 }}, " // Match all plusKeywords
            + "{'resumeKeywords': { $nin: ?1 }}, " // Exclude minusKeywords
            + "{'resumeKeywords': { $in: ?2 }}" // Match any orPlusKeywords
            + "] }")
    Page<CandidateMidas> findCandidatesBySkills(List<String> andCriteria, List<String> orCriteria, List<String> notCriteria, Pageable pageable);

    //    @Override
//    public List<CandidateMidas> searchCandidatesByNormalizedPhone(String searchText) {
////        MongoTemplate mongoTemplate=mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
//
//        // Step 1: Clean searchText for phone number matching
////        String cleanedPhone = searchText.replaceAll("[^0-9]", "");
//        String cleanedPhone = searchText.replaceAll("[^\\d]", "");
//        // Step 2: Create a query object
//        Query query = new Query();
//
//        // Step 3: Determine the type of searchText and build the query accordingly
//        if (isValidEmail(searchText)) {
//            String escapedSearchText = Pattern.quote(searchText);
//            query.addCriteria(Criteria.where("email").regex(".*" + escapedSearchText + ".*", "i"));
//        } else if (!cleanedPhone.isEmpty()) {
//            query.addCriteria(
//                    Criteria.where("fullText").regex(".*"+searchText+".*", "i")  // Formatted phone number (case insensitive)
//            );
//            List<CandidateMidas> candidateMidas=   mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class, "candidateMidas");
//            if(candidateMidas.size()==0){
//                query= new Query();
//                query.addCriteria(Criteria.where("phone").is(cleanedPhone));
//                candidateMidas=   mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class, "candidateMidas");
//                return  candidateMidas;
//            }
//        } else {
//            query.addCriteria(Criteria.where("name").regex(searchText, "i"));
//        }
//        List<CandidateMidas> candidateMidas=   mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class, "candidateMidas");
//        return candidateMidas;
//    }


    List<CandidateMidas> searchCandidatesByNormalizedPhone(String searchText, int page, int size);

    List<CandidateMidas> searchCandidatesByNormalizedPhone(String searchText);

    List<CandidateMidas> getChangeLogsByUserIds(Map<String, List<String>> options);

    public List<CandidateChangeLogResponse> getCandidateMidasByChangeLogs(List<String> userIds);
    List<CandidateChangeLogResponse> getCandidatesAndChangeLogs(List<String> userIds);

    List<CandidateChangeLogResponse> getCandidatesAndChangeLogs(List<String> userIds, Date fromDate, Date toDate);

    CandidateMidas addCandidate(CandidateMidas candidateMidas);

    Optional<CandidateMidas> getCandidateByPhone(String formattedPhone);

    Page<CandidateMidas> getCandidatesWithMissingPhone(Pageable pageable);

    CandidateMidas updateCandidatePhone(CandidateMidas candidate);
    Page<CandidateMidas> getCandidatesWithMissingSpecificFields(List<String> fields, Pageable pageable);

    MidasCandidateServiceImpl.SearchResult<CandidateMidas> searchCandidatesWithCount(String q, int page, int size);
}
