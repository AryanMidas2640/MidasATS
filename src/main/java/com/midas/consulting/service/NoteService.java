package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.Note;
import com.midas.consulting.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {
//    @Autowired
//    private NoteRepository noteRepository;

    @Autowired
    private MongoTemplateProvider mongoTemplateFactory;



    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);
    public Note createNoteWithUser(Note note, String userId) {
        User user = findUserObjectById(userId);
        note.setCreatedBy(user);
        note.setDateCreated(new Date());
        return mongoTemplateFactory.getMongoTemplate().save(note);
    }

    private String getTenantIdFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-Tenant");
        }
        throw new IllegalStateException("Tenant ID not found in the request header");
    }

    public List<Note> getAllNotes() {
        return mongoTemplateFactory.getMongoTemplate().findAll(Note.class);
    }


    public Optional<Note> getNoteById(String id) {
        Note note = mongoTemplateFactory.getMongoTemplate().findById(id, Note.class);
        return Optional.ofNullable(note);
    }


    public Note updateNote(String id, Note noteDetails) {
//        Optional<Note> optionalNote = noteRepository.findById(id);
//        Query query = new Query(Criteria.where("_id").is(id));
        Note note =  mongoTemplateFactory.getMongoTemplate().findById(id, Note.class);
        if (note!=null) {
//            Note note = optionalNote.get();
            note.setNoteType(noteDetails.getNoteType());
            note.setNote(noteDetails.getNote());
            note.setDateCreated(noteDetails.getDateCreated());
            // Optionally update createdBy if needed
            if (noteDetails.getCreatedBy() != null) {
                note.setCreatedBy(noteDetails.getCreatedBy());
            }

            return  mongoTemplateFactory.getMongoTemplate().save(note);
        } else {
            throw new RuntimeException("Note not found with id: " + id);
        }
    }

    public void deleteNoteById(String id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplateFactory.getMongoTemplate().remove(query, Note.class);
    }


    public Note addLinkedNoteToOriginal(String originalNoteId, Note newLinkedNote, String userId) {
        MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate();
        User user = findUserObjectById(userId);
        newLinkedNote.setCreatedBy(user);

        Note originalNote = mongoTemplate.findById(originalNoteId, Note.class);

        if (originalNote != null) {
            newLinkedNote.setLinkToId(originalNoteId); // Set the linkToId to reference the original note

            // Initialize the linked notes list if it is null
            if (originalNote.getLinkedNotes() == null) {
                originalNote.setLinkedNotes(new ArrayList<>());
            }

            originalNote.getLinkedNotes().add(newLinkedNote);
            mongoTemplate.save(newLinkedNote); // Save the new linked note
            return mongoTemplate.save(originalNote); // Save the updated original note
        } else {
            throw new RuntimeException("Original note not found with id: " + originalNoteId);
        }
    }


    private User findUserObjectById(String userId) {
        User user = mongoTemplateFactory.getMongoTemplate().findById(userId, User.class);
        if (user == null) {
            throw new RuntimeException("User not found with id: " + userId);
        }
        return user;
    }


    public Optional<Note> getNoteByLinkToId(String id) {
        MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate();
        Query query = new Query(Criteria.where("linkToId").is(id));
        Note note = mongoTemplate.findOne(query, Note.class);
        return Optional.ofNullable(note);
    }

    public List<Note> viewNoteBySourceId(String id) {
        MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate();
        Query query = new Query(Criteria.where("linkToId").is(id));
        return mongoTemplate.find(query, Note.class);
    }

    public List<Note> viewNoteByCandidateId(String candidateId) {
        MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate();
        Query query = new Query(Criteria.where("candidateId").is(candidateId));
        return mongoTemplate.find(query, Note.class);
    }


    public List<Note> fetchNotesBasedOnUserAndCriteria(Map<String, Object> options) {
        // Build the aggregation pipeline
        Aggregation aggregation = buildAggregation(options);

        // Get the MongoTemplate instance based on the tenant
        MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate();

        // Execute and fetch results
        return mongoTemplate.aggregate(aggregation, Note.class, Note.class).getMappedResults();
    }

    private Aggregation buildAggregation(Map<String, Object> options) {
        List<AggregationOperation> operations = new ArrayList<>();

        // Build criteria for notes
        Criteria noteCriteria = new Criteria();
        options.forEach((key, value) -> {
            if ("candidateId".equalsIgnoreCase(key)) {
                noteCriteria.and("candidateId").is(value);
            } else if ("noteType".equalsIgnoreCase(key)) {
                noteCriteria.and("noteType").is(value);
            } else if ("dateCreated".equalsIgnoreCase(key) && value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Date> dateRange = (Map<String, Date>) value;
                if (dateRange.containsKey("from") && dateRange.containsKey("to")) {
                    noteCriteria.and("dateCreated").gte(dateRange.get("from")).lte(dateRange.get("to"));
                }
            }
        });

        // Match notes based on criteria
        if (!noteCriteria.getCriteriaObject().isEmpty()) {
            operations.add(Aggregation.match(noteCriteria));
        }

        // Include lookup if user-related filters exist
        boolean hasUserFilters = options.containsKey("userEmail") || options.containsKey("userFullName");
        if (hasUserFilters) {
            operations.add(Aggregation.lookup("user", "createdBy", "_id", "userDetails"));

            // Add user-related filters
            Criteria userCriteria = new Criteria();
            if (options.containsKey("userEmail")) {
                userCriteria.and("userDetails.email").is(options.get("userEmail"));
            }
            if (options.containsKey("userFullName")) {
                String fullNameRegex = ".*" + options.get("userFullName") + ".*";
                userCriteria.and("userDetails.firstName").regex(fullNameRegex, "i");
            }

            // Match user details if criteria exist
            if (!userCriteria.getCriteriaObject().isEmpty()) {
                operations.add(Aggregation.match(userCriteria));
            }
        }

        // Build the aggregation
        return Aggregation.newAggregation(operations);
    }


//    public List<Note> fetchNotesBasedOnCriteria(Map<String, List<String>> options) {
//        Query query = new Query();
//        MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate();
//
//        // Apply filters based on options
//        options.forEach((key, value) -> {
//            if (value != null && !value.isEmpty()) {
//                switch (key.toLowerCase()) {
//                    case "candidateid":
//                        // Handle candidateId with 'in' for multiple values
//                        query.addCriteria(Criteria.where("candidateId").in(value));
//                        break;
//
//                    case "notetype":
//                        // Handle noteType with 'in' for multiple values
//                        query.addCriteria(Criteria.where("noteType").in(value));
//                        break;
//
//                    case "userEmail": {
//                        // Step 1: Find Users whose email is in the list
//                        List<User> users = mongoTemplate.find(
//                                Query.query(Criteria.where("email").in(value)), User.class
//                        );
//
//                        // Step 2: Extract User IDs
//                        List<String> userIds = users.stream()
//                                .map(User::getId)  // Assuming `getId()` returns the ObjectId
//                                .collect(Collectors.toList());
//
//                        // Step 3: Match Notes where createdBy is in userIds
//                        criteria.and("createdBy").in(userIds);
//                        break;
//                    }
//
//                    case "datecreated":
//                        // Expect two values: fromDate and toDate
//                        if (value.size() == 2) {
//                            try {
//                                Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(value.get(0));
//                                Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse(value.get(1));
//                                query.addCriteria(Criteria.where("dateCreated").gte(fromDate).lte(toDate));
//                            } catch (ParseException e) {
//                                throw new IllegalArgumentException("Invalid date format for 'dateCreated'. Use 'yyyy-MM-dd'.", e);
//                            }
//                        } else {
//                            throw new IllegalArgumentException("'dateCreated' must contain exactly 2 values: [fromDate, toDate]");
//                        }
//                        break;
//
//                    default:
//                        logger.warn("Unknown filter key: {}", key);
//                        throw new RuntimeException("Key Specified was not included in query :" + key+"-"+value);
//                }
//            }
//        });
//
//        // Execute the query and fetch notes
//        return mongoTemplate.find(query, Note.class);
//    }
public List<Note> fetchNotesBasedOnCriteria(Map<String, List<String>> options) {
    MongoTemplate mongoTemplate = mongoTemplateFactory.getMongoTemplate();
    Criteria criteria = new Criteria();

    options.forEach((key, value) -> {
        if (value != null && !value.isEmpty()) {
            switch (key.toLowerCase()) {
                case "candidateid":
                    criteria.and("candidateId").in(value);
                    break;

                case "notetype":
                    criteria.and("noteType").in(value);
                    break;

                case "useremail":
                    // Step 1: Find Users whose email is in the list
                    List<User> users = mongoTemplate.find(
                            Query.query(Criteria.where("email").in(value)), User.class
                    );

                    // Step 2: Extract User IDs
                    List<String> userIds = users.stream()
                            .map(User::getId)  // Assuming `getId()` returns the ObjectId
                            .collect(Collectors.toList());

                    // Step 3: Match Notes where createdBy is in userIds
                    criteria.and("createdBy").in(userIds);
                    break;

                case "datecreated":
                    if (value.size() == 2) {
                        try {
                            Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(value.get(0));
                            Date toDate = new SimpleDateFormat("yyyy-MM-dd").parse(value.get(1));
                            criteria.and("dateCreated").gte(fromDate).lte(toDate);
                        } catch (ParseException e) {
                            throw new IllegalArgumentException("Invalid date format for 'dateCreated'. Use 'yyyy-MM-dd'.", e);
                        }
                    } else {
                        throw new IllegalArgumentException("'dateCreated' must contain exactly 2 values: [fromDate, toDate]");
                    }
                    break;

                default:
                    logger.warn("Unknown filter key: {}", key);
                    throw new RuntimeException("Key Specified was not included in query: " + key + "-" + value);
            }
        }
    });

    // Execute the final query
    Query query = new Query(criteria);
    return mongoTemplate.find(query, Note.class);
}

}
