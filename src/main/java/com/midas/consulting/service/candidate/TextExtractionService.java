package com.midas.consulting.service.candidate;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.candidate.CandidateMidas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TextExtractionService {

    @Autowired
    private MongoTemplateProvider mongoTemplate;

    // Extract details where phone numbers are blank
    public List<String> extractDetailsPhoneBlank() {
        // Query to find candidates where the phone is blank
        Query query = new Query(Criteria.where("phone").is(""));
        List<CandidateMidas> candidatesMidas = mongoTemplate.getMongoTemplate().find(query, CandidateMidas.class);
        List<String> result = new ArrayList<>();

        candidatesMidas.forEach(x -> {
            CandidateMidas updatedCandidate = null;
            // Extract phone using regex
            String previousPhone = x.getPhone();
            String phone = extractPhoneNumber(x.getFullText());
            if (x.getPhone().equalsIgnoreCase("")) {
                x.setPhone(phone); // Update with the extracted phone number
                updatedCandidate = mongoTemplate.getMongoTemplate().save(x); // Save updated candidate
            }

            result.add(updatedCandidate.getId() + " | Previous phone: " + previousPhone + " | Updated phone: " + updatedCandidate.getPhone());
        });
        return result;
    }

    // Method to extract phone number using regex (international format)
    private String extractPhoneNumber(String text) {
        String phonePattern = "(\\+?\\d{1,3})?[\\s.-]?\\(?\\d{2,4}\\)?[\\s.-]?\\d{2,4}[\\s.-]?\\d{2,4}[\\s.-]?\\d{2,4}";
        Pattern pattern = Pattern.compile(phonePattern);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            // Return the first matched phone number
            return matcher.group();
        } else {
            System.out.println("Invalid phone number");
            return "Phone number not found";
        }
    }
}
