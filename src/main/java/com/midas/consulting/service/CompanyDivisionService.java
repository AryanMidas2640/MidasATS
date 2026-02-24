package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.CompanyDivision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CompanyDivisionService {

    private final MongoTemplateProvider mongoTemplate;

    @Autowired
    public CompanyDivisionService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public CompanyDivision createDivision(CompanyDivision division) {
        division.setDateCreated(new Date());
//        division.setDateModified(new Date());
      return   mongoTemplate.getMongoTemplate().save(division);  // Save the division using MongoTemplate
    }

    public List<CompanyDivision> getAllDivisions() {
     return    mongoTemplate.getMongoTemplate().findAll(CompanyDivision.class);  // Get all divisions
    }

    public Optional<CompanyDivision> getDivisionById(String id) {
        Query query = new Query(Criteria.where("id").is(id));  // Find division by ID
        CompanyDivision division =mongoTemplate.getMongoTemplate().findOne(query, CompanyDivision.class);
        return Optional.ofNullable(division);  // Return wrapped in Optional
    }

    public CompanyDivision updateDivision(String id, CompanyDivision division) {
        Query query = new Query(Criteria.where("id").is(id));  // Query by ID
        CompanyDivision existingDivision =mongoTemplate.getMongoTemplate().findOne(query, CompanyDivision.class);

        if (existingDivision != null) {
            existingDivision.setName(division.getName())
                    .setDescription(division.getDescription())
                    .setCode(division.getCode())
                    .setActive(division.getActive())
                    .setDateModified(new Date());
          return   mongoTemplate.getMongoTemplate().save(existingDivision);  // Save the updated division
        } else {
            throw new RuntimeException("Division not found");
        }
    }

    public void deleteDivision(String id) {
        Query query = new Query(Criteria.where("id").is(id));  // Query by ID
       mongoTemplate.getMongoTemplate().remove(query, CompanyDivision.class);  // Remove division by ID
    }
}
