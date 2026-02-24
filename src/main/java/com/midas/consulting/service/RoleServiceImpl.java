package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.dto.mapper.RoleMapper;
import com.midas.consulting.dto.model.user.RoleDto;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.user.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of RoleService for managing Role entities
 */
@Service
public class RoleServiceImpl implements RoleService {
    
    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);
    
    private final MongoTemplateProvider mongoTemplateProvider;
    
    @Autowired
    public RoleServiceImpl(MongoTemplateProvider mongoTemplateProvider) {
        this.mongoTemplateProvider = mongoTemplateProvider;
    }
    
    @Override
    public RoleDto createRole(RoleDto roleDto) {
        logger.debug("Creating new role: {}", roleDto.getRole());
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        
        // Check if role already exists
        Query query = Query.query(Criteria.where("role").is(roleDto.getRole()));
        Role existingRole = mongoTemplate.findOne(query, Role.class);
        
        if (existingRole != null) {
            throw new MidasCustomException.DuplicateEntityException(
                "Role with name '" + roleDto.getRole() + "' already exists"
            );
        }
        
        Role role = RoleMapper.toRole(roleDto);
        Role savedRole = mongoTemplate.save(role);
        
        logger.info("Successfully created role: {} with ID: {}", savedRole.getRole(), savedRole.getId());
        return RoleMapper.toRoleDto(savedRole);
    }
    
    @Override
    public List<RoleDto> getAllRoles() {
        logger.debug("Fetching all roles");
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        
        List<Role> roles = mongoTemplate.findAll(Role.class);
        
        logger.info("Found {} roles", roles.size());
        return roles.stream()
                .map(RoleMapper::toRoleDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<RoleDto> getRoleById(String id) {
        logger.debug("Fetching role by ID: {}", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Role role = mongoTemplate.findById(id, Role.class);
        
        if (role != null) {
            logger.info("Found role: {}", role.getRole());
            return Optional.of(RoleMapper.toRoleDto(role));
        }
        
        logger.warn("Role not found with ID: {}", id);
        return Optional.empty();
    }
    
    @Override
    public Optional<RoleDto> getRoleByName(String roleName) {
        logger.debug("Fetching role by name: {}", roleName);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Query query = Query.query(Criteria.where("role").is(roleName));
        Role role = mongoTemplate.findOne(query, Role.class);
        
        if (role != null) {
            logger.info("Found role: {} with ID: {}", role.getRole(), role.getId());
            return Optional.of(RoleMapper.toRoleDto(role));
        }
        
        logger.warn("Role not found with name: {}", roleName);
        return Optional.empty();
    }
    
    @Override
    public RoleDto updateRole(String id, RoleDto roleDto) {
        logger.debug("Updating role with ID: {}", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        
        // Check if role exists
        Role existingRole = mongoTemplate.findById(id, Role.class);
        if (existingRole == null) {
            logger.error("Role not found with ID: {}", id);
            throw new MidasCustomException.EntityNotFoundException(
                "Role not found with ID: " + id
            );
        }
        
        // Check if new role name is already taken by another role
        if (!existingRole.getRole().equals(roleDto.getRole())) {
            Query query = Query.query(Criteria.where("role").is(roleDto.getRole()));
            Role duplicateRole = mongoTemplate.findOne(query, Role.class);
            
            if (duplicateRole != null && !duplicateRole.getId().equals(id)) {
                throw new MidasCustomException.DuplicateEntityException(
                    "Role with name '" + roleDto.getRole() + "' already exists"
                );
            }
        }
        
        // Update role
        existingRole.setRole(roleDto.getRole());
        Role updatedRole = mongoTemplate.save(existingRole);
        
        logger.info("Successfully updated role: {} with ID: {}", updatedRole.getRole(), updatedRole.getId());
        return RoleMapper.toRoleDto(updatedRole);
    }
    
    @Override
    public boolean deleteRole(String id) {
        logger.debug("Deleting role with ID: {}", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        
        // Check if role exists
        Role role = mongoTemplate.findById(id, Role.class);
        if (role == null) {
            logger.error("Role not found with ID: {}", id);
            throw new MidasCustomException.EntityNotFoundException(
                "Role not found with ID: " + id
            );
        }
        
        // Check if any users are assigned this role
        Query userQuery = Query.query(Criteria.where("roles._id").is(id));
        boolean hasUsers = mongoTemplate.exists(userQuery, com.midas.consulting.model.user.User.class);
        
        if (hasUsers) {
            logger.warn("Cannot delete role {} - users are still assigned to it", role.getRole());
            throw new MidasCustomException.DuplicateEntityException(
                "Cannot delete role '" + role.getRole() + "' - users are still assigned to it"
            );
        }
        
        Query query = Query.query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, Role.class);
        
        logger.info("Successfully deleted role: {} with ID: {}", role.getRole(), id);
        return true;
    }
    
    @Override
    public boolean existsByRoleName(String roleName) {
        logger.debug("Checking if role exists: {}", roleName);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        
        Query query = Query.query(Criteria.where("role").is(roleName));
        boolean exists = mongoTemplate.exists(query, Role.class);
        
        logger.debug("Role {} exists: {}", roleName, exists);
        return exists;
    }
}