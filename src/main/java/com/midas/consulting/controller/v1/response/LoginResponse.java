package com.midas.consulting.controller.v1.response;

import com.midas.consulting.dto.model.user.UserDto;
import lombok.Data;

@Data
public class LoginResponse {
    private  String status;
    private  Object response;
    private UserDto user;
}

