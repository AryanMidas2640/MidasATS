package com.midas.consulting.dto.mapper.hrms;

import com.midas.consulting.controller.v1.request.hrms.CreateEmployeeRequest;
import com.midas.consulting.model.hrms.Employee;
import com.midas.consulting.model.user.User;
import org.springframework.stereotype.Component;

/**
 * Created by Dheeraj Singh.
 */
@Component
public class EmployeeMapper {

    public static Employee toEmployee(CreateEmployeeRequest createEmployeeRequest, User user) {
        return new Employee()
                .setId(createEmployeeRequest.getId())
                .setName(createEmployeeRequest.getName())
                .setAddress(createEmployeeRequest.getAddress())
                .setUser(user)
                .setEmail(createEmployeeRequest.getEmail())
                .setCity(createEmployeeRequest.getCity())
                .setDob(createEmployeeRequest.getDob())
                .setSsn(createEmployeeRequest.getSsn())
                .setContactDetails(createEmployeeRequest.getContactDetails())
                .setZipCode(createEmployeeRequest.getZipCode())
                .setStatus(createEmployeeRequest.getStatus())
                .setState(createEmployeeRequest.getState());
//                .setPhone(createEmployeeRequest.getPhone())
//                .setEmail(createEmployeeRequest.getEmail())
//                .setContactPerson(createEmployeeRequest.getContactPerson());
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
