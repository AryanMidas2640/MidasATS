package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.hrms.CreateProjectRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.ProjectsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/project")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class ProjectsController {


    private AuthenticationManager authenticationManager;


    private UserService userService;


    private ProjectsService projectsService;


    @Autowired
    public ProjectsController(AuthenticationManager authenticationManager, UserService userService, ProjectsService projectsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.projectsService = projectsService;
    }

    @GetMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllProjects(Principal principal) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            response.setPayload(projectsService.getAllProjects());
        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
        }
        return response;//
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getProjectById(@PathVariable String id) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            response.setPayload(projectsService.getProjectById(id).orElse(null));
        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }

    @GetMapping("/getProjectByEmployeeId/{employeeId}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getProjectByEmployeeId(@PathVariable String employeeId) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            response.setPayload(projectsService.getProjectByEmployeeId(employeeId));
        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return response;
        }
        return response;
    }

    @PostMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response saveProject(@RequestBody CreateProjectRequest createProjectRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK).setPayload(projectsService.saveProject(createProjectRequest, userService.findUserByEmail(principal.getName())));

        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
        }
        return response;
    }

    @PatchMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response updateProject(@RequestBody CreateProjectRequest createProjectRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK).setPayload(projectsService.updateProject(createProjectRequest, userService.findUserByEmail(principal.getName())));
        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
        }
        return response;
    }

    //
    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteVMS(@PathVariable String id) {
        Response response = new Response();
        try {
            projectsService.deleteProject(id);
            response.setPayload("Project with " + id + "deleted successfully");
            response.setStatus(Response.Status.OK);

        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }
//
}
