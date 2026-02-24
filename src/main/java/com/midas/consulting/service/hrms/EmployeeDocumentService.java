package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.hrms.CreateEmployeeDocumentRequest;
import com.midas.consulting.controller.v1.request.hrms.CreateSubmissionDocumentRequest;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.hrms.Employee;
import com.midas.consulting.model.hrms.EmployeeDocs;
import com.midas.consulting.model.hrms.SubmissionDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeDocumentService {

    private final MongoTemplateProvider mongoTemplate;

    @Autowired
    public EmployeeDocumentService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<EmployeeDocs> getAllDocuments() {
        return mongoTemplate.getMongoTemplate().findAll(EmployeeDocs.class);
    }

    public Optional<EmployeeDocs> getDocumentById(String id) {
        return Optional.ofNullable(mongoTemplate.getMongoTemplate().findById(id, EmployeeDocs.class));
    }

    public EmployeeDocs saveDocument(CreateEmployeeDocumentRequest createEmployeeDocumentRequest, UserDto userDto) throws Exception {
        // Fetching employee
        Query employeeQuery = new Query(Criteria.where("id").is(createEmployeeDocumentRequest.getEmployeeId()));
        Employee employee = mongoTemplate.getMongoTemplate().findOne(employeeQuery, Employee.class);

        if (employee != null) {
            // Create and save EmployeeDocs
            EmployeeDocs employeeDocs = new EmployeeDocs();
            employeeDocs.setEmployee(employee)
                    .setUser(UserMapper.toUser(userDto))
                    .setType(createEmployeeDocumentRequest.getDocType())
                    .setExpiryDate(createEmployeeDocumentRequest.getExpiryDate())
                    .setDocName(createEmployeeDocumentRequest.getDocName())
                    .setDocDesc(createEmployeeDocumentRequest.getDocDesc())
                    .setDocStructure(createEmployeeDocumentRequest.getUploadDocStructure());

            return mongoTemplate.getMongoTemplate().save(employeeDocs);
        }

        throw new MidasCustomException.DuplicateEntityException("Employee not found or document already exists.");
    }

    public SubmissionDocument saveSubmissionDocument(CreateSubmissionDocumentRequest createEmployeeDocumentRequest, UserDto userDto) throws Exception {
        SubmissionDocument submissionDocument = new SubmissionDocument();

        // Populate submission document fields
        submissionDocument.setEmployeeId(userDto.getId())
                .setVmsName(createEmployeeDocumentRequest.getVmsName())
                .setSrcId(createEmployeeDocumentRequest.getSourceId())
                .setAchieveDate(createEmployeeDocumentRequest.getAchieveDate())
                .setDocType(createEmployeeDocumentRequest.getDocType())
                .setEmail(createEmployeeDocumentRequest.getEmail())
                .setAttachmentType(createEmployeeDocumentRequest.getAttachmentType())
                .setExpiryDate(createEmployeeDocumentRequest.getExpiryDate())
                .setDocName(createEmployeeDocumentRequest.getDocName())
                .setStatus(createEmployeeDocumentRequest.getStatus())
                .setComment(createEmployeeDocumentRequest.getComment())
                .setCompleteDate(createEmployeeDocumentRequest.getCompleteDate())
                .setMeetRequirements(createEmployeeDocumentRequest.getMeetRequirements())
                .setPassFailResponse(createEmployeeDocumentRequest.getPassFailResponse())
                .setCertificateNumber(createEmployeeDocumentRequest.getCertificateNumber())
                .setUploadDocStructure(createEmployeeDocumentRequest.getUploadDocStructure());

        return mongoTemplate.getMongoTemplate().save(submissionDocument);
    }

    public EmployeeDocs updateDocument(CreateEmployeeDocumentRequest createEmployeeDocumentRequest, UserDto userDto) throws Exception {
        // Fetch the document by ID
        Query query = new Query(Criteria.where("id").is(createEmployeeDocumentRequest.getId()));
        EmployeeDocs existingDocument = mongoTemplate.getMongoTemplate().findOne(query, EmployeeDocs.class);

        if (existingDocument != null) {
            // Fetch the employee
            Query employeeQuery = new Query(Criteria.where("id").is(createEmployeeDocumentRequest.getEmployeeId()));
            Employee employee = mongoTemplate.getMongoTemplate().findOne(employeeQuery, Employee.class);

            if (employee != null) {
                // Update the document
                existingDocument.setEmployee(employee)
                        .setUser(UserMapper.toUser(userDto))
                        .setType(createEmployeeDocumentRequest.getDocType())
                        .setExpiryDate(createEmployeeDocumentRequest.getExpiryDate())
                        .setDocStructure(createEmployeeDocumentRequest.getUploadDocStructure());

                return mongoTemplate.getMongoTemplate().save(existingDocument);
            }

            throw new MidasCustomException.DuplicateEntityException("Employee not found.");
        }

        throw new MidasCustomException.DuplicateEntityException("Document not found.");
    }

    public void deleteDocument(String id) {
        // Fetch and delete the document
        Query query = new Query(Criteria.where("id").is(id));
        EmployeeDocs document = mongoTemplate.getMongoTemplate().findOne(query, EmployeeDocs.class);

        if (document != null) {
            mongoTemplate.getMongoTemplate().remove(document);
        }
    }
}
