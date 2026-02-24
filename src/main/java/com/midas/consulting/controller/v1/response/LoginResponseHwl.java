package com.midas.consulting.controller.v1.response;

import lombok.Data;

import java.time.LocalDate;
@Data
public class LoginResponseHwl {
    private String access_token;
    private String instance_url;
    private String id;
    private String token_type;
    private LocalDate issued_at;
    private String signature;

}