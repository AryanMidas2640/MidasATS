package com.midas.consulting.controller.v1.request.candidate;

import com.textkernel.tx.models.resume.skills.ResumeV2Skills;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
public class CandidateSearchRequest {
    private String email;
    private String phone;
    private String city;
    private String state;
    private String title;
    private List<ResumeV2Skills> includeSkills;
    private List<ResumeV2Skills>  excludeSkills;
}