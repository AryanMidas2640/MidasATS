package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.hrms.CreateFacilityRequest;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.mapper.hrms.FacilityMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.hrms.Client;
import com.midas.consulting.model.hrms.Facility;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FacilityService {

    private final MongoTemplateProvider mongoTemplate;

    @Autowired
    public FacilityService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Facility> getAllFacilities() {
        log.info("Fetching all facilities");
        return mongoTemplate.getMongoTemplate().findAll(Facility.class);
    }

    public Optional<Facility> getFacilityById(String id) {
        log.info("Fetching facility by id: {}", id);
        return Optional.ofNullable(mongoTemplate.getMongoTemplate().findById(id, Facility.class));
    }

    public List<Facility> getFacilitiesByClientId(String clientId) {
        log.info("Fetching facilities for client id: {}", clientId);
//        Query query = new Query(Criteria.where("parentClient.$id").is(clientId));
        Query query = new Query(
                Criteria.where("parentClient.$id").is(new ObjectId(clientId))
        );
        return mongoTemplate.getMongoTemplate().find(query, Facility.class);
    }

    public Facility saveFacility(CreateFacilityRequest createFacilityRequest, UserDto userDto) throws Exception {
        log.info("Saving new facility: {}", createFacilityRequest.getFacilityName());

        // Validate parent client exists
        Client parentClient = mongoTemplate.getMongoTemplate().findById(
                createFacilityRequest.getParentClientID(),
                Client.class
        );

        if (parentClient == null) {
            log.error("Parent client not found with id: {}", createFacilityRequest.getParentClientID());
            throw new MidasCustomException.DuplicateEntityException(
                    "Parent client not found with id: " + createFacilityRequest.getParentClientID()
            );
        }

        // Map the incoming request and save the facility
        Facility facilityToBeSaved = FacilityMapper.toFacility(createFacilityRequest, UserMapper.toUser(userDto));
        facilityToBeSaved.setParentClient(parentClient);
        facilityToBeSaved.setCreatedBy(userDto.getEmail());
        facilityToBeSaved.setDateCreated(LocalDateTime.now());
        facilityToBeSaved.setDateModified(LocalDateTime.now());

        Facility savedFacility = mongoTemplate.getMongoTemplate().save(facilityToBeSaved);
        log.info("Facility saved successfully with id: {}", savedFacility.getId());
        return savedFacility;
    }

    public Facility updateFacility(CreateFacilityRequest createFacilityRequest, UserDto userDto) throws Exception {
        log.info("Updating facility with id: {}", createFacilityRequest.getId());

        if (createFacilityRequest.getId() == null || createFacilityRequest.getId().isEmpty()) {
            log.error("Facility ID is required for update");
            throw new MidasCustomException.DuplicateEntityException("Facility ID is required for update");
        }

        // Fetch existing facility
        Facility existingFacility = mongoTemplate.getMongoTemplate().findById(
                createFacilityRequest.getId(),
                Facility.class
        );

        if (existingFacility == null) {
            log.error("Facility not found with id: {}", createFacilityRequest.getId());
            throw new MidasCustomException.DuplicateEntityException(
                    "Facility not found with id: " + createFacilityRequest.getId()
            );
        }

        // Validate parent client exists
        Client parentClient = mongoTemplate.getMongoTemplate().findById(
                createFacilityRequest.getParentClientID(),
                Client.class
        );

        if (parentClient == null) {
            log.error("Parent client not found with id: {}", createFacilityRequest.getParentClientID());
            throw new MidasCustomException.DuplicateEntityException(
                    "Parent client not found with id: " + createFacilityRequest.getParentClientID()
            );
        }

        // Map the incoming request and update the facility
        Facility facilityToUpdate = FacilityMapper.toFacility(createFacilityRequest, UserMapper.toUser(userDto));
        facilityToUpdate.setId(existingFacility.getId());
        facilityToUpdate.setParentClient(parentClient);
        facilityToUpdate.setCreatedBy(existingFacility.getCreatedBy());
        facilityToUpdate.setDateCreated(existingFacility.getDateCreated());
        facilityToUpdate.setDateModified(LocalDateTime.now());

        Facility updatedFacility = mongoTemplate.getMongoTemplate().save(facilityToUpdate);
        log.info("Facility updated successfully with id: {}", updatedFacility.getId());
        return updatedFacility;
    }

    public void deleteFacility(String id) throws Exception {
        log.info("Deleting facility with id: {}", id);

        Facility facility = mongoTemplate.getMongoTemplate().findById(id, Facility.class);

        if (facility == null) {
            log.error("Facility not found with id: {}", id);
            throw new MidasCustomException.DuplicateEntityException(
                    "Facility not found with id: " + id
            );
        }

        mongoTemplate.getMongoTemplate().remove(facility);
        log.info("Facility deleted successfully with id: {}", id);
    }
}