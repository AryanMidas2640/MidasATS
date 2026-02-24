package com.midas.consulting.dto.mapper;

import com.midas.consulting.dto.model.user.RoleDto;
import com.midas.consulting.model.user.Role;
import org.springframework.stereotype.Component;

/**
 * Created by Dheeraj Singh.
 */
@Component
public class RoleMapper {

    public static RoleDto toRoleDto(Role role) {
        return new RoleDto()
                .setRole(role.getRole())
                .setId(role.getId());
    }

    public static Role toRole(RoleDto role) {
        return new Role()
                .setRole(role.getRole())
                .setId(role.getId());
    }
}
