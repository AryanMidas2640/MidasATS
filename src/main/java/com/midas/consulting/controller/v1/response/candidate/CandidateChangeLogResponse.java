package com.midas.consulting.controller.v1.response.candidate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.midas.consulting.model.candidate.CandidateMidas;
import com.midas.consulting.model.hrms.ChangeLog;
import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;
@Setter
@Getter
@NoArgsConstructor
@Accessors
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateChangeLogResponse {

    private String userId;
    private User userDetails;
    private  List<CandidateMidas>  candidateMidas;
private List<ChangeLog> changeLogs;
}