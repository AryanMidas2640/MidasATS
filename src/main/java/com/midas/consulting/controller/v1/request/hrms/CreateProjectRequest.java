package com.midas.consulting.controller.v1.request.hrms;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.midas.consulting.model.hrms.*;
import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateProjectRequest {
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String employeeId;
    private String id;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Date startDate;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Date endDate;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String designation;
    private Boolean status = false;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String name;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String facilityId;
    private float guaranteeHours;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String occupationType;
    private String organisationId;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Double billRates;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private ProjectType projectType;
    private String projectId;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Double overTimeRates;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Double payRates;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Double preDeim;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private Double travelAllowance;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String projectStatus;
    private List<TimeSheet> timeSheets;
}
