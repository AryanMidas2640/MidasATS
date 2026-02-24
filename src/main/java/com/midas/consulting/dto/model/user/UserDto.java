package com.midas.consulting.dto.model.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.midas.consulting.model.user.UserType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Set;

/**
 * Created by Dheeraj Singh.
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@ToString
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {

    private String id;
    private String profilePicture;
    private boolean isActive;
    private String email;
    private UserType userType;
    private String password;
    private String manager;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private Boolean isZoomUser;
    private Set<RoleDto> roles;
    public String getFullName() {
        return firstName != null ? firstName.concat(" ").concat(lastName) : "";
    }
}
