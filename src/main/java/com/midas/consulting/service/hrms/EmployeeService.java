package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.hrms.CreateEmployeeRequest;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.mapper.hrms.EmployeeMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.hrms.Employee;
import com.midas.consulting.model.hrms.EmployeeDocs;
import com.midas.consulting.model.hrms.Organisation;
import com.midas.consulting.model.hrms.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {

    private final MongoTemplateProvider mongoTemplate;

    @Autowired
    public EmployeeService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Employee> getAllEmployees() {
        Query query = new Query();
        return mongoTemplate.getMongoTemplate().find(query, Employee.class);
    }

    public Optional<Employee> getEmployeeById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        Employee employee = mongoTemplate.getMongoTemplate().findOne(query, Employee.class);
        return Optional.ofNullable(employee);
    }

    public Employee saveEmployee(CreateEmployeeRequest createEmployeeRequest, UserDto userDto) throws Exception {
        // Check if the employee with the same email already exists
        Query query = new Query(Criteria.where("email").is(createEmployeeRequest.getEmail()));
        Employee existingEmployee = mongoTemplate.getMongoTemplate().findOne(query, Employee.class);

        if (existingEmployee != null) {
            throw new MidasCustomException.DuplicateEntityException("Employee with the same email already exists");
        }

        // Get organisation by ID
        Optional<Organisation> organisation = Optional.ofNullable(
                mongoTemplate.getMongoTemplate().findById(createEmployeeRequest.getOrganizationId(), Organisation.class)
        );

        if (!organisation.isPresent()) {
            throw new MidasCustomException.DuplicateEntityException("Organisation not found");
        }

        // Get projects
        List<Project> projects = mongoTemplate.getMongoTemplate().find(
                new Query(Criteria.where("id").in(createEmployeeRequest.getProjects())),
                Project.class
        );

        Employee employeeToBeSaved = EmployeeMapper.toEmployee(createEmployeeRequest, UserMapper.toUser(userDto));
        employeeToBeSaved.setProjects(projects);
        employeeToBeSaved.setCreateDate(new Date());
        employeeToBeSaved.setModifyDate(new Date());
        employeeToBeSaved.setOrganisation(organisation.get());

        return mongoTemplate.getMongoTemplate().save(employeeToBeSaved);
    }

    public Employee updateEmployee(CreateEmployeeRequest createEmployeeRequest, UserDto userDto) throws Exception {
        // Find existing employee
        Query query = new Query(Criteria.where("id").is(createEmployeeRequest.getId()));
        Employee existingEmployee = mongoTemplate.getMongoTemplate().findOne(query, Employee.class);

        if (existingEmployee == null) {
            throw new MidasCustomException.DuplicateEntityException("Employee not found");
        }

        // Get organisation by ID
        Optional<Organisation> organisation = Optional.ofNullable(
                mongoTemplate.getMongoTemplate().findById(createEmployeeRequest.getOrganizationId(), Organisation.class)
        );

        if (!organisation.isPresent()) {
            throw new MidasCustomException.DuplicateEntityException("Organisation not found");
        }

        // Get updated projects
        List<Project> projects = mongoTemplate.getMongoTemplate().find(
                new Query(Criteria.where("id").in(createEmployeeRequest.getProjects())),
                Project.class
        );

        // Update employee details
        Employee employeeToBeUpdated = EmployeeMapper.toEmployee(createEmployeeRequest, UserMapper.toUser(userDto));
        employeeToBeUpdated.setModifyDate(new Date());
        employeeToBeUpdated.setProjects(projects);
        employeeToBeUpdated.setOrganisation(organisation.get());

        return mongoTemplate.getMongoTemplate().save(employeeToBeUpdated);
    }

    public void deleteEmployee(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.getMongoTemplate().remove(query, Employee.class);
    }

    public List<EmployeeDocs> getDocsByEmployeeId(String employeeId) {
        Query query = new Query(Criteria.where("employee.id").is(employeeId));
        return mongoTemplate.getMongoTemplate().find(query, EmployeeDocs.class);
    }
}
