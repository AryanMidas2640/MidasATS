package com.midas.consulting.service;

import com.midas.consulting.dto.model.user.RoleDto;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Role CRUD operations
 */
public interface RoleService {
    
    /**
     * Create a new role
     * 
     * @param roleDto Role data transfer object
     * @return Created role DTO
     */
    RoleDto createRole(RoleDto roleDto);
    
    /**
     * Get all roles
     * 
     * @return List of all roles
     */
    List<RoleDto> getAllRoles();
    
    /**
     * Get role by ID
     * 
     * @param id Role ID
     * @return Optional containing the role if found
     */
    Optional<RoleDto> getRoleById(String id);
    
    /**
     * Get role by role name
     * 
     * @param roleName Role name
     * @return Optional containing the role if found
     */
    Optional<RoleDto> getRoleByName(String roleName);
    
    /**
     * Update an existing role
     * 
     * @param id Role ID
     * @param roleDto Updated role data
     * @return Updated role DTO
     */
    RoleDto updateRole(String id, RoleDto roleDto);
    
    /**
     * Delete a role by ID
     * 
     * @param id Role ID
     * @return true if deleted successfully
     */
    boolean deleteRole(String id);
    
    /**
     * Check if role exists by name
     * 
     * @param roleName Role name
     * @return true if role exists
     */
    boolean existsByRoleName(String roleName);
}