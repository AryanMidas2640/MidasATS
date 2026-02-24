package com.midas.consulting.controller.v1.api;

import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.dto.model.user.RoleDto;
import com.midas.consulting.service.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST Controller for Role CRUD operations
 * Provides endpoints for managing roles in the system
 */
@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/roles")
@Api(value = "Role Management", description = "Operations for managing roles in the HRMS application")
public class RoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);
    
    private final RoleService roleService;
    
    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    
    /**
     * Create a new role
     * POST /api/v1/roles
     */
    @PostMapping
    @ApiOperation(value = "Create a new role", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully created role"),
        @ApiResponse(code = 400, message = "Invalid input or duplicate role"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Response> createRole(@RequestBody @Valid RoleDto roleDto) {
        logger.info("Creating new role: {}", roleDto.getRole());
        
        Response response = new Response();
        try {
            RoleDto createdRole = roleService.createRole(roleDto);
            response.setStatus(Response.Status.OK);
            response.setPayload(createdRole);
            logger.info("Successfully created role with ID: {}", createdRole.getId());
            
        } catch (Exception e) {
            logger.error("Error creating role: {}", e.getMessage(), e);
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all roles
     * GET /api/v1/roles
     */
    @GetMapping
    @ApiOperation(value = "Get all roles", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved roles"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Response> getAllRoles() {
        logger.info("Fetching all roles");
        
        Response response = new Response();
        try {
            List<RoleDto> roles = roleService.getAllRoles();
            response.setStatus(Response.Status.OK);
            response.setPayload(roles);
            logger.info("Successfully fetched {} roles", roles.size());
            
        } catch (Exception e) {
            logger.error("Error fetching roles: {}", e.getMessage(), e);
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get role by ID
     * GET /api/v1/roles/{id}
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "Get role by ID", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved role"),
        @ApiResponse(code = 404, message = "Role not found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Response> getRoleById(@PathVariable String id) {
        logger.info("Fetching role with ID: {}", id);
        
        Response response = new Response();
        try {
            Optional<RoleDto> role = roleService.getRoleById(id);
            
            if (role.isPresent()) {
                response.setStatus(Response.Status.OK);
                response.setPayload(role.get());
                logger.info("Successfully fetched role: {}", role.get().getRole());
            } else {
                response.setStatus(Response.Status.NOT_FOUND);
                response.setErrors("Role not found with ID: " + id);
                logger.warn("Role not found with ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching role: {}", e.getMessage(), e);
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get role by name
     * GET /api/v1/roles/name/{roleName}
     */
    @GetMapping("/name/{roleName}")
    @ApiOperation(value = "Get role by name", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved role"),
        @ApiResponse(code = 404, message = "Role not found"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Response> getRoleByName(@PathVariable String roleName) {
        logger.info("Fetching role with name: {}", roleName);
        
        Response response = new Response();
        try {
            Optional<RoleDto> role = roleService.getRoleByName(roleName);
            
            if (role.isPresent()) {
                response.setStatus(Response.Status.OK);
                response.setPayload(role.get());
                logger.info("Successfully fetched role with ID: {}", role.get().getId());
            } else {
                response.setStatus(Response.Status.NOT_FOUND);
                response.setErrors("Role not found with name: " + roleName);
                logger.warn("Role not found with name: {}", roleName);
            }
            
        } catch (Exception e) {
            logger.error("Error fetching role: {}", e.getMessage(), e);
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an existing role
     * PUT /api/v1/roles/{id}
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "Update an existing role", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully updated role"),
        @ApiResponse(code = 404, message = "Role not found"),
        @ApiResponse(code = 400, message = "Invalid input or duplicate role name"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Response> updateRole(
            @PathVariable String id,
            @RequestBody @Valid RoleDto roleDto) {
        logger.info("Updating role with ID: {}", id);
        
        Response response = new Response();
        try {
            RoleDto updatedRole = roleService.updateRole(id, roleDto);
            response.setStatus(Response.Status.OK);
            response.setPayload(updatedRole);
            logger.info("Successfully updated role: {}", updatedRole.getRole());
            
        } catch (Exception e) {
            logger.error("Error updating role: {}", e.getMessage(), e);
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a role
     * DELETE /api/v1/roles/{id}
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete a role", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully deleted role"),
        @ApiResponse(code = 404, message = "Role not found"),
        @ApiResponse(code = 400, message = "Cannot delete role with assigned users"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Response> deleteRole(@PathVariable String id) {
        logger.info("Deleting role with ID: {}", id);
        
        Response response = new Response();
        try {
            boolean deleted = roleService.deleteRole(id);
            
            if (deleted) {
                response.setStatus(Response.Status.OK);
                response.setPayload("Role deleted successfully");
                logger.info("Successfully deleted role with ID: {}", id);
            }
            
        } catch (Exception e) {
            logger.error("Error deleting role: {}", e.getMessage(), e);
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Check if role exists by name
     * GET /api/v1/roles/exists/{roleName}
     */
    @GetMapping("/exists/{roleName}")
    @ApiOperation(value = "Check if role exists", response = Response.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully checked role existence"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    public ResponseEntity<Response> checkRoleExists(@PathVariable String roleName) {
        logger.info("Checking if role exists: {}", roleName);
        
        Response response = new Response();
        try {
            boolean exists = roleService.existsByRoleName(roleName);
            response.setStatus(Response.Status.OK);
            response.setPayload(exists);
            logger.info("Role '{}' exists: {}", roleName, exists);
            
        } catch (Exception e) {
            logger.error("Error checking role existence: {}", e.getMessage(), e);
            response.setStatus(Response.Status.EXCEPTION);
            response.setErrors(e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}