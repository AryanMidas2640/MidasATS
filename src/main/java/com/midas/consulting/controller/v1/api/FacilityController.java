package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.hrms.CreateFacilityRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.FacilityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/facility")
@Api(value = "facility-management", description = "Operations for Facility management")
@Slf4j
public class FacilityController {

    private final UserService userService;
    private final FacilityService facilityService;

    @Autowired
    public FacilityController(UserService userService, FacilityService facilityService) {
        this.userService = userService;
        this.facilityService = facilityService;
    }

    @GetMapping
    @ResponseBody
    @ApiOperation(value = "Get all facilities", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllFacilities(Principal principal) {
        Response response = new Response();
        try {
            log.info("Fetching all facilities for user: {}", principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(facilityService.getAllFacilities());
        } catch (Exception e) {
            log.error("Error fetching all facilities", e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "Get facility by ID", authorizations = {@Authorization(value = "apiKey")})
    public Response getFacilityById(@PathVariable String id, Principal principal) {
        Response response = new Response();
        try {
            log.info("Fetching facility with id: {} for user: {}", id, principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(facilityService.getFacilityById(id)
                            .orElseThrow(() -> new Exception("Facility not found with id: " + id)));
        } catch (Exception e) {
            log.error("Error fetching facility with id: {}", id, e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @GetMapping("/client/{clientId}")
    @ResponseBody
    @ApiOperation(value = "Get facilities by client ID", authorizations = {@Authorization(value = "apiKey")})
    public Response getFacilitiesByClientId(@PathVariable String clientId, Principal principal) {
        Response response = new Response();
        try {
            log.info("Fetching facilities for client id: {} by user: {}", clientId, principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(facilityService.getFacilitiesByClientId(clientId));
        } catch (Exception e) {
            log.error("Error fetching facilities for client id: {}", clientId, e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @PostMapping
    @ResponseBody
    @ApiOperation(value = "Create a new facility", authorizations = {@Authorization(value = "apiKey")})
    public Response saveFacility(@Valid @RequestBody CreateFacilityRequest createFacilityRequest, 
                                Principal principal) {
        Response response = new Response();
        try {
            log.info("Creating new facility: {} by user: {}", 
                    createFacilityRequest.getFacilityName(), principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(facilityService.saveFacility(createFacilityRequest, 
                            userService.findUserByEmail(principal.getName())));
        } catch (Exception e) {
            log.error("Error creating facility", e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @PatchMapping
    @ResponseBody
    @ApiOperation(value = "Update an existing facility", authorizations = {@Authorization(value = "apiKey")})
    public Response updateFacility(@Valid @RequestBody CreateFacilityRequest createFacilityRequest, 
                                  Principal principal) {
        Response response = new Response();
        try {
            log.info("Updating facility with id: {} by user: {}", 
                    createFacilityRequest.getId(), principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(facilityService.updateFacility(createFacilityRequest, 
                            userService.findUserByEmail(principal.getName())));
        } catch (Exception e) {
            log.error("Error updating facility with id: {}", createFacilityRequest.getId(), e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "Delete a facility", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteFacility(@PathVariable String id, Principal principal) {
        Response response = new Response();
        try {
            log.info("Deleting facility with id: {} by user: {}", id, principal.getName());
            facilityService.deleteFacility(id);
            response.setStatus(Response.Status.OK)
                    .setPayload("Facility with id " + id + " deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting facility with id: {}", id, e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }
}