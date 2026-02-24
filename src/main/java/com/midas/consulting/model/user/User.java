package com.midas.consulting.model.user;

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

/**
 * Created by Dheeraj Singh.
 */
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "user")
public class User {
    @Id
    private String id;
    private String profilePicture;
    private boolean isActive;
    @Indexed(unique = true, direction = IndexDirection.ASCENDING)
    private String email;
    private UserType userType;
    private String password;
    @DBRef
    private User manager;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    @DBRef
    private Set<Role> roles;
    private Boolean isZoomUser;
    private Date dateCreated;
    private Date dateModified;
    public String getFullName() {
        return firstName != null ? firstName.concat(" ").concat(lastName) : "";
    }
}

