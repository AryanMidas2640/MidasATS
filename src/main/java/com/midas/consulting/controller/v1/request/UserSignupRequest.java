package com.midas.consulting.controller.v1.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.midas.consulting.model.user.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

/**
 * Created by Dheeraj Singh.
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSignupRequest {
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String email;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String password;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String firstName;
    @NotEmpty(message = "{constraints.NotEmpty.message}")
    private String lastName;
    private String mobileNumber;
    private Boolean IsZoomUser;
    private String manager;
    private Set<Role> roles;
}