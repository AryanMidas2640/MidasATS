package com.midas.consulting.controller.v1.api;

import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.ProjectsService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/pipeline")
@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class PipelineController {
    private AuthenticationManager authenticationManager;
    private UserService userService;
    private ProjectsService projectsService;


    @Autowired
    public PipelineController(AuthenticationManager authenticationManager, UserService userService, ProjectsService projectsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.projectsService = projectsService;
    }
}
