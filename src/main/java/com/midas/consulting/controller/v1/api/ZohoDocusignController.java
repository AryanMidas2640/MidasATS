//package com.midas.consulting.controller.v1.api;
//
//import com.midas.consulting.controller.v1.request.hrms.CreateFacilityRequest;
//import com.midas.consulting.service.UserService;
//import com.midas.consulting.service.hrms.FacilitiyService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.Authorization;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.web.bind.annotation.*;
//
//import java.security.Principal;
//
//@RestController
//@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
//@RequestMapping("/api/v1/zoho-docusign")
//
//@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
//public class ZohoDocusignController {
//
//
//    private AuthenticationManager authenticationManager;
//
//    private UserService userService;
//
//
//
//    private FacilitiyService facilitiyService;
//    @Autowired
//    public ZohoDocusignController(AuthenticationManager authenticationManager, UserService userService, FacilitiyService facilitiyService) {
//        this.authenticationManager = authenticationManager;
//        this.userService = userService;
//        this.facilitiyService = facilitiyService;
//    }
//
//    @GetMapping
//    @ResponseBody
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public Response getAllFacilities(Principal principal) {
//        Response response = new Response();
//        try {
//            response.setStatus(Response.Status.OK).setPayload(facilitiyService.getAllFacilities());
//        } catch (Exception ee) {
//            response.setStatus(Response.Status.EXCEPTION);
//            response.setErrors(ee.getMessage());
//        }
//        return response;
//    }
//
//    @GetMapping("/{id}")
//    @ResponseBody
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public Response getFacilityById(@PathVariable String id) {
//        Response response = new Response();
//        try {
//            response.setStatus(Response.Status.OK)
//                    .setPayload(facilitiyService.getFacilityById(id).orElse(null));
//        } catch (Exception ee) {
//            response.setStatus(Response.Status.EXCEPTION);
//            response.setErrors(ee.getMessage());
//        }
//        return response;
//    }
//
//    @PostMapping
//    @ResponseBody
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public Response saveFacility(@RequestBody CreateFacilityRequest createFacilityRequest, Principal principal) throws Exception {
//        Response response
//                = new Response();
//        try {
//            response.setStatus(Response.Status.OK).setPayload((facilitiyService.saveFacility(createFacilityRequest, userService.findUserByEmail(principal.getName()))));
//        } catch (Exception ee) {
//            response.setErrors(ee.getMessage());
//            response.setStatus(Response.Status.EXCEPTION);
//            return response;
//        }
//        return response;
//    }
//
//    @PatchMapping
//    @ResponseBody
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public Response updateFacility(@RequestBody CreateFacilityRequest createOrganisationRequest, Principal principal) throws Exception {
//        Response response = new Response();
//        try {
//            response.setStatus(Response.Status.OK).setPayload(facilitiyService.updateFacility(createOrganisationRequest, userService.findUserByEmail(principal.getName())));
//        } catch (Exception ee) {
//            response.setStatus(Response.Status.EXCEPTION);
//            response.setErrors(ee.getMessage());
//            return response;
//        }
//        return response;
//    }
//
//    @DeleteMapping("/{id}")
//    @ResponseBody
//    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
//    public Response deleteFacility(@PathVariable String id) {
//        Response response = new Response();
//        try {
//            facilitiyService.deleteFacility(id);
//            response.setStatus(Response.Status.OK).setPayload("Faclility with id " + id + " deleted successfully");
//        } catch (Exception ee) {
//            response.setErrors(ee.getMessage());
//            response.setStatus(Response.Status.EXCEPTION);
//            return response;
//        }
//        return response;
//    }
//
//}
//
