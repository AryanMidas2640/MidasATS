package com.midas.consulting.model.hrms;

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
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "employee")
public class Employee {
    @Id
    private String id;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String name;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String address;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String city;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String contactDetails;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private Date createDate;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String dob;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String email;

    @DBRef
    @Lazy
    private  Organisation organisation;

    private Date modifyDate;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String ssn;
    @Indexed(unique = true, direction = IndexDirection.DESCENDING)
    private String state;
    private Boolean status;
    @DBRef
    @Lazy
    private User user;
    private String zipCode;
    @DBRef
    @Lazy
    private List<Project> projects;
}
