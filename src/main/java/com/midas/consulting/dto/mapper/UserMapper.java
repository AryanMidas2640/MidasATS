package com.midas.consulting.dto.mapper;

import com.midas.consulting.dto.model.user.RoleDto;
import com.midas.consulting.dto.model.user.UserDto;
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
public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto()
                .setId(user.getId())
                .setPassword(user.getPassword())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setMobileNumber(user.getMobileNumber())
                .setPassword(user.getPassword())
                .setProfilePicture(user.getProfilePicture())
                .setActive(user.isActive())
                .setIsZoomUser(user.getIsZoomUser())
                .setRoles(new HashSet<RoleDto>(user
                        .getRoles()
                        .stream()
                        .map(role -> new ModelMapper().map(role, RoleDto.class))
                        .collect(Collectors.toSet())));
    }

    public static User toUser(UserDto user) {
        return new User()
                .setId(user.getId())
                .setProfilePicture(user.getProfilePicture())
                .setPassword(user.getPassword())
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setMobileNumber(user.getMobileNumber())
                .setIsZoomUser(user.getIsZoomUser())
                .setActive(user.isActive())
                .setRoles(new HashSet<Role>(user
                        .getRoles()
                        .stream()
                        .map(role -> new ModelMapper().map(role, Role.class))
                        .collect(Collectors.toSet())));
    }

}
