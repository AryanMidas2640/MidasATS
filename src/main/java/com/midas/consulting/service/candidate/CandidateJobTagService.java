package com.midas.consulting.service.candidate;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.candidatetagging.CandidateJobTag;
import com.midas.consulting.model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CandidateJobTagService {
    @Autowired
    private MongoTemplateProvider mongoTemplateFactory;
    private String getTenantIdFromHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("X-Tenant");
        }
        throw new IllegalStateException("Tenant ID not found in the request header");
    }
    public CandidateJobTag tagCandidateToJob(String candidateId, String jobId, User taggedBy, String tagStatus) {
        CandidateJobTag tag = new CandidateJobTag();
        tag.setCandidateId(candidateId);
        tag.setJobId(jobId);
        tag.setTaggedBy(taggedBy);
        tag.setDateTime(LocalDateTime.now());
        tag.setTagStatus(tagStatus);
        return mongoTemplateFactory.getMongoTemplate().save(tag);
    }

    public List<CandidateJobTag> getTagsByCandidateId(String candidateId) {
        Query query = new Query(Criteria.where("candidateId").is(candidateId));
        return mongoTemplateFactory.getMongoTemplate().find(query, CandidateJobTag.class);
    }

    public List<CandidateJobTag> getTagsByJobId(String jobId) {
        Query query = new Query(Criteria.where("jobId").is(jobId));
        return mongoTemplateFactory.getMongoTemplate().find(query, CandidateJobTag.class);
    }

}
