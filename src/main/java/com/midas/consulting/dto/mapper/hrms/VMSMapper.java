package com.midas.consulting.dto.mapper.hrms;

import com.midas.consulting.controller.v1.request.hrms.CreateVMSRequest;
import com.midas.consulting.dto.model.user.RoleDto;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.model.hrms.VMS;
import com.midas.consulting.model.user.Role;
import com.midas.consulting.model.user.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Created by Dheeraj Singh.
 */
@Component
public class VMSMapper {

    public static VMS toVMS(CreateVMSRequest createVMSRequest, User user) {
        return new VMS()
                .setId(createVMSRequest.getId())
                .setName(createVMSRequest.getName())
                .setUrl(createVMSRequest.getUrl())
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
