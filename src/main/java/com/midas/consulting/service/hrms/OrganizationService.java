package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.hrms.CreateOrganisationRequest;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.mapper.hrms.OrganisationMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.hrms.Organisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrganizationService {

    private final MongoTemplateProvider mongoTemplate;

    @Autowired
    public OrganizationService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Organisation> getAllOrganisations() {
        return mongoTemplate.getMongoTemplate().findAll(Organisation.class);
    }

    public Optional<Organisation> getOrganisationById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        Organisation organisation = mongoTemplate.getMongoTemplate().findOne(query, Organisation.class);
        return Optional.ofNullable(organisation);
    }

    public Organisation saveOrganisation(CreateOrganisationRequest createOrganisationRequest, UserDto userDto) throws Exception {
        // Check if the organisation with the same name already exists
        Query query = new Query(Criteria.where("organizationName").is(createOrganisationRequest.getOrganizationName()));
        Organisation existingOrganisation = mongoTemplate.getMongoTemplate().findOne(query, Organisation.class);

        if (existingOrganisation != null) {
            throw new MidasCustomException.DuplicateEntityException("The Client namely " + createOrganisationRequest.getOrganizationName() + " is already present as " + existingOrganisation + " provided does not exist !");
        }

        Organisation organisationToBeSaved = OrganisationMapper.toOrganisation(createOrganisationRequest, UserMapper.toUser(userDto));
        return mongoTemplate.getMongoTemplate().save(organisationToBeSaved);
    }

    public Organisation updateOrganisation(CreateOrganisationRequest createOrganisationRequest, UserDto userDto) throws Exception {
        // Find existing organisation by ID
        Query query = new Query(Criteria.where("id").is(createOrganisationRequest.getId()));
        Organisation existingOrganisation = mongoTemplate.getMongoTemplate().findOne(query, Organisation.class);

        if (existingOrganisation == null) {
            throw new MidasCustomException.DuplicateEntityException("The Organisation namely " + createOrganisationRequest.getOrganizationName() + " does not exist.");
        }

        Organisation updatedOrganisation = OrganisationMapper.toOrganisation(createOrganisationRequest, UserMapper.toUser(userDto));
        updatedOrganisation.setId(existingOrganisation.getId()); // Preserve the existing ID

        return mongoTemplate.getMongoTemplate().save(updatedOrganisation);
    }

    public void deleteOrganisation(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.getMongoTemplate().remove(query, Organisation.class);
    }
}
