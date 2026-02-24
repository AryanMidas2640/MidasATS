package com.midas.consulting.controller.v1.response.candidate;

import com.midas.consulting.dto.model.user.UserDto;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
@Setter
@Data

@Accessors(chain = true)
@Getter
public class CandidateJobTagResponse {

    private String id;
    private String candidateId;
    private String jobId;
    private UserDto taggedBy;
    private LocalDateTime dateTime;
    private String tagStatus;

    public CandidateJobTagResponse(String id, String candidateId, String jobId, UserDto taggedBy, LocalDateTime dateTime, String tagStatus) {
        this.id = id;
        this.candidateId = candidateId;
        this.jobId = jobId;
        this.taggedBy = taggedBy;
        this.dateTime = dateTime;
        this.tagStatus = tagStatus;
    }
}