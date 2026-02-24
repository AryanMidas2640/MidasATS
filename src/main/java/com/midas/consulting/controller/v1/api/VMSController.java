package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.hrms.CreateVMSRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.VMSService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/vms")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class VMSController {


    private AuthenticationManager authenticationManager;


    private UserService userService;


    private VMSService vmsService;

    @Autowired
    public VMSController(AuthenticationManager authenticationManager, UserService userService, VMSService vmsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.vmsService = vmsService;
    }

    @GetMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllVMS(Principal principal) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            response.setPayload(vmsService.getAllVMS());
        } catch (Exception ee) {
            response.setErrors(ee.getMessage());
            response.setStatus(Response.Status.EXCEPTION);
            return response;
        }
        return response;//
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getVMSById(@PathVariable String id) {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK);
            response.setPayload(vmsService.getVMSById(id).orElse(null));
        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
        }
        return response;
    }

    @PostMapping()
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response saveVMS(@RequestBody CreateVMSRequest createVMSRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK).setPayload(vmsService.saveVMS(createVMSRequest, userService.findUserByEmail(principal.getName())));

        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return response;
        }
        return response;
    }

    @PatchMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response updateVMS(@RequestBody CreateVMSRequest createVMSRequest, Principal principal) throws Exception {
        Response response = new Response();
        try {
            response.setStatus(Response.Status.OK).setPayload(vmsService.updateVMS(createVMSRequest, userService.findUserByEmail(principal.getName())));

        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return response;
        }
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteVMS(@PathVariable String id) {
        Response response = new Response();
        try {
            vmsService.deleteVMS(id);
            response.setPayload("VMS with " + id + "deleted successfully");
            response.setStatus(Response.Status.OK);
        } catch (Exception ee) {
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return response;
        }
        return response;
    }
}