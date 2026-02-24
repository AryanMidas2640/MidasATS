package com.midas.consulting.model;

import com.midas.consulting.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Getter
@Document(collection = "team")
public class Team {
    @Id
    private String id;
    @Indexed(unique = false, direction = IndexDirection.ASCENDING)
    private String name;
    private String description;
    @DBRef
    private CompanyDivision divisionId; // Reference to CompanyDivision
    private Boolean active;
    private Boolean crossView;  // reference for showing the cross view the team data to everyone
    private Date dateCreated;
    private Date dateModified;
    @DBRef
    private Set<User> userList;

}
