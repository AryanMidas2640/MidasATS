package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.hrms.CreateProjectRequest;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.mapper.hrms.ProjectMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.hrms.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectsService {

    private final MongoTemplateProvider mongoTemplate;

    @Autowired
    public ProjectsService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Project> getAllProjects() {
        return mongoTemplate.getMongoTemplate().findAll(Project.class);
    }

    public Optional<Project> getProjectById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        Project project = mongoTemplate.getMongoTemplate().findOne(query, Project.class);
        return Optional.ofNullable(project);
    }

    public Project saveProject(CreateProjectRequest createProjectRequest, UserDto userDto) throws Exception {
        // Check if the project already exists
        Query projectQuery = new Query(Criteria.where("name").is(createProjectRequest.getName()));
        Project existingProject = mongoTemplate.getMongoTemplate().findOne(projectQuery, Project.class);

        // Check if Facility and Employee exist
        Query facilityQuery = new Query(Criteria.where("id").is(createProjectRequest.getFacilityId()));
        Facility facility = mongoTemplate.getMongoTemplate().findOne(facilityQuery, Facility.class);

        Query employeeQuery = new Query(Criteria.where("id").is(createProjectRequest.getEmployeeId()));
        Employee employee = mongoTemplate.getMongoTemplate().findOne(employeeQuery, Employee.class);

        if (existingProject == null && facility != null && employee != null) {
            Query organisationQuery = new Query(Criteria.where("id").is(createProjectRequest.getOrganisationId()));
            Organisation organisation = mongoTemplate.getMongoTemplate().findOne(organisationQuery, Organisation.class);

            Project projectToBeSaved = ProjectMapper.toProject(createProjectRequest, UserMapper.toUser(userDto));
            if (organisation != null) {
                projectToBeSaved.setOrganisation(organisation);
            }
            projectToBeSaved.setFacility(facility);

            if (createProjectRequest.getProjectType() == ProjectType.EXTENSION) {
                Query projectParentQuery = new Query(Criteria.where("id").is(createProjectRequest.getProjectId()));
                Project parentProject = mongoTemplate.getMongoTemplate().findOne(projectParentQuery, Project.class);

                if (parentProject != null) {
                    projectToBeSaved.setProject(parentProject);
                } else {
                    throw new MidasCustomException.EntityNotFoundException("The project was not found " + createProjectRequest.getProjectId());
                }
            }

            // Save the project
            Project savedProject = mongoTemplate.getMongoTemplate().save(projectToBeSaved);

            // Update the employee's projects
            Set<Project> projectsFromEmployee = employee.getProjects().stream().collect(Collectors.toSet());
            projectsFromEmployee.add(savedProject);
            employee.setProjects(projectsFromEmployee.stream().collect(Collectors.toList()));

            mongoTemplate.getMongoTemplate().save(employee);

            return savedProject;
        }

        throw new MidasCustomException.DuplicateEntityException("The project namely " + createProjectRequest.getName() + " is already present or invalid data provided!");
    }

    public Project updateProject(CreateProjectRequest createProjectRequest, UserDto userDto) throws Exception {
        Query projectQuery = new Query(Criteria.where("id").is(createProjectRequest.getId()));
        Project existingProject = mongoTemplate.getMongoTemplate().findOne(projectQuery, Project.class);

        Query facilityQuery = new Query(Criteria.where("id").is(createProjectRequest.getFacilityId()));
        Facility facility = mongoTemplate.getMongoTemplate().findOne(facilityQuery, Facility.class);

        Query organisationQuery = new Query(Criteria.where("id").is(createProjectRequest.getOrganisationId()));
        Organisation organisation = mongoTemplate.getMongoTemplate().findOne(organisationQuery, Organisation.class);

        if (existingProject != null && facility != null && organisation != null) {
            Project updatedProject = ProjectMapper.toProject(createProjectRequest, UserMapper.toUser(userDto));
            updatedProject.setId(existingProject.getId());
            updatedProject.setOrganisation(organisation);
            updatedProject.setFacility(facility);

            return mongoTemplate.getMongoTemplate().save(updatedProject);
        }

        throw new MidasCustomException.DuplicateEntityException("The project namely " + createProjectRequest.getName() + " is already present or invalid data provided!");
    }

    public void deleteProject(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.getMongoTemplate().remove(query, Project.class);
    }

    public List<Project> getProjectByEmployeeId(String employeeId) {
        Query employeeQuery = new Query(Criteria.where("id").is(employeeId));
        Employee employee = mongoTemplate.getMongoTemplate().findOne(employeeQuery, Employee.class);

        if (employee != null) {
            return employee.getProjects();
        }

        throw new MidasCustomException.EntityNotFoundException("Projects not found for employee " + employeeId);
    }
}
