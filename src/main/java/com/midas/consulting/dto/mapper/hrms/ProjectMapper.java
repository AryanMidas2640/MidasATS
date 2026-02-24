package com.midas.consulting.dto.mapper.hrms;

import com.midas.consulting.controller.v1.request.hrms.CreateProjectRequest;
import com.midas.consulting.model.hrms.Project;
import com.midas.consulting.model.user.User;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Dheeraj Singh.
 */
@Component
public class ProjectMapper {

    public static Project toProject(CreateProjectRequest createProjectRequest, User user) {
        return new Project()
                .setId(createProjectRequest.getId())
                .setName(createProjectRequest.getName())
                .setUser(user)
                .setCreateDate(new Date())
                .setProjectStatus(createProjectRequest.getProjectStatus())
                .setProjectType(createProjectRequest.getProjectType())
                .setOccupationType(createProjectRequest.getOccupationType())
                .setDesignation(createProjectRequest.getDesignation())
                .setEndDate(createProjectRequest.getEndDate())
                .setStartDate(createProjectRequest.getStartDate())
                .setStatus(createProjectRequest.getStatus())
                .setBillRates(createProjectRequest.getBillRates())
                .setBillRates(createProjectRequest.getBillRates())
                .setPayRates(createProjectRequest.getPayRates())

                .setGuaranteeHours(createProjectRequest.getGuaranteeHours())
                .setOverTimeRates(createProjectRequest.getOverTimeRates())
                .setPreDeim(createProjectRequest.getPreDeim())
                .setTravelAllowance(createProjectRequest.getTravelAllowance())
                .setTimeSheets(createProjectRequest.getTimeSheets());
    }


}
