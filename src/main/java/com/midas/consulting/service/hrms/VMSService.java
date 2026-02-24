package com.midas.consulting.service.hrms;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.request.hrms.CreateVMSRequest;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.mapper.hrms.VMSMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.hrms.VMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VMSService {

    private MongoTemplateProvider mongoTemplate;

    @Autowired
    public VMSService(MongoTemplateProvider mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<VMS> getAllVMS() {
        return mongoTemplate.getMongoTemplate().findAll(VMS.class);
    }

    public Optional<VMS> getVMSById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        VMS vms = mongoTemplate.getMongoTemplate().findOne(query, VMS.class);
        return Optional.ofNullable(vms);
    }

    public VMS saveVMS(CreateVMSRequest vmsRequest, UserDto userDto) throws Exception {
        Query query = new Query(Criteria.where("name").is(vmsRequest.getName()));
        VMS existingVMS = mongoTemplate.getMongoTemplate().findOne(query, VMS.class);

        if (existingVMS == null) {
            VMS vms = VMSMapper.toVMS(vmsRequest, UserMapper.toUser(userDto));
            return mongoTemplate.getMongoTemplate().save(vms);
        }
        throw new MidasCustomException.DuplicateEntityException("The VMS namely " + vmsRequest.getName() + " is already present!");
    }

    public VMS updateVMS(CreateVMSRequest vmsRequest, UserDto userDto) throws Exception {
        Query query = new Query(Criteria.where("id").is(vmsRequest.getId()));
        VMS existingVMS = mongoTemplate.getMongoTemplate().findOne(query, VMS.class);

        if (existingVMS != null) {
            VMS vmsToUpdate = VMSMapper.toVMS(vmsRequest, UserMapper.toUser(userDto));
            vmsToUpdate.setId(existingVMS.getId());  // Ensure the existing ID is kept
            return mongoTemplate.getMongoTemplate().save(vmsToUpdate);
        }
        throw new MidasCustomException.DuplicateEntityException("The VMS namely " + vmsRequest.getName() + " is already present!");
    }

    public void deleteVMS(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.getMongoTemplate().remove(query, VMS.class);
    }
}
