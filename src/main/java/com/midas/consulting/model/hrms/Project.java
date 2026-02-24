package com.midas.consulting.model.hrms;

import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;
import java.util.Date;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "project")
public class Project {
    @Id
    private String id;
    private String projectStatus;
    private Date startDate;
    private Date endDate;
    private String designation;
    private Boolean status;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String name;
    @DBRef
    @Lazy
    private Facility facility;
    private float guaranteeHours;
    private String occupationType;


    private Double billRates;

    private ProjectType projectType;
    @DBRef
    @Lazy
    private Project project;
    private Date createDate;
    private Date modifyDate;
    private Double overTimeRates;
    private Double payRates;
    private Double preDeim;

    @DBRef
    private  Organisation organisation;
    private Double travelAllowance;

    @DBRef
    @Lazy
    private User user;
    private Boolean getStatus;
    @Lazy
    @DBRef
    private List<TimeSheet> timeSheets;
}