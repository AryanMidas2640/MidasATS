package com.midas.consulting.dto.mapper.hrms;

import com.midas.consulting.controller.v1.request.hrms.CreateOrganisationRequest;
import com.midas.consulting.model.hrms.Organisation;
import com.midas.consulting.model.user.User;
import org.springframework.stereotype.Component;

/**
 * Created by Dheeraj Singh.
 */
@Component
public class OrganisationMapper {

    public static Organisation toOrganisation(CreateOrganisationRequest createOrganisationRequest, User user) {
        return new Organisation()
                .setId(createOrganisationRequest.getId())
                .setName(createOrganisationRequest.getOrganizationName())
                .setWebsite(createOrganisationRequest.getWebsite())
                .setUser(user);
    }

//    public static User toUser(UserDto user) {
//        return new User()
//                .setId(user.getId())
//                .setProfilePicture(user.getProfilePicture())
//                .setPassword(user.getPassword())
//                .setEmail(user.getEmail())
//                .setFirstName(user.getFirstName())
//                .setLastName(user.getLastName())
//                .setMobileNumber(user.getMobileNumber())
//
//                .setRoles(new HashSet<Role>(user
//                        .getRoles()
//                        .stream()
//                        .map(role -> new ModelMapper().map(role, Role.class))
//                        .collect(Collectors.toSet())));
//    }

}
