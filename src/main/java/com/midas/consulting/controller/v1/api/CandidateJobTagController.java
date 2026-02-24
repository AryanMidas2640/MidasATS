package com.midas.consulting.controller.v1.api;

//import com.midas.consulting.config.database.MultiTenantMongoTemplateFactory;
import com.midas.consulting.controller.v1.response.candidate.CandidateJobTagResponse;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.model.candidatetagging.CandidateJobTag;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.candidate.CandidateJobTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/candidateJobTags")

@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")


@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class CandidateJobTagController {


    private CandidateJobTagService candidateJobTagService;
    private UserService userService;



    @Autowired

    public  CandidateJobTagController(UserService userService,CandidateJobTagService candidateJobTagService){
        this.candidateJobTagService=candidateJobTagService;
        this.userService = userService;

    }




    @PostMapping("/tag")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public CandidateJobTagResponse tagCandidateToJob(Principal principal, @RequestParam String candidateId,
                                                     @RequestParam String jobId,
                                                     @RequestParam String taggedBy,
                                                     @RequestParam String tagStatus) {
        CandidateJobTag tag = candidateJobTagService.tagCandidateToJob(candidateId, jobId, UserMapper.toUser( userService.findUserByEmail(principal.getName())), tagStatus);
        return new CandidateJobTagResponse(
               tag.getId(), tag.getCandidateId(), tag.getJobId(),  userService.findUserByEmail(principal.getName()), tag.getDateTime(), tag.getTagStatus());
    }

    @GetMapping("/candidate/{candidateId}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public List<CandidateJobTagResponse> getTagsByCandidateId(Principal principal,@PathVariable String candidateId) {


        return candidateJobTagService.getTagsByCandidateId(candidateId)
                .stream()
                .map(tag -> new CandidateJobTagResponse(tag.getId(),tag.getCandidateId(), tag.getJobId(),  userService.findUserByEmail(principal.getName()), tag.getDateTime(), tag.getTagStatus()))
                .collect(Collectors.toList());
    }

    @GetMapping("/job/{jobId}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public List<CandidateJobTagResponse> getTagsByJobId(Principal principal, @PathVariable String jobId) {
        return candidateJobTagService.getTagsByJobId(jobId)
                .stream()
                .map(tag -> new CandidateJobTagResponse(tag.getId(),tag.getCandidateId(), tag.getJobId(),  userService.findUserByEmail(principal.getName()), tag.getDateTime(), tag.getTagStatus()))
                .collect(Collectors.toList());
    }
}
