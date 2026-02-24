package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.request.hrms.CreateClientRequest;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.ClientsService;
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
@RequestMapping("/api/v1/client")
@Api(value = "client-management", description = "Operations for Client management")
@Slf4j
public class ClientController {

    private final UserService userService;
    private final ClientsService clientsService;

    @Autowired
    public ClientController(UserService userService, ClientsService clientsService) {
        this.userService = userService;
        this.clientsService = clientsService;
    }

    @GetMapping
    @ResponseBody
    @ApiOperation(value = "Get all clients", authorizations = {@Authorization(value = "apiKey")})
    public Response getAllClients(Principal principal) {
        Response response = new Response();
        try {
            log.info("Fetching all clients for user: {}", principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(clientsService.getAllClients());
        } catch (Exception e) {
            log.error("Error fetching all clients", e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "Get client by ID", authorizations = {@Authorization(value = "apiKey")})
    public Response getClientById(@PathVariable String id, Principal principal) {
        Response response = new Response();
        try {
            log.info("Fetching client with id: {} for user: {}", id, principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(clientsService.getClientById(id)
                            .orElseThrow(() -> new Exception("Client not found with id: " + id)));
        } catch (Exception e) {
            log.error("Error fetching client with id: {}", id, e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @PostMapping
    @ResponseBody
    @ApiOperation(value = "Create a new client", authorizations = {@Authorization(value = "apiKey")})
    public Response saveClient(@Valid @RequestBody CreateClientRequest createClientRequest, 
                               Principal principal) {
        Response response = new Response();
        try {
            log.info("Creating new client: {} by user: {}", 
                    createClientRequest.getClientName(), principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(clientsService.saveClient(createClientRequest, 
                            userService.findUserByEmail(principal.getName())));
        } catch (Exception e) {
            log.error("Error creating client", e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @PatchMapping
    @ResponseBody
    @ApiOperation(value = "Update an existing client", authorizations = {@Authorization(value = "apiKey")})
    public Response updateClient(@Valid @RequestBody CreateClientRequest createClientRequest, 
                                 Principal principal) {
        Response response = new Response();
        try {
            log.info("Updating client with id: {} by user: {}", 
                    createClientRequest.getId(), principal.getName());
            response.setStatus(Response.Status.OK)
                    .setPayload(clientsService.updateClient(createClientRequest, 
                            userService.findUserByEmail(principal.getName())));
        } catch (Exception e) {
            log.error("Error updating client with id: {}", createClientRequest.getId(), e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "Delete a client", authorizations = {@Authorization(value = "apiKey")})
    public Response deleteClient(@PathVariable String id, Principal principal) {
        Response response = new Response();
        try {
            log.info("Deleting client with id: {} by user: {}", id, principal.getName());
            clientsService.deleteClient(id);
            response.setStatus(Response.Status.OK)
                    .setPayload("Client with id " + id + " deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting client with id: {}", id, e);
            response.setStatus(Response.Status.EXCEPTION)
                    .setErrors(e.getMessage());
        }
        return response;
    }
}