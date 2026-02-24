package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.hrms.CreateClientRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.ClientsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@RequestMapping("/api/v1/zoom")

@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class ZoomCommunicationController {

//    @Autowired
    private AuthenticationManager authenticationManager;

//    @Autowired
    private UserService userService;


    private ClientsService clientsService;
    @Autowired
    public ZoomCommunicationController(AuthenticationManager authenticationManager, UserService userService, ClientsService clientsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.clientsService = clientsService;
    }

    @GetMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllClients(Principal principal) {
        Response response = new Response();
        response.setStatus(Response.Status.OK).setPayload(clientsService.getAllClients());
        return response;
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response getClientById(@PathVariable String id) {
        Response response = new Response();
        response.setStatus(Response.Status.OK).setPayload(clientsService.getClientById(id).orElse(null));
        return response;
    }

    @PostMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response saveClient(@RequestBody CreateClientRequest createClientRequest, Principal principal) throws Exception {
        Response response = new Response();
        try{
            response.setStatus(Response.Status.OK).setPayload(clientsService.saveClient(createClientRequest, userService.findUserByEmail(principal.getName())));
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
        }
        return response;
    }

    @PatchMapping
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response updateClient(@RequestBody CreateClientRequest createClientRequest, Principal principal) throws Exception {
        Response response= new Response();
        try{
            response.setStatus(Response.Status.OK).setPayload(clientsService.updateClient(createClientRequest, userService.findUserByEmail(principal.getName())));
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
        }

        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteClient(@PathVariable String id) {
        Response response = new Response();
        try{
            clientsService.deleteClient(id);
            response.setStatus(Response.Status.OK).setPayload("Client with " + id + " Deleted successfully");
        }catch (Exception ee){
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(ee.getMessage());
            return  response;
        }
        return response;
    }
}