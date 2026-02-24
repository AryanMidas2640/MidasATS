package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.hrms.CreateOrganisationRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.model.hrms.Organisation;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.OrganizationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/organisation")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class OrganisationController {


    private AuthenticationManager authenticationManager;


    private UserService userService;




    private OrganizationService organizationService;
    @Autowired
    public OrganisationController(AuthenticationManager authenticationManager, UserService userService, OrganizationService organizationService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.organizationService = organizationService;
    }

    @GetMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllOrganisations() {
        Response response = new Response();
        try {
            List<Organisation> organisations = organizationService.getAllOrganisations();
            response.setStatus(Response.Status.OK);
            response.setPayload(organisations);
        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response<Organisation> getOrganisationById(@PathVariable String id) {
        Response response = new Response();
        try{
            Optional<Organisation> organisation = organizationService.getOrganisationById(id);
            if (organisation.isPresent()){
                response.setPayload(organisation.get());
                response.setStatus(Response.Status.OK);
                return  response ;
            }
            response.setErrors("The Organization you are looking for was not found");
            response.setStatus(Response.Status.NOT_FOUND);
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return  response;
        }
        return  response ;
    }


    @PostMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response<Organisation> saveOrganisation(@RequestBody CreateOrganisationRequest createOrganisationRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            createOrganisationRequest.setId("");
            Organisation organisation = organizationService.saveOrganisation(createOrganisationRequest, userService.findUserByEmail(principal.getName()));
            if (organisation != null) {
                response.setStatus(Response.Status.OK);
                response.setPayload(organisation);
                return response;
            }
            response.setStatus(Response.Status.NOT_FOUND);
            response.setErrors("There were some issue in saving the organization");
        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
        }
        return response;
    }


    @PatchMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response updateOrganisation(@RequestBody CreateOrganisationRequest createOrganisationRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            Organisation organisation = organizationService.updateOrganisation(createOrganisationRequest, userService.findUserByEmail(principal.getName()));

            if (organisation != null) {
                response.setStatus(Response.Status.OK);
                response.setPayload(organisation);
                return response;
            }
            response.setStatus(Response.Status.NOT_FOUND);
            response.setErrors("There were some issue in updating the organization");
        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;
    }

    @DeleteMapping("/{id}")

    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteOrganisation(@PathVariable String id) {
        Response response = new Response();
        try{
            organizationService.deleteOrganisation(id);
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION).setErrors(ee.getMessage());
            return  response;
        }
        response.setStatus(Response.Status.OK).setPayload("Organization deleted successfully !");
        return response;
    }

}
