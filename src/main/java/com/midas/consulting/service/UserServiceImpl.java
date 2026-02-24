package com.midas.consulting.service;
// Add these imports to the top of your UserServiceImpl.java file

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.api.JwtAuthenticationController;
import com.midas.consulting.controller.v1.request.UserSignupRequest;
import com.midas.consulting.dto.mapper.RoleMapper;
import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.model.user.RoleDto;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.exception.EntityType;
import com.midas.consulting.exception.ExceptionType;
import com.midas.consulting.exception.MidasCustomException;
import com.midas.consulting.model.user.ConfirmationToken;
import com.midas.consulting.model.user.Role;
import com.midas.consulting.model.user.User;
import com.midas.consulting.model.user.UserType;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by Dheeraj Singh.
 */
@Component
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final MongoTemplateProvider mongoTemplateProvider;
    private final ModelMapper modelMapper;

//    @Autowired
    private final ATSEmailTemplateService atsEmailTemplateService;
    @Autowired
    public UserServiceImpl(BCryptPasswordEncoder bCryptPasswordEncoder,
                           MongoTemplateProvider mongoTemplateProvider,
                           ModelMapper modelMapper, ATSEmailTemplateService atsEmailTemplateService) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mongoTemplateProvider = mongoTemplateProvider;
        this.modelMapper = modelMapper;
        this.atsEmailTemplateService = atsEmailTemplateService;
    }

    @Override
    public UserDto signup(UserDto userDto) {
        logger.debug("Signing up user with email: {}", userDto.getEmail());
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("email").is(userDto.getEmail()));
        User user = mongoTemplate.findOne(query, User.class);

        if (user == null) {
            user = new User()
                    .setEmail(userDto.getEmail())
                    .setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()))
                    .setRoles(new HashSet<>())
                    .setFirstName(userDto.getFirstName())
                    .setLastName(userDto.getLastName())
                    .setProfilePicture(userDto.getProfilePicture())
                    .setActive(false)
                    .setDateModified(new Date())
                    .setDateCreated(new Date())
                    .setMobileNumber(userDto.getMobileNumber());

            return UserMapper.toUserDto(mongoTemplate.save(user));
        }
        throw exception(EntityType.USER, ExceptionType.DUPLICATE_ENTITY, userDto.getEmail());
    }

    @Override
    public UserDto signup(UserSignupRequest userSignupRequest) {
        logger.debug("Signing up user with email: {}", userSignupRequest.getEmail());
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("email").is(userSignupRequest.getEmail()));
        User user = mongoTemplate.findOne(query, User.class);

        User manager = null;
        if (userSignupRequest.getManager() != null) {
            manager = mongoTemplate.findById(userSignupRequest.getManager(), User.class);
        }

        if (user == null) {
            user = new User()
                    .setEmail(userSignupRequest.getEmail())
                    .setPassword(bCryptPasswordEncoder.encode(userSignupRequest.getPassword()))
                    .setRoles(userSignupRequest.getRoles())
                    .setFirstName(userSignupRequest.getFirstName())
                    .setLastName(userSignupRequest.getLastName())
                    .setProfilePicture("https://i.pinimg.com/736x/65/49/ca/6549cacdca6c392649a70153981bd27d.jpg")
                    .setActive(false)
                    .setUserType(UserType.INTERNAL)
                    .setIsZoomUser(userSignupRequest.getIsZoomUser())
                    .setDateModified(new Date())
                    .setDateCreated(new Date())
                    .setManager(manager)
                    .setMobileNumber(userSignupRequest.getMobileNumber());;
           UserDto savedUser= UserMapper.toUserDto(mongoTemplate.save(user));
//            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());
            return savedUser ;
        }
        throw exception(EntityType.USER, ExceptionType.DUPLICATE_ENTITY,
                userSignupRequest.getEmail() + " Or the manager was not found " + userSignupRequest.getManager());
    }

    @Override
    public ResponseEntity<?> confirmEmail(String confirmationToken) {
        logger.debug("Confirming email with token: {}", confirmationToken);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("confirmationToken").is(confirmationToken));
        ConfirmationToken token = mongoTemplate.findOne(query, ConfirmationToken.class);

        if (token != null) {
            return ResponseEntity.ok("Email verified successfully!");
        }
        return ResponseEntity.badRequest().body("Error: Couldn't verify email");
    }

    @Override
    public UserDto findUserByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("email").regex("^" + email + "$", "i"));
        User user = mongoTemplate.findOne(query, User.class);

        if (user != null) {
            return UserMapper.toUserDto(user);
        }
        throw exception(EntityType.USER, ExceptionType.ENTITY_NOT_FOUND, email);
    }

    @Override
    public UserDto updateProfile(UserDto userDto) {
        logger.debug("Updating profile for user: {}", userDto.getEmail());
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query userQuery = Query.query(Criteria.where("email").is(userDto.getEmail()));
        User user = mongoTemplate.findOne(userQuery, User.class);

        Role role = null;
        if (!userDto.getRoles().isEmpty()) {
            String roleId = userDto.getRoles().stream().findFirst().get().getId();
            role = mongoTemplate.findById(roleId, Role.class);
        }

        User manager = null;
        if (userDto.getManager() != null) {
            manager = mongoTemplate.findById(userDto.getManager(), User.class);
        }

        if (user != null && role != null) {
            RoleDto roleDto = RoleMapper.toRoleDto(role);
            Set<RoleDto> roles = new HashSet<>();
            roles.add(roleDto);
            userDto.setRoles(roles);

            User userToSave = UserMapper.toUser(userDto);
            userToSave.setManager(manager);
            return UserMapper.toUserDto(mongoTemplate.save(userToSave));
        }
        throw exception(EntityType.USER, ExceptionType.ENTITY_NOT_FOUND, userDto.getEmail());
    }

    @Override
    public UserDto changePassword(UserDto userDto, String newPassword) {
        logger.debug("Changing password for user: {}", userDto.getEmail());
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("email").is(userDto.getEmail()));
        User user = mongoTemplate.findOne(query, User.class);

        if (user != null) {
            user.setPassword(bCryptPasswordEncoder.encode(newPassword));
            return UserMapper.toUserDto(mongoTemplate.save(user));
        }
        throw exception(EntityType.USER, ExceptionType.ENTITY_NOT_FOUND, userDto.getEmail());
    }

    @Override
    public List<Role> allRoles() {
        logger.debug("Getting all roles");
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        return mongoTemplate.findAll(Role.class);
    }

    @Override
    public List<User> allUsers() {
        logger.debug("Getting all active users");
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        Query query = Query.query(Criteria.where("isActive").is(true));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public Object updatePassword(JwtAuthenticationController.PasswordResetRequest passwordResetRequest) {
        logger.debug("Updating password for user ID: {}", passwordResetRequest.getUserId());
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        User user = mongoTemplate.findById(passwordResetRequest.getUserId(), User.class);
        if (user != null) {
            user.setActive(true);
            user.setPassword(bCryptPasswordEncoder.encode(passwordResetRequest.getNewPassword()));
            return mongoTemplate.save(user);
        }
        throw new MidasCustomException.EntityNotFoundException("User looking for was not found in our database!");
    }

    @Override
    public UserDto findUserById(String id) {
        logger.debug("Finding user by ID: {}", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        User user = mongoTemplate.findById(id, User.class);
        return user != null ? UserMapper.toUserDto(user) : null;
    }

    @Override
    public List<User> getUserByRoll(String roleId) {
        logger.debug("Getting users by role ID: {}", roleId);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("roles._id").is(roleId));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public List<User> getUsersByManager(String managerId) {
        logger.debug("Getting users by manager ID: {}", managerId);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("manager._id").is(managerId));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public User findUserObjectById(String id) {
        logger.debug("Finding user object by ID: {}", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
        return mongoTemplate.findById(id, User.class);
    }

    @Override
    public List<UserDto> findManagersByUser(String id) {
        logger.debug("Finding managers for user ID: {}", id);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        List<UserDto> userDtos = new ArrayList<>();
        User userFound = mongoTemplate.findById(id, User.class);

        if (userFound == null) {
            throw new MidasCustomException.EntityNotFoundException("The user with id " + id + " was not found");
        }

        // Check if the user is a Recruiter
        Optional<Role> recruiterRole = userFound.getRoles().stream()
                .filter(role -> role.getRole().equalsIgnoreCase("RECRUITER"))
                .findFirst();

        if (recruiterRole.isPresent()) {
            // User is a Recruiter, get both Team Lead and Account Manager
            addManagerIfExists(userDtos, userFound.getManager());
            User teamLead = userFound.getManager();
            if (teamLead != null && teamLead.getManager() != null) {
                userDtos.add(UserMapper.toUserDto(teamLead.getManager()));
            }
        } else {
            // User is not a Recruiter, return only the Account Manager
            User accountManager = userFound.getManager();
            if (accountManager != null) {
                userDtos.add(UserMapper.toUserDto(accountManager));
            } else {
                throw new MidasCustomException.EntityNotFoundException(
                        "The user with id " + id + " does not have an account manager");
            }
        }

        return userDtos;
    }

    private void addManagerIfExists(List<UserDto> userDtos, User manager) {
        if (manager != null) {
            userDtos.add(UserMapper.toUserDto(manager));
        }
    }

    @Override
    public UserDto getUserByEmail(String email) {
        logger.debug("Getting user by email: {}", email);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("email").is(email.trim()));
        User user = mongoTemplate.findOne(query, User.class);

        if (user != null) {
            return UserMapper.toUserDto(user);
        }
        throw new MidasCustomException.EntityNotFoundException("Entity you were looking for was not found");
    }

    /**
     * @param userId
     * @return
     */
    @Override
    public String getUserRole(String userId) {
        logger.debug("Getting user by userId: {}", userId);
        MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

        Query query = Query.query(Criteria.where("email").is(userId.trim()));
        User user = mongoTemplate.findOne(query, User.class);

        if (user != null) {
            return user.getRoles().stream().findFirst().get().getRole();
        }
        throw new MidasCustomException.EntityNotFoundException("Entity you were looking for was not found");
    }

    private RuntimeException exception(EntityType entityType, ExceptionType exceptionType, String... args) {
        return MidasCustomException.throwException(entityType, exceptionType, args);
    }
}