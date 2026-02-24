package com.midas.consulting.service;

import com.midas.consulting.controller.v1.api.JwtAuthenticationController;
import com.midas.consulting.controller.v1.request.UserSignupRequest;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.model.user.Role;
import com.midas.consulting.model.user.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Created by Dheeraj Singh.
 */
public interface UserService {
    /**
     * Register a new user
     *
     * @param userDto
     * @return
     */
    UserDto signup(UserDto userDto);
    UserDto signup(UserSignupRequest userSignupRequest);
    ResponseEntity<?> confirmEmail(String confirmationToken);

    /**
     * Search an existing user
     *
     * @param email
     * @return
     */
    UserDto findUserByEmail(String email);

    /**
     * Update profile of the user
     *
     * @param userDto
     * @return
     */
    UserDto updateProfile(UserDto userDto);

    /**
     * Update password
     *
     * @param newPassword
     * @return
     */
    UserDto changePassword(UserDto userDto, String newPassword);

    List<Role> allRoles();

    List<User> allUsers();

    Object updatePassword(JwtAuthenticationController.PasswordResetRequest passwordResetRequest);

    UserDto findUserById(String id);

    List<User> getUserByRoll(String roleId);

    List<User> getUsersByManager(String managerId);

    User findUserObjectById(String id);

     List<UserDto> findManagersByUser(String id);

    UserDto getUserByEmail(String email);

    String getUserRole(String userId);
}
