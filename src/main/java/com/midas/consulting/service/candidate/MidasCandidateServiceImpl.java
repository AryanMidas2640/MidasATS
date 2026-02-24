package com.midas.consulting.service.candidate;

//import com.midas.consulting.config.database.MultiTenantmongoTemplateProvider;
// Required imports

import com.github.benmanes.caffeine.cache.Cache;
import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.candidate.CreateCandidateRequestMidas;
import com.midas.consulting.controller.v1.response.candidate.CandidateChangeLogResponse;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.mapper.candidate.CandidateMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.model.candidate.CandidateMidas;
import com.midas.consulting.model.hrms.ChangeLog;
import com.midas.consulting.model.user.User;
import com.midas.consulting.service.ActivityService;
import com.midas.consulting.service.UserService;
import com.midas.consulting.util.BooleanExpressionParser;
import com.midas.consulting.util.FileUtils;
import com.mongodb.DBRef;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MidasCandidateServiceImpl implements  IMidasCandidateService {

@Autowired
    private MongoTemplateProvider mongoTemplateProvider;
    @Autowired
//    private MongoTemplate mongoTemplate;

    ModelMapper modelMapper = new ModelMapper();

//    @Autowired
//    private  CandidateRepository candidateRepository;
//
    @Autowired
    ActivityService activityService;
    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(MidasCandidateServiceImpl.class);
    @Autowired
    private UserService userService;




    // Method to retrieve tenant ID from the header
    private String getTenantIdFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-Tenant");
        }
        throw new IllegalStateException("Tenant ID not found in the request header");
    }



    @Override
    public CandidateMidas addCandidate(CreateCandidateRequestMidas createCandidateRequest) throws MidasCustomException.ParsingException {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader()); // Create a MongoTemplate for the specific tenant
        CandidateMidas candidate = modelMapper.map(createCandidateRequest, CandidateMidas.class);
        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeDateTime(new Date());
        changeLog.setUserNotes("System Added for first time");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> list= authentication.getAuthorities();

        UserDto user  =userService.getUserByEmail((String)authentication.getPrincipal());

        changeLog.setUser(UserMapper.toUser(user));
        ChangeLog changeLogSaved=mongoTemplateProvider   .getMongoTemplate().save(changeLog);
        candidate.setChangeLog(changeLogSaved);
        candidate.setActive(true);

        CandidateMidas candidateSaved = mongoTemplateProvider.getMongoTemplate().save(candidate); // Save using tenant-specific MongoTemplate
        return candidateSaved; // Return the saved candidate
    }

    @Override
    public Page<CandidateMidas> getCandidatesWithMissingPhone(Pageable pageable) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Criteria criteria = new Criteria().orOperator(
                Criteria.where("phone").is(null),
                Criteria.where("phone").is(""),
                Criteria.where("phone").regex("^\\s*$") // Only whitespace
        );

        Query query = new Query(criteria);

        long total = mongoTemplate.count(query, CandidateMidas.class);

        query.with(pageable);
        List<CandidateMidas> candidates = mongoTemplate.find(query, CandidateMidas.class);

        return new PageImpl<>(candidates, pageable, total);
    }

    @Override
    public Optional<CandidateMidas> getCandidateByPhone(String phone) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = new Query(Criteria.where("phone").is(phone));
        CandidateMidas candidate = mongoTemplate.findOne(query, CandidateMidas.class);

        return Optional.ofNullable(candidate);
    }

    @Override
    public CandidateMidas updateCandidatePhone(CandidateMidas candidate) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        // Update the last_updated timestamp
        candidate.setLast_updated(new Date());

        return mongoTemplate.save(candidate);
    }

    public Optional<CandidateMidas> findByEmailOrPhoneNumber(String email, String phoneNumber) {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader()); // Create a MongoTemplate for the specific tenant

        // Build the query using Spring Data MongoDB's Criteria and Query classes
        Query query = Query.query(
                Criteria.where("email").is(email)
                        .orOperator(Criteria.where("phone").is(phoneNumber))
        );

        return Optional.ofNullable(mongoTemplateProvider.getMongoTemplate().findOne(query, CandidateMidas.class)); // Use findOne for single result
    }


    @Override
    public CandidateMidas updateCandidate(CreateCandidateRequestMidas createCandidateRequest) throws Exception {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
        CandidateMidas candidate = mongoTemplateProvider.getMongoTemplate().findById(createCandidateRequest.getId(), CandidateMidas.class);

        if (candidate != null) {
            // Update candidate fields with values from createCandidateRequest
            candidate = CandidateMapper.toCandidate(createCandidateRequest);
            ChangeLog changeLog = new ChangeLog();
            changeLog.setChangeDateTime(new Date());
            changeLog.setUserNotes("System Added for first time");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Collection<? extends GrantedAuthority> list= authentication.getAuthorities();

            UserDto user  =userService.getUserByEmail((String)authentication.getPrincipal());
            changeLog.setUser(UserMapper.toUser(user));
            ChangeLog changeLogSaved=mongoTemplateProvider.getMongoTemplate().save(changeLog);
            candidate.setChangeLog(changeLogSaved);
            candidate.setActive(true);
            mongoTemplateProvider.getMongoTemplate().save(candidate); // Save the updated candidate
            return candidate;
        } else {
            throw new Exception("CandidateRequest not found with id: " + createCandidateRequest.getId());
        }
    }

    @Override
    public CandidateMidas deleteCandidate(String id) throws Exception {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
        CandidateMidas candidate = mongoTemplateProvider.getMongoTemplate().findById(id, CandidateMidas.class);

        if (candidate != null) {
            mongoTemplateProvider.getMongoTemplate().remove(candidate);
            return candidate;
        } else {
            throw new Exception("CandidateRequest not found with id: " + id);
        }
    }


    @Override
    public Page<CandidateMidas> getAllCandidates(Pageable pageable) {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
        Query query = new Query().with(pageable);

        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
        long count = mongoTemplateProvider.getMongoTemplate().count(new Query(), CandidateMidas.class);

        return new PageImpl<>(candidates, pageable, count);
    }


    /**
     * @return
     */
    @Override
    public List<CandidateMidas> getAllCandidates() {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().findAll(CandidateMidas.class);
        System.out.println(candidates.size());
        
        return candidates;
        
    }


    @Override
    public Optional<CandidateMidas> getCandidateById(String id) {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
        CandidateMidas candidate = mongoTemplateProvider.getMongoTemplate().findById(id, CandidateMidas.class);
        return Optional.ofNullable(candidate);
    }
    @Override
    public Optional<CandidateMidas> getCandidateByEmail(String email) {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
        String cleanedEmail = email.trim().replaceAll("^[,\\-\"'@#]+|[,\\-\"'@#]+$", "");

        Query query = new Query(Criteria.where("email").is(cleanedEmail));
        CandidateMidas candidate = mongoTemplateProvider.getMongoTemplate().findOne(query, CandidateMidas.class);

        return Optional.ofNullable(candidate);
    }


    /**
     * @param includeSkills
     * @param excludeSkills
     * @param emailRegex
     * @param phoneRegex
     * @param cityRegex
     * @param stateRegex
     * @return
     */
    @Override
    public List<CandidateMidas> searchCandidates(List<String> includeSkills, List<String> excludeSkills, String emailRegex, String phoneRegex, String cityRegex, String stateRegex) {
        return null;
    }



    public CandidateMidas mapDocumentToCandidateMidasWithCounts(Document doc) {
        CandidateMidas candidateMidas = new CandidateMidas();

        // Map the simple fields
        candidateMidas.setId(doc.getObjectId("_id").toString());
        candidateMidas.setName(doc.getString("name"));
        candidateMidas.setEmail(doc.getString("email"));
        candidateMidas.setPhone(doc.getString("phone"));
//        candidateMidas.setTotalExp(doc.getString("totalExp"));
//        candidateMidas.setDegree(doc.getString("degree"));
//        candidateMidas.setUniversity(doc.getString("university"));

//        // Map the companiesWorkedAt field (assuming it's an array of strings)
//        List<Object> companiesWorkedAt = (List<Object>) doc.get("companiesWorkedAt");
//        if (companiesWorkedAt != null) {
//            candidateMidas.setCompaniesWorkedAt(companiesWorkedAt);
//        }

//        // Map the uploadCount
//        candidateMidas.setUploadCount(doc.getInteger("uploadCount", 0));

        // Map the additionalProperties field
//        Document additionalProperties = (Document) doc.get("additionalProperties");
//        if (additionalProperties != null) {
//            candidateMidas.setAdditionalProperties(additionalProperties);
//        }

        // Map the changeLogDetails
        List<Document> changeLogDetailsList = (List<Document>) doc.get("changeLogDetails");
        if (changeLogDetailsList != null) {
            List<ChangeLog> changeLogDetails = new ArrayList<>();
            for (Document changeLogDetailDoc : changeLogDetailsList) {
                ChangeLog changeLogDetail = new ChangeLog();
                changeLogDetail.setId(changeLogDetailDoc.getObjectId("_id").toString());
                changeLogDetail.setUser(changeLogDetailDoc.get("user",User.class));
                // map other fields in ChangeLogDetails as necessary
                changeLogDetails.add(changeLogDetail);
                candidateMidas.setChangeLog(changeLogDetail);
            }

        }

        return candidateMidas;
    }
    public List<CandidateMidas> getCandidateMidasByUserIds(List<String> userIds) {
        // Convert userIds to ObjectId list
        List<ObjectId> objectIds = userIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());
        AggregationOptions options = AggregationOptions.builder().allowDiskUse(true).build();

        // Build the aggregation pipeline
        Aggregation aggregation = Aggregation.newAggregation(
                // Step 1: $lookup to join the 'candidateMidas' collection with 'changeLog'
                Aggregation.lookup("changeLog", "changeLog.$id", "_id", "changeLogDetails"),

                // Step 2: $unwind the 'changeLogDetails' array
                Aggregation.unwind("changeLogDetails"),

                // Step 3: $lookup to join 'changeLog' collection with 'user'
                Aggregation.lookup("user", "changeLogDetails.user.$id", "_id", "userDetails"),

                // Step 4: $unwind the 'userDetails' array
                Aggregation.unwind("userDetails"),

                // Step 5: $match to filter based on user._id field
                Aggregation.match(Criteria.where("userDetails._id").in(objectIds)),

                // Step 6: $project to include all fields from the candidateMidas document and DBRefs
                Aggregation.project()
                        .andExclude("_id") // Optionally exclude _id if not needed
                        .and("changeLogDetails").as("changeLogDetails") // Include the entire 'changeLogDetails' subdocument
                        .and("userDetails").as("userDetails") // Include the entire 'userDetails' subdocument
                        .and("name").as("name")
                        .and("email").as("email")
                        .and("phone").as("phone")
                        .and("totalExp").as("totalExp")
                        .and("degree").as("degree")
                        .and("university").as("university")
                        .and("companiesWorkedAt").as("companiesWorkedAt")
        )  .withOptions(options);;

        // Execute the aggregation query
        AggregationResults<CandidateMidas> results =  mongoTemplateProvider.getMongoTemplate()
                .aggregate(aggregation, "candidateMidas", CandidateMidas.class);

        // Return the results as a list of CandidateMidas
        return results.getMappedResults();
    }


    public List<CandidateChangeLogResponse> getCandidateMidasByUserIdsWithCounts(List<String> userIds) {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        // Get the Mongo collection
        MongoCollection<Document> collection = mongoTemplateProvider.getMongoTemplate().getCollection("candidateMidas");

        // Convert userIds to ObjectId list
        List<ObjectId> objectIds = userIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

//        List<Document> aggregationPipeline = Arrays.asList(
//                // Step 1: $lookup to join 'changeLog' collection
//                new Document("$lookup", new Document("from", "changeLog")
//                        .append("localField", "changeLog.$id")
//                        .append("foreignField", "_id")
//                        .append("as", "changeLogDetails")),
//
//                // Step 2: $unwind the 'changeLogDetails' array
//                new Document("$unwind", new Document("path", "$changeLogDetails")),
//
//                // Step 3: $lookup to join 'user' collection
//                new Document("$lookup", new Document("from", "user")
//                        .append("localField", "changeLogDetails.user.$id")
//                        .append("foreignField", "_id")
//                        .append("as", "userDetails")),
//
//                // Step 4: $unwind the 'userDetails' array
//                new Document("$unwind", new Document("path", "$userDetails")),
//
//                // Step 5: $match to filter based on user._id field
//                new Document("$match", new Document("userDetails._id", new Document("$in", objectIds))),
//
//                // Step 6: $group to aggregate by userId and collect relevant fields
//                new Document("$group", new Document("_id", "$userDetails._id")
//                        .append("candidates", new Document("$push", new Document("changeLogDetails", "$changeLogDetails")
//                              ))),
//
//                // Optional: $project to control which fields are included
//                new Document("$project", new Document("_id", 0)
//                        .append("userId", "$_id")
//                        .append("candidates", 1))
//        );

        List<Document> aggregationPipeline = Arrays.asList(new Document("$lookup",
                        new Document("from", "changeLog")
                                .append("localField", "changeLog.$id")
                                .append("foreignField", "_id")
                                .append("as", "changeLogDetails")),
                new Document("$unwind",
                        new Document("path", "$changeLogDetails")),
                new Document("$lookup",
                        new Document("from", "user")
                                .append("localField", "changeLogDetails.user.$id")
                                .append("foreignField", "_id")
                                .append("as", "userDetails")),
                new Document("$unwind",
                        new Document("path", "$userDetails")),
                new Document("$match",
                        new Document("userDetails._id",
                                new Document("$in", objectIds))),
                new Document("$group",
                        new Document("_id", "$userDetails._id")
                                .append("candidates",
                                        new Document("$push",
                                                new Document("changeLogDetails", "$changeLogDetails")))),
                new Document("$project",
                        new Document("_id", 0L)
                                .append("userId", "$_id")
                                .append("name", "$firstName")
                                .append("candidates", 1L)));


        // Execute the aggregation query
        AggregateIterable<Document> result = collection.aggregate(aggregationPipeline);

        // List to hold the mapped results
        List<CandidateChangeLogResponse> candidateMidasList = new ArrayList<>();

        // Process each document from the aggregation result
        for (Document doc : result) {
            CandidateMidas candidate = mapDocumentToCandidateMidasWithCounts(doc);
//            candidateMidasList.add(candidate);
        }

        // Return the list of CandidateMidas objects
        return candidateMidasList;
    }


    /**
     * @return
     */
    @Override
    public List<CandidateMidas> getAllCandidateNotOnProject() {
        // Create a MongoTemplate instance with tenant ID from the header
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        // Retrieve all candidates from the database
        return mongoTemplateProvider.getMongoTemplate().findAll(CandidateMidas.class);
    }

    /**
     * @param filePath
     * @return
     */

    @Override
    public String getCandidateEmailFromDoc(Path filePath) throws IOException{
        String docText = "";
        String email="";
        if (isPDF(String.valueOf(filePath))) {
            docText = FileUtils.convertPdfToText(String.valueOf(filePath));
            email = FileUtils.extractEmail(docText);
        } else if (isDocx(String.valueOf(filePath))) {
            docText = FileUtils.convertWordToText(String.valueOf(filePath));
            email = FileUtils.extractEmail(docText);
        }  else if (isDoc(String.valueOf(filePath))) {
            docText = FileUtils.convertDocToText(String.valueOf(filePath));
            email = FileUtils.extractEmail(docText);
        } else {
            System.out.println("The file type is unknown.");
        }

        return email;
    }
    @Override
    public boolean getCandidateByEmailFromDoc(Path filePath) throws IOException {
        String docText = "";
        String email = "";

        // Determine the file type and extract text and email accordingly
        if (isPDF(filePath.toString())) {
            docText = FileUtils.convertPdfToText(filePath.toString());
            email = FileUtils.extractEmail(docText);
        } else if (isDocx(filePath.toString())) {
            docText = FileUtils.convertWordToText(filePath.toString());
            email = FileUtils.extractEmail(docText);
        } else if (isDoc(filePath.toString())) {
            docText = FileUtils.convertDocToText(filePath.toString());
            email = FileUtils.extractEmail(docText);
        } else {
            System.out.println("The file type is unknown.");
            return false;
        }

        // Extract first email if multiple are found
        String primaryEmail = email.split(",")[0].trim();

        // Query MongoDB for a candidate with the extracted email
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());
        Query query = new Query(Criteria.where("email").is(primaryEmail));
        CandidateMidas candidate = mongoTemplateProvider.getMongoTemplate().findOne(query, CandidateMidas.class);

        return candidate != null;
    }


    public List<CandidateMidas> searchCandidates(String emailRegex, String phoneRegex) {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        // Build the query
        Query query = new Query();
        if (emailRegex != null && !emailRegex.isEmpty()) {
            query.addCriteria(Criteria.where("email").regex(emailRegex, "i")); // Case-insensitive
        }
        if (phoneRegex != null && !phoneRegex.isEmpty()) {
            query.addCriteria(Criteria.where("phone").regex(phoneRegex, "i")); // Case-insensitive
        }

        // Execute the query
        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);

        return candidates;
    }


    /**
     * @return
     */
    // Method to get distinct skills from all candidates
    public Set<String> getDistinctSkills() {
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        // Use MongoTemplate to get distinct values for the "skills" field
        List<String> skills = mongoTemplateProvider.getMongoTemplate().findDistinct("skills", CandidateMidas.class, String.class);

        // Process the list to handle case insensitivity, whitespace trimming, and remove empty skills
        return skills.stream()
                .map(String::trim)        // Trim whitespace from each skill
                .filter(skill -> !skill.isEmpty()) // Filter out empty skills
                .map(String::toLowerCase) // Convert to lowercase for case-insensitive uniqueness
                .collect(Collectors.toSet()); // Collect unique skills into a Set
    }

    /**
     * @param query
     * @return
     */
    @Override
    // Method to get autocomplete suggestions based on partial input
    public Set<String> getSkillSuggestions(String query) {
        Set<String> allSkills = getDistinctSkills();
        String lowerCaseQuery = query.toLowerCase();
        return allSkills.stream()
                .filter(skill -> skill.contains(lowerCaseQuery)) // Filter skills containing the query
                .collect(Collectors.toSet()); // Collect filtered skills
    }
    @Override
    public Page<CandidateMidas> findCandidatesBySkills(List<String> andCriteria, List<String> orCriteria, List<String> notCriteria, Pageable pageable) {
        // Implement your logic for querying candidates by skills
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        // Create the main list for all criteria
        List<Criteria> criteriaList = new ArrayList<>();

        // AND criteria
        if (andCriteria != null && !andCriteria.isEmpty()) {
            andCriteria.forEach(skill ->
                    criteriaList.add(Criteria.where("skills").regex("\\b" + Pattern.quote(skill) + "\\b", "i"))
            );
        }

        // OR criteria
        if (orCriteria != null && !orCriteria.isEmpty()) {
            List<Criteria> orCriteriaList = new ArrayList<>();
            orCriteria.forEach(skill ->
                    orCriteriaList.add(Criteria.where("skills").regex("\\b" + Pattern.quote(skill) + "\\b", "i"))
            );
            criteriaList.add(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
        }

        // NOT criteria
        if (notCriteria != null && !notCriteria.isEmpty()) {
            notCriteria.forEach(skill ->
                    criteriaList.add(Criteria.where("skills").not().regex("\\b" + Pattern.quote(skill) + "\\b", "i"))
            );
        }

        // Combine criteria with AND logic at the top level
        Criteria finalCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        Query query = new Query(finalCriteria).with(pageable);

        // Execute query and get results
        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
        long total = mongoTemplateProvider.getMongoTemplate().count(Query.of(query).limit(-1).skip(-1L), CandidateMidas.class);

        return new PageImpl<>(candidates, pageable, total);    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }


    /**
     * @param searchText
     * @return
     */
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



    @Override
    public List<CandidateMidas> searchCandidatesByNormalizedPhone(String searchText, int page, int size) {
        // Input validation
        if (searchText == null || searchText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        searchText = searchText.trim();
        String cleanedPhone = searchText.replaceAll("[^\\d]", "");

        Query query = new Query();

        // Build query based on search type
        if (isValidEmail(searchText)) {
            // Exact email match first, then partial
            Criteria emailCriteria = new Criteria().orOperator(
                    Criteria.where("email").is(searchText.toLowerCase()),
                    Criteria.where("email").regex(Pattern.quote(searchText), "i")
            );
            query.addCriteria(emailCriteria);

        } else if (!cleanedPhone.isEmpty() && cleanedPhone.length() >= 10) {

          searchText=  searchText.replace("\"", "").trim();           // Remove double quotes
// Phone search with multiple strategies
            Criteria phoneCriteria = new Criteria().orOperator(
                    Criteria.where("phone").is(cleanedPhone),
                    Criteria.where("normalizedPhone").is(cleanedPhone),
                    Criteria.where("fullText").regex(".*" + Pattern.quote(searchText) + ".*", "i")
            );
            query.addCriteria(phoneCriteria);

        } else {

            searchText=  searchText.replace("\"", "").trim();               // Remove double quotes
            // Name search - more targeted approach
            Criteria nameCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex("^" + Pattern.quote(searchText), "i"), // Starts with
                    Criteria.where("firstName").regex("^" + Pattern.quote(searchText), "i"),
                    Criteria.where("lastName").regex("^" + Pattern.quote(searchText), "i"),
                    Criteria.where("fullName").regex(".*" + Pattern.quote(searchText) + ".*", "i") // Contains
            );
            query.addCriteria(nameCriteria);
        }

        // Add pagination andq sorting
        query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));

        // Limit fields returned to reduce response size
        query.fields()
                .include("id")
                .include("name")
                .include("firstName")
                .include("lastName")
                .include("email")
                .include("phone")
                .include("currentRole")
                .include("experience")
                .include("location")
                .include("state")
                .include("city")
                .include("zip")
                .include("primarySpeciality")
                .include("updatedAt");

        return mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class, "candidateMidas");
    }

    // Overloaded method for backward compatibility
    @Override
    public List<CandidateMidas> searchCandidatesByNormalizedPhone(String searchText) {
        return searchCandidatesByNormalizedPhone(searchText, 0, 50); // Default to first 50 results
    }



    /**
     * Search candidates across multiple fields safely with exact matching
     * @param searchText the text to search for
     * @return list of matching candidates (limited to prevent large responses)
     */
    @Override
    public List<CandidateMidas> searchAcrossAllFieldsSafelyExact(String searchText) {
        return searchAcrossAllFieldsSafelyExact(searchText, 0, 100); // Default limit
    }

    /**
     * Search candidates across multiple fields with pagination
     * @param searchText the text to search for
     * @param page page number (0-based)
     * @param size number of results per page
     * @return list of matching candidates
     */
    public List<CandidateMidas> searchAcrossAllFieldsSafelyExact(String searchText, int page, int size) {
        // Input validation
        if (searchText == null || searchText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Sanitize input to prevent regex injection
        String sanitizedSearchText = sanitizeSearchInput(searchText.trim());

        if (sanitizedSearchText.isEmpty()) {
            return Collections.emptyList();
        }

        // Build query using the helper method
        Query query = buildSearchQuery(sanitizedSearchText);

        // Add pagination and sorting
        query.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));

        // Limit fields to reduce response size
        query.fields()
                .include("id")
                .include("name")
                .include("firstName")
                .include("lastName")
                .include("email")
                .include("phone")
                .include("city")
                .include("state")
                .include("currentCompany")
                .include("designation")
                .include("experience")
                .include("skills")
                .include("updatedAt");

        // Execute query with explicit collection name
        return mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class, "candidateMidas");
    }

    /**
     * Sanitize search input to prevent injection attacks
     * @param input raw search input
     * @return sanitized search text
     */
    private String sanitizeSearchInput(String input) {
        if (input == null) return "";

        return input
                .replaceAll("[\"'`\\\\]", "")           // Remove quotes and backslashes
                .replaceAll("[<>{}\\[\\]()]", "")       // Remove brackets and parentheses
                .replaceAll("\\$", "")                  // Remove dollar signs (MongoDB operators)
                .replaceAll("\\s+", " ")                // Normalize whitespace
                .trim();
    }

    /**
     * Get search results with total count for pagination
     * @param searchText search query
     * @param page page number
     * @param size page size
     * @return search results with metadata
     */
    public SearchResult<CandidateMidas> searchAcrossAllFieldsWithCount(String searchText, int page, int size) {
        // Input validation
        if (searchText == null || searchText.trim().isEmpty()) {
            return new SearchResult<>(Collections.emptyList(), 0, page, size);
        }

        String sanitizedSearchText = sanitizeSearchInput(searchText.trim());
        if (sanitizedSearchText.isEmpty()) {
            return new SearchResult<>(Collections.emptyList(), 0, page, size);
        }

        // Build base query once
        Query baseQuery = buildSearchQuery(sanitizedSearchText);

        // Get total count first (more efficient)
        long totalCount = mongoTemplateProvider.getMongoTemplate().count(baseQuery, "candidateMidas");

        if (totalCount == 0) {
            return new SearchResult<>(Collections.emptyList(), 0, page, size);
        }

        // Clone query for results with pagination
        Query resultsQuery = buildSearchQuery(sanitizedSearchText);
        resultsQuery.with(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));

        // Add field limiting
        resultsQuery.fields()
                .include("id")
                .include("name")
                .include("firstName")
                .include("lastName")
                .include("email")
                .include("phone")
                .include("city")
                .include("state")
                .include("currentCompany")
                .include("designation")
                .include("experience")
                .include("skills")
                .include("updatedAt");

        List<CandidateMidas> results = mongoTemplateProvider.getMongoTemplate()
                .find(resultsQuery, CandidateMidas.class, "candidateMidas");

        return new SearchResult<>(results, totalCount, page, size);
    }

    private Query buildSearchQuery(String sanitizedSearchText) {
        if (sanitizedSearchText == null || sanitizedSearchText.trim().isEmpty()) {
            return new Query();
        }

        Criteria criteria = new Criteria().orOperator(
                // Exact matches for structured data
                Criteria.where("zip").is(sanitizedSearchText),
                Criteria.where("phone").is(sanitizedSearchText),
                Criteria.where("email").is(sanitizedSearchText.toLowerCase()),
                Criteria.where("name").is(sanitizedSearchText.toLowerCase()),
//                Criteria.where("lastName").is(sanitizedSearchText.toLowerCase())
//                // Case-insensitive exact matches for names/locations
                Criteria.where("city").regex("^" + Pattern.quote(sanitizedSearchText) + "$", "i"),
                Criteria.where("state").regex("^" + Pattern.quote(sanitizedSearchText) + "$", "i"),
                Criteria.where("name").regex("^" + Pattern.quote(sanitizedSearchText) + "$", "i")
//                Criteria.where("lastName").regex("^" + Pattern.quote(sanitizedSearchText) + "$", "i"),

                // Exact word matching in text fields (not partial)
//                Criteria.where("currentCompany").regex("\\b" + Pattern.quote(sanitizedSearchText) + "\\b", "i"),
//                Criteria.where("designation").regex("\\b" + Pattern.quote(sanitizedSearchText) + "\\b", "i")
        );

        return new Query(criteria);
    }
    /**
     * SearchResult wrapper class for pagination metadata
     */
    public static class SearchResult<T> {
        private List<T> results;
        private long totalCount;
        private int currentPage;
        private int pageSize;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;

        public SearchResult(List<T> results, long totalCount, int currentPage, int pageSize) {
            this.results = results;
            this.totalCount = totalCount;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
            this.hasNext = currentPage < totalPages - 1;
            this.hasPrevious = currentPage > 0;
        }

        // Getters
        public List<T> getResults() { return results; }
        public long getTotalCount() { return totalCount; }
        public int getCurrentPage() { return currentPage; }
        public int getPageSize() { return pageSize; }
        public int getTotalPages() { return totalPages; }
        public boolean isHasNext() { return hasNext; }
        public boolean isHasPrevious() { return hasPrevious; }
    }

    // Helper method for paginated search with count
    public SearchResult<CandidateMidas> searchCandidatesWithCount(String searchText, int page, int size) {
        List<CandidateMidas> results = searchCandidatesByNormalizedPhone(searchText, page, size);

        // Get total count for pagination (remove pagination from query)
        Query countQuery = buildSearchQueryOld(searchText);
        long totalCount = mongoTemplateProvider.getMongoTemplate().count(countQuery, "candidateMidas");

        return new SearchResult<>(results, totalCount, page, size);
    }

    private Query buildSearchQueryOld(String searchText) {
        // Extract the query building logic for reuse
        Query query = new Query();
        String cleanedPhone = searchText.replaceAll("[^\\d]", "");

        if (isValidEmail(searchText)) {
            Criteria emailCriteria = new Criteria().orOperator(
                    Criteria.where("email").is(searchText.toLowerCase()),
                    Criteria.where("email").regex(Pattern.quote(searchText), "i")
            );
            query.addCriteria(emailCriteria);
        } else if (!cleanedPhone.isEmpty() && cleanedPhone.length() >= 10) {
            Criteria phoneCriteria = new Criteria().orOperator(
                    Criteria.where("phone").is(cleanedPhone),
                    Criteria.where("normalizedPhone").is(cleanedPhone),
                    Criteria.where("fullText").regex(".*" + Pattern.quote(searchText) + ".*", "i")
            );
            query.addCriteria(phoneCriteria);
        } else {
            Criteria nameCriteria = new Criteria().orOperator(
                    Criteria.where("name").regex("^" + Pattern.quote(searchText), "i"),
                    Criteria.where("firstName").regex("^" + Pattern.quote(searchText), "i"),
                    Criteria.where("lastName").regex("^" + Pattern.quote(searchText), "i"),
                    Criteria.where("fullName").regex(".*" + Pattern.quote(searchText) + ".*", "i")
            );
            query.addCriteria(nameCriteria);
        }

        return query;
    }

    // Response wrapper for pagination
    public static class SearchResultExact<T> {
        private List<T> results;
        private long totalCount;
        private int currentPage;
        private int pageSize;
        private int totalPages;

        public SearchResultExact(List<T> results, long totalCount, int currentPage, int pageSize) {
            this.results = results;
            this.totalCount = totalCount;
            this.currentPage = currentPage;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        }

        // Getters and setters...
    }
    /**
     * @param options
     * @return
     */
    @Override
    public List<CandidateMidas> getChangeLogsByUserIds(Map<String, List<String>> options) {
        return null;
    }

    /**
     * @param userIds
     * @return
     */
    @Override
    public List<CandidateChangeLogResponse> getCandidateMidasByChangeLogs(List<String> userIds) {

        MongoTemplate mongoTemplate=mongoTemplateProvider.getMongoTemplate();
        // Convert userIds to ObjectIds
        List<ObjectId> objectIdList = userIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<Document> pipeline = Arrays.asList(
                new Document("$lookup", new Document("from", "changeLog")
                        .append("localField", "changeLog.$id")
                        .append("foreignField", "_id")
                        .append("as", "changeLogDetails")),
                new Document("$unwind", new Document("path", "$changeLogDetails")),
                new Document("$lookup", new Document("from", "user")
                        .append("localField", "changeLogDetails.user.$id")
                        .append("foreignField", "_id")
                        .append("as", "userDetails")),
                new Document("$unwind", new Document("path", "$userDetails")),
                new Document("$match", new Document("userDetails._id", new Document("$in", objectIdList))),
                new Document("$group", new Document("_id", "$userDetails._id")
                        .append("candidates", new Document("$push", new Document("changeLogDetails", "$changeLogDetails")
                                .append("id", "$_id")
                                .append("name", "$name")
                                .append("email", "$email")))),
                new Document("$project", new Document("_id", 0)
                        .append("userId", "$_id")
                        .append("name", "$firstName")
                        .append("candidates", 1))
        );

        // Execute the aggregation
        AggregateIterable<Document> result = mongoTemplate.getCollection("candidateMidas").aggregate(pipeline);

        // Map the results
        return mapCandidateChangeLogResponses(result.into(new ArrayList<>()),mongoTemplate);
    }


    public List<CandidateChangeLogResponse> mapCandidateChangeLogResponses(List<Document> aggregationResults, MongoTemplate mongoTemplate) {
        List<CandidateChangeLogResponse> responses = new ArrayList<>();

        for (Document result : aggregationResults) {
            // Create CandidateChangeLogResponse for each user
            CandidateChangeLogResponse response = new CandidateChangeLogResponse();
            List<Document> candidates = result.getList("candidates", Document.class);
            List<CandidateMidas> candidateMidasList = candidates.stream()
                    .map(candidateDoc -> {
                        CandidateMidas candidateMidas = new CandidateMidas();
                        candidateMidas.setId(candidateDoc.getObjectId("id").toString());
                        candidateMidas.setName(candidateDoc.getString("name"));
                        candidateMidas.setEmail(candidateDoc.getString("email"));
                        Document changeLogDetails = candidateDoc.get("changeLogDetails", Document.class);
                        if (changeLogDetails != null) {
                            ChangeLog changeLog = new ChangeLog();
                            changeLog.setId(changeLogDetails.getObjectId("_id").toString());
                            changeLog.setUserNotes(changeLogDetails.getString("userNotes"));
                            // Fetch userDbRef from changeLogDetails
                            DBRef userDbRef = changeLogDetails.get("user", DBRef.class);

                            // Fetch the user from the database using mongoTemplate
                            if (userDbRef != null) {
                                // Assuming you have a User class and your user collection name is "users"
                                User user = mongoTemplate.findById(userDbRef.getId(), User.class);
                                if (user != null) {
                                    response.setUserId(user.getId());  // Set the userId from the User object
                                    changeLog.setUser(user);  // Set the user on the changeLog object
                                }
                            }
                            response.setUserId(userDbRef.getId().toString());
                            candidateMidas.setChangeLog(changeLog); // Assuming there's a setChangeLog method in CandidateMidas
                        }
                        return candidateMidas;
                    })
                    .collect(Collectors.toList());


            response.setCandidateMidas(candidateMidasList);
            responses.add(response);
        }

        return responses;
    }


    //===============

// Add this method to your MidasCandidateServiceImpl class
// This replaces the problematic getCandidatesAndChangeLogs method


    @Override
    public List<CandidateChangeLogResponse> getCandidatesAndChangeLogs(List<String> userIds) {
        long startTime = System.currentTimeMillis();
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        try {
            logger.info("Starting getCandidatesAndChangeLogs for {} users", userIds.size());

            // Input validation
            if (userIds == null || userIds.isEmpty()) {
                return Collections.emptyList();
            }

            // Convert to ObjectIds with validation
            List<ObjectId> objectIdList = userIds.stream()
                    .filter(Objects::nonNull)
                    .map(userId -> {
                        try {
                            return new ObjectId(userId);
                        } catch (IllegalArgumentException e) {
                            logger.warn("Invalid ObjectId: {}", userId);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (objectIdList.isEmpty()) {
                logger.warn("No valid ObjectIds found");
                return Collections.emptyList();
            }

            // SIMPLIFIED APPROACH - Use direct queries instead of complex aggregation
            logger.info("Step 1: Finding users");
            Query userQuery = new Query(Criteria.where("_id").in(objectIdList));
            userQuery.maxTime(10, TimeUnit.SECONDS); // Add timeout
            List<User> users = mongoTemplate.find(userQuery, User.class);

            if (users.isEmpty()) {
                logger.warn("No users found for provided IDs");
                return Collections.emptyList();
            }

            logger.info("Step 2: Finding change logs for {} users", users.size());
            Query changeLogQuery = new Query(Criteria.where("user.$id").in(objectIdList))
                    .limit(1000) // Limit to prevent timeout
                    .with(Sort.by(Sort.Direction.DESC, "changeDateTime"));
            changeLogQuery.maxTime(15, TimeUnit.SECONDS); // Add timeout

            List<ChangeLog> changeLogs = mongoTemplate.find(changeLogQuery, ChangeLog.class);

            if (changeLogs.isEmpty()) {
                logger.warn("No change logs found");
                return Collections.emptyList();
            }

            logger.info("Step 3: Finding candidates for {} change logs", changeLogs.size());
            List<ObjectId> changeLogIds = changeLogs.stream()
                    .map(cl -> new ObjectId(cl.getId()))
                    .collect(Collectors.toList());

            Query candidateQuery = new Query(Criteria.where("changeLog.$id").in(changeLogIds))
                    .limit(500); // Limit candidates
            candidateQuery.maxTime(15, TimeUnit.SECONDS); // Add timeout

            List<CandidateMidas> candidates = mongoTemplate.find(candidateQuery, CandidateMidas.class);

            logger.info("Step 4: Building response for {} candidates", candidates.size());

            // Build response map
            Map<String, CandidateChangeLogResponse> responseMap = new HashMap<>();

            for (CandidateMidas candidate : candidates) {
                if (candidate.getChangeLog() != null) {
                    // Find the full change log
                    ChangeLog fullChangeLog = changeLogs.stream()
                            .filter(cl -> cl.getId().equals(candidate.getChangeLog().getId()))
                            .findFirst()
                            .orElse(null);

                    if (fullChangeLog != null && fullChangeLog.getUser() != null) {
                        String userId = fullChangeLog.getUser().getId();

                        CandidateChangeLogResponse response = responseMap.computeIfAbsent(userId, id -> {
                            CandidateChangeLogResponse res = new CandidateChangeLogResponse();
                            res.setUserId(userId);
                            res.setCandidateMidas(new ArrayList<>());
                            return res;
                        });

                        candidate.setChangeLog(fullChangeLog);
                        response.getCandidateMidas().add(candidate);
                    }
                }
            }

            List<CandidateChangeLogResponse> result = new ArrayList<>(responseMap.values());

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Completed getCandidatesAndChangeLogs in {}ms, returning {} responses", duration, result.size());

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Error in getCandidatesAndChangeLogs after {}ms: {}", duration, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // Optimized result mapping
    private List<CandidateChangeLogResponse> mapOptimizedResults(List<Document> results) {
        Map<String, CandidateChangeLogResponse> responseMap = new HashMap<>();

        for (Document doc : results) {
            String userId = doc.get("userId").toString();

            CandidateChangeLogResponse response = responseMap.computeIfAbsent(userId, id -> {
                CandidateChangeLogResponse res = new CandidateChangeLogResponse();
                res.setUserId(userId);
                res.setCandidateMidas(new ArrayList<>());
                return res;
            });

            // Map candidate data
            CandidateMidas candidateMidas = new CandidateMidas();
            candidateMidas.setId(doc.get("candidateId", ObjectId.class).toString());
            candidateMidas.setName(doc.getString("name"));
            candidateMidas.setEmail(doc.getString("email"));

            if (doc.get("date_added") != null) {
                candidateMidas.setDate_added((Date) doc.get("date_added"));
            }

            // Map change log data
            Document changeLogDoc = doc.get("changeLogDetails", Document.class);
            if (changeLogDoc != null) {
                ChangeLog changeLog = new ChangeLog();
                changeLog.setId(changeLogDoc.getObjectId("_id").toString());
                changeLog.setUserNotes(changeLogDoc.getString("userNotes"));
                changeLog.setChangeDateTime(changeLogDoc.getDate("changeDateTime"));

                // Map user data
                Document userDoc = doc.get("userDetails", Document.class);
                if (userDoc != null) {
                    User user = new User();
                    user.setId(userDoc.getObjectId("_id").toString());
                    user.setFirstName(userDoc.getString("firstName"));
                    user.setLastName(userDoc.getString("lastName"));
                    user.setEmail(userDoc.getString("email"));
                    changeLog.setUser(user);
                }

                candidateMidas.setChangeLog(changeLog);
            }

            response.getCandidateMidas().add(candidateMidas);
        }

        return new ArrayList<>(responseMap.values());
    }

    // Fallback simple approach
    private List<CandidateChangeLogResponse> getCandidatesAndChangeLogsSimple(List<String> userIds) {
        logger.info("Using simple fallback approach for getCandidatesAndChangeLogs");

        try {
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            // Step 1: Get users by IDs
            List<ObjectId> userObjectIds = userIds.stream()
                    .filter(Objects::nonNull)
                    .map(userId -> {
                        try {
                            return new ObjectId(userId);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            Query userQuery = new Query(Criteria.where("_id").in(userObjectIds));
            List<User> users = mongoTemplate.find(userQuery, User.class);

            if (users.isEmpty()) {
                return Collections.emptyList();
            }

            // Step 2: Get change logs for these users (with reasonable limit)
            Query changeLogQuery = new Query(Criteria.where("user.$id").in(userObjectIds))
                    .limit(1000) // Limit to prevent timeout
                    .with(Sort.by(Sort.Direction.DESC, "changeDateTime"));

            List<ChangeLog> changeLogs = mongoTemplate.find(changeLogQuery, ChangeLog.class);

            if (changeLogs.isEmpty()) {
                return Collections.emptyList();
            }

            // Step 3: Get change log IDs
            List<ObjectId> changeLogIds = changeLogs.stream()
                    .map(cl -> new ObjectId(cl.getId()))
                    .collect(Collectors.toList());

            // Step 4: Get candidates with these change logs (with limit)
            Query candidateQuery = new Query(Criteria.where("changeLog.$id").in(changeLogIds))
                    .limit(500); // Limit candidates to prevent timeout

            List<CandidateMidas> candidates = mongoTemplate.find(candidateQuery, CandidateMidas.class);

            // Step 5: Build response
            Map<String, CandidateChangeLogResponse> responseMap = new HashMap<>();

            for (CandidateMidas candidate : candidates) {
                if (candidate.getChangeLog() != null) {
                    // Find the full change log
                    ChangeLog fullChangeLog = changeLogs.stream()
                            .filter(cl -> cl.getId().equals(candidate.getChangeLog().getId()))
                            .findFirst()
                            .orElse(null);

                    if (fullChangeLog != null && fullChangeLog.getUser() != null) {
                        String userId = fullChangeLog.getUser().getId();

                        CandidateChangeLogResponse response = responseMap.computeIfAbsent(userId, id -> {
                            CandidateChangeLogResponse res = new CandidateChangeLogResponse();
                            res.setUserId(userId);
                            res.setCandidateMidas(new ArrayList<>());
                            return res;
                        });

                        candidate.setChangeLog(fullChangeLog);
                        response.getCandidateMidas().add(candidate);
                    }
                }
            }

            return new ArrayList<>(responseMap.values());

        } catch (Exception e) {
            logger.error("Error in simple fallback approach: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    //=====================


    @Override
    public List<CandidateChangeLogResponse> getCandidatesAndChangeLogs(List<String> userIds, Date fromDate, Date toDate) {

        List<ObjectId> objectIdList = userIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<Document> pipeline = Arrays.asList(
                new Document("$lookup", new Document("from", "changeLog")
                        .append("localField", "changeLog.$id")
                        .append("foreignField", "_id")
                        .append("as", "changeLogDetails")),

                new Document("$unwind", new Document("path", "$changeLogDetails")),

                // 🟡 Filter on changeLogDetails.changeDateTime
                new Document("$match", new Document("changeLogDetails.changeDateTime",
                        new Document("$gte", fromDate)
                                .append("$lte", toDate))),

                new Document("$lookup", new Document("from", "user")
                        .append("localField", "changeLogDetails.user.$id")
                        .append("foreignField", "_id")
                        .append("as", "userDetails")),

                new Document("$unwind", new Document("path", "$userDetails")),

                new Document("$match", new Document("userDetails._id", new Document("$in", objectIdList))),

                new Document("$project", new Document("_id", 0)
                        .append("userId", "$userDetails._id")
                        .append("userName", "$userDetails.firstName")
                        .append("candidateId", "$_id")
                        .append("name", "$name")
                        .append("email", "$email")
                        .append("dateAdded", "$date_added")
                        .append("changeLogDetails", "$changeLogDetails"))
        );

        AggregateIterable<Document> results = mongoTemplateProvider.getMongoTemplate()
                .getCollection("candidateMidas")
                .aggregate(pipeline);

        Map<String, CandidateChangeLogResponse> responseMap = new HashMap<>();

        for (Document doc : results) {
            String userId = doc.get("userId").toString();
            CandidateChangeLogResponse response = responseMap.computeIfAbsent(userId, id -> {
                CandidateChangeLogResponse res = new CandidateChangeLogResponse();
                res.setUserId(userId);
                res.setCandidateMidas(new ArrayList<>());
                return res;
            });

            CandidateMidas candidateMidas = new CandidateMidas();
            candidateMidas.setId(doc.get("candidateId").toString());
            candidateMidas.setName(doc.getString("name"));
            candidateMidas.setEmail(doc.getString("email"));

            if (doc.get("dateAdded") != null) {
                candidateMidas.setDate_added((Date) doc.get("dateAdded"));
            }

            ChangeLog changeLog = mongoTemplateProvider.getMongoTemplate().getConverter()
                    .read(ChangeLog.class, (Document) doc.get("changeLogDetails"));

            candidateMidas.setChangeLog(changeLog);
            response.getCandidateMidas().add(candidateMidas);
        }

        return new ArrayList<>(responseMap.values());
    }





    // Alternative approach using Spring Data MongoDB Query Methods

    public List<CandidateChangeLogResponse> getCandidatesAndChangeLogsAlternative(List<String> userIds, Date fromDate, Date toDate) {

        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        // Step 1: Find all changeLogs in date range by specified users
        List<ObjectId> userObjectIds = userIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        Criteria changeLogCriteria = Criteria.where("changeDateTime").gte(fromDate).lte(toDate)
                .and("user.$id").in(userObjectIds);

        Query changeLogQuery = new Query(changeLogCriteria);
        List<ChangeLog> changeLogs = mongoTemplate.find(changeLogQuery, ChangeLog.class);

        if (changeLogs.isEmpty()) {
            return new ArrayList<>();
        }

        // Step 2: Get changeLog IDs
        List<ObjectId> changeLogIds = changeLogs.stream()
                .map(cl -> new ObjectId(cl.getId()))
                .collect(Collectors.toList());

        // Step 3: Find candidates with these changeLogs
        Criteria candidateCriteria = Criteria.where("changeLog.$id").in(changeLogIds);
        Query candidateQuery = new Query(candidateCriteria);
        List<CandidateMidas> candidates = mongoTemplate.find(candidateQuery, CandidateMidas.class);

        // Step 4: Build response map
        Map<String, CandidateChangeLogResponse> responseMap = new HashMap<>();

        for (CandidateMidas candidate : candidates) {
            if (candidate.getChangeLog() != null) {
                ChangeLog changeLog = mongoTemplate.findById(candidate.getChangeLog().getId(), ChangeLog.class);

                // Double-check date range (safety net)
                if (changeLog != null && changeLog.getChangeDateTime() != null &&
                        !changeLog.getChangeDateTime().before(fromDate) &&
                        !changeLog.getChangeDateTime().after(toDate)) {

                    String userId = changeLog.getUser().getId();

                    CandidateChangeLogResponse response = responseMap.computeIfAbsent(userId, id -> {
                        CandidateChangeLogResponse res = new CandidateChangeLogResponse();
                        res.setUserId(userId);
                        res.setCandidateMidas(new ArrayList<>());
                        return res;
                    });

                    candidate.setChangeLog(changeLog);
                    response.getCandidateMidas().add(candidate);
                }
            }
        }

        return new ArrayList<>(responseMap.values());
    }


    /**
     * @param candidate
     * @return
     */
    @Override
    public CandidateMidas addCandidate(CandidateMidas candidate) {//
        //  MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader()); // Create a MongoTemplate for the specific tenant
//        CandidateMidas candidate = modelMapper.map(createCandidateRequest, CandidateMidas.class);
        ChangeLog changeLog = new ChangeLog();
        changeLog.setChangeDateTime(new Date());
        changeLog.setUserNotes("System Added for first time");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Collection<? extends GrantedAuthority> list= authentication.getAuthorities();

        UserDto user  =userService.getUserByEmail((String)authentication.getPrincipal());

        changeLog.setUser(UserMapper.toUser(user));
        ChangeLog changeLogSaved=mongoTemplateProvider   .getMongoTemplate().save(changeLog);
        candidate.setChangeLog(changeLogSaved);
        candidate.setActive(true);

        CandidateMidas candidateSaved = mongoTemplateProvider.getMongoTemplate().save(candidate); // Save using tenant-specific MongoTemplate
        return candidateSaved; // Return the saved candidate
    }


    @Autowired
    private Cache<String, Tenant> tenantCache;

    public static String extractDatabaseName(String connectionString) {
        // Regex to match the database name after the last slash and before any query parameters
        String regex = "mongodb://.*?/(.*?)(\\?|$)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(connectionString);

        if (matcher.find()) {
            // Return the database name part from the connection string
            return matcher.group(1);
        }

        return null; // Return null if no database name is found
    }


    /**
     * @param plusKeywords
     * @param minusKeywords
     * @param orPlusKeywords
     * @param pageable
     * @return
     */
    @Override
    public Page<CandidateMidas> findCandidatesByKeywords(List<String> plusKeywords, List<String> minusKeywords, List<String> orPlusKeywords, Pageable pageable) {
        return null;
    }



public List<CandidateMidas> findCandidates(Map<String, List<String>> andCriteria, Map<String, List<String>> orCriteria, Map<String, List<String>> notCriteria) {
    Query query = new Query();

    // Handle AND criteria
    if (andCriteria != null && !andCriteria.isEmpty()) {
        andCriteria.forEach((field, skills) -> {
            if (!skills.isEmpty()) {
                Criteria criteria = Criteria.where(field)
                        .all(skills.stream()
                                .map(skill -> Pattern.compile(skill, Pattern.CASE_INSENSITIVE))
                                .collect(Collectors.toList()));
                query.addCriteria(criteria);
            }
        });
    }

    // Handle OR criteria
    if (orCriteria != null && !orCriteria.isEmpty()) {
        orCriteria.forEach((field, skills) -> {
            if (!skills.isEmpty()) {
                Criteria criteria = Criteria.where(field)
                        .in(skills.stream()
                                .map(skill -> Pattern.compile(skill, Pattern.CASE_INSENSITIVE))
                                .collect(Collectors.toList()));
                query.addCriteria(new Criteria().orOperator(criteria));
            }
        });
    }

    // Handle NOT criteria
    if (notCriteria != null && !notCriteria.isEmpty()) {
        notCriteria.forEach((field, skills) -> {
            if (!skills.isEmpty()) {
                Criteria criteria = Criteria.where(field)
                        .not()
                        .all(skills.stream()
                                .map(skill -> Pattern.compile(skill, Pattern.CASE_INSENSITIVE))
                                .collect(Collectors.toList()));
                query.addCriteria(criteria);
            }
        });
    }

    return mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
}


    /**
     * @param plusKeywords
     * @param minusKeywords
     * @param orPlusKeywords
     * @return
     */

    public Page<CandidateMidas> searchCandidateResumesPaged(
            List<String> plusKeywords,
            List<String> minusKeywords,
            List<String> orPlusKeywords,
            Pageable pageable) {

        List<Criteria> criteriaList = new ArrayList<>();

        // Handle AND conditions (Plus Keywords)
        for (String keyword : plusKeywords) {
            criteriaList.add(Criteria.where("fullText").regex(keyword, "i")); // Case-insensitive search
        }

        // Handle NOT conditions (Minus Keywords)
        for (String keyword : minusKeywords) {
            criteriaList.add(Criteria.where("fullText").not().regex(keyword, "i"));
        }

        // Handle OR conditions (Or Plus Keywords)
        if (!orPlusKeywords.isEmpty()) {
            List<Criteria> orCriteriaList = new ArrayList<>();
            for (String keyword : orPlusKeywords) {
                orCriteriaList.add(Criteria.where("fullText").regex(keyword, "i"));
            }
            criteriaList.add(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
        }

        // Combine all criteria
        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

        // Apply pagination and sorting
        query.with(pageable);

        // Fetch the paginated results
        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);

        // Count total elements for the given criteria (ignoring pagination)
        long total = mongoTemplateProvider.getMongoTemplate().count(Query.of(query).limit(-1).skip(-1), CandidateMidas.class);

        // Return the paginated results
        return new PageImpl<>(candidates, pageable, total);
    }


    @Override
    public List<CandidateMidas> searchCandidateResumes(List<String> plusKeywords, List<String> minusKeywords, List<String> orPlusKeywords) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Handle AND conditions (Plus Keywords)
        for (String keyword : plusKeywords) {
            criteriaList.add(Criteria.where("fullText").regex(keyword, "i")); // Case-insensitive search
        }

        // Handle NOT conditions (Minus Keywords)
        for (String keyword : minusKeywords) {
            criteriaList.add(Criteria.where("fullText").not().regex(keyword, "i"));
        }

        // Handle OR conditions (Or Plus Keywords)
        if (!orPlusKeywords.isEmpty()) {
            List<Criteria> orCriteriaList = new ArrayList<>();
            for (String keyword : orPlusKeywords) {
                orCriteriaList.add(Criteria.where("fullText").regex(keyword, "i"));
            }
            criteriaList.add(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
        }

        // Combine all criteria
        Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        return mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
    }



    public List<CandidateMidas> findCandidatesBySkills(List<String> includeSkills, List<String> orSkills, List<String> excludeSkills) {
        Criteria baseCriteria = new Criteria();
        Query baseQuery = new Query(baseCriteria);
        List<CandidateMidas> candidates = this.mongoTemplateProvider.getMongoTemplate().find(baseQuery, CandidateMidas.class);
        if (!includeSkills.isEmpty()) {
            candidates = (List)candidates.stream().filter((candidate) -> {
                return includeSkills.stream().allMatch((skill) -> {
                    return candidate.getSkills().stream().anyMatch((s) -> {
                        return s instanceof String && Pattern.compile(skill, 2).matcher(s).find();
                    });
                });
            }).collect(Collectors.toList());
        }

        if (!orSkills.isEmpty()) {
            candidates = (List)candidates.stream().filter((candidate) -> {
                return orSkills.stream().anyMatch((skill) -> {
                    return candidate.getSkills().stream().anyMatch((s) -> {
                        return s instanceof String && Pattern.compile(skill, 2).matcher(s).find();
                    });
                });
            }).collect(Collectors.toList());
        }

        if (!excludeSkills.isEmpty()) {
            candidates = (List)candidates.stream().filter((candidate) -> {
                return excludeSkills.stream().noneMatch((skill) -> {
                    return candidate.getSkills().stream().anyMatch((s) -> {
                        return s instanceof String && Pattern.compile(skill, 2).matcher(s).find();
                    });
                });
            }).collect(Collectors.toList());
        }

        return candidates;
    }


    private String buildRegexPattern(List<String> skills) {
        return skills.stream()
                .map(skill -> Pattern.quote(skill.trim()))
                .collect(Collectors.joining("|"));
    }

    @Override
    public List<CandidateMidas> searchAcrossAllFields(String searchText) {
//    return  candidateRepository.searchAcrossAllFieldsSafely(searchText);
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        // Build the search criteria
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("fullText").regex(searchText, "i"),
                Criteria.where("city").regex(searchText, "i"),
                Criteria.where("state").regex(searchText, "i"),
                Criteria.where("zip").regex(searchText, "i")
                // Add other fields you need to search across, replacing as necessary
        );

        Query query = new Query(criteria);

        // Execute the query
        return mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
    }

    /**
     * @param searchText
     * @return
     */
    @Override
    public List<CandidateMidas> searchAcrossAllFieldsN(String searchText) {
//        return  candidateRepository.searchAcrossAllFieldsSafelyN(searchText);
//        MongoTemplate mongoTemplate = mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        // Build the search criteria
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("fullText").regex(searchText, "i"),
                Criteria.where("city").regex(searchText, "i"),
                Criteria.where("state").regex(searchText, "i"),
                Criteria.where("zip").regex(searchText, "i")
                // Add other fields you need to search across, replacing as necessary
        );

        Query query = new Query(criteria);

        // Execute the query
        return mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
    }

    /**
     * @param booleanExpression
     * @param pageable
     * @return
     */
    @Override
    public Page<CandidateMidas> searchCandidateResumesCityStatePaged(String booleanExpression, Pageable pageable) {
//        MongoTemplate mongoTemplate=mongoTemplateProvider.createMongoTemplate(getTenantIdFromHeader());

        List<Criteria> criteriaList = new ArrayList<>();
        BooleanExpressionParser parser = new BooleanExpressionParser();

        // Parse the boolean expression
        if (booleanExpression != null && !booleanExpression.isEmpty()) {
            criteriaList.add(parser.parse(booleanExpression));
        }

        // Combine all criteria with AND logic at the top level
        Criteria combinedCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        Query query = new Query(combinedCriteria);
        logger.info("Required Search Query  -"+query.toString() );
        query.with(pageable);
        logger.info("Required Search Query Pagable - "+query.toString() );
        // Execute query and count results
        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
        long total = mongoTemplateProvider.getMongoTemplate().count(Query.of(query).limit(-1).skip(-1L), CandidateMidas.class);
//        logger.info("Search count and Ids - ",query, candidates );
        return new PageImpl<>(candidates, pageable, total);
    }

    /**
     * @param city
     * @param state
     * @param zip
     * @param booleanExpression
     * @param pageable
     * @return
     */
    @Override


    public Page<CandidateMidas> searchCandidateResumesCityStatePaged(
            List<String> city, List<String> state, List<String> zip, String booleanExpression, Pageable pageable) {

        List<Criteria> criteriaList = new ArrayList<>();
        BooleanExpressionParser parser = new BooleanExpressionParser();

        // Parse the boolean expression
        if (booleanExpression != null && !booleanExpression.isEmpty()) {
            criteriaList.add(parser.parse(booleanExpression));
        }

        // Location-based criteria for city, state, zip
        if (city != null && !city.isEmpty()) {
            List<Criteria> cityCriteria = city.stream()
                    .map(value -> Criteria.where("city").regex(Pattern.quote(value), "i"))
                    .collect(Collectors.toList());
            criteriaList.add(new Criteria().andOperator(cityCriteria.toArray(new Criteria[0])));
        }

        if (state != null && !state.isEmpty()) {
            List<Criteria> stateCriteria = state.stream()
                    .map(value -> Criteria.where("state").is(value))
                    .collect(Collectors.toList());
            criteriaList.add(new Criteria().andOperator(stateCriteria.toArray(new Criteria[0])));
        }

        if (zip != null && !zip.isEmpty()) {
            List<Criteria> zipCriteria = zip.stream()
                    .map(value -> Criteria.where("zip").is(value))
                    .collect(Collectors.toList());
            criteriaList.add(new Criteria().andOperator(zipCriteria.toArray(new Criteria[0])));
        }

        // Combine all criteria with AND logic at the top level
        Criteria combinedCriteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        Query query = new Query(combinedCriteria);
        query.with(pageable);

        // Execute query and count results
//        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
//        long total = mongoTemplateProvider.getMongoTemplate().count(Query.of(query).limit(-1).skip(-1L), CandidateMidas.class);

        // Execute query and count results
        List<CandidateMidas> candidates = mongoTemplateProvider.getMongoTemplate().find(query, CandidateMidas.class);
        long total = mongoTemplateProvider.getMongoTemplate().count(Query.of(query).limit(-1).skip(-1L), CandidateMidas.class);

        return new PageImpl<>(candidates, pageable, total);
    }

    public static boolean isPDF(String filePath) {
        return filePath.toLowerCase().endsWith(".pdf");
    }

    public static boolean isDocx(String filePath) {
        return (filePath.toLowerCase().endsWith(".docx") ) ;
    }
    public static boolean isDoc(String filePath) {
        return (filePath.toLowerCase().endsWith(".doc")) ;
    }


    @Override
    public Page<CandidateMidas> getCandidatesWithMissingSpecificFields(List<String> fields, Pageable pageable) {
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        List<Criteria> fieldCriteriaList = new ArrayList<>();

        for (String field : fields) {
            Criteria fieldCriteria = new Criteria().orOperator(
                    Criteria.where(field.toLowerCase()).is(null),
                    Criteria.where(field.toLowerCase()).is(""),
                    Criteria.where(field.toLowerCase()).regex("^\\s*$") // Only whitespace
            );
            fieldCriteriaList.add(fieldCriteria);
        }

        // Use OR operator to find candidates missing ANY of the specified fields
        Criteria combinedCriteria = new Criteria().orOperator(
                fieldCriteriaList.toArray(new Criteria[0])
        );

        Query query = new Query(combinedCriteria);
        long total = mongoTemplate.count(query, CandidateMidas.class);

        query.with(pageable);
        List<CandidateMidas> candidates = mongoTemplate.find(query, CandidateMidas.class);

        return new PageImpl<>(candidates, pageable, total);
    }
}
