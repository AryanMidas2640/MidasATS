package com.midas.consulting.controller.v1.api;

import com.midas.consulting.dto.mapper.UserMapper;
import com.midas.consulting.dto.model.user.UserDto;
import com.midas.consulting.model.candidate.Activity;
import com.midas.consulting.model.user.User;
import com.midas.consulting.service.ActivityService;
import com.midas.consulting.service.UserService;
import com.midas.consulting.service.hrms.ClientsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/activities")

@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
@Api(value = "hrms-application", description = "Operations pertaining to the HRMS application")
public class ActivityController {


    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final ClientsService clientsService;

    private final ActivityService activityService;

    @Autowired
    public ActivityController(ActivityService activityService,AuthenticationManager authenticationManager, UserService userService, ClientsService clientsService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.clientsService = clientsService;
        this.activityService=activityService;
    }



    @GetMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<Activity>> getAllActivities() {
        List<Activity> activities = activityService.getAllActivities();
        return new ResponseEntity<>(activities, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Activity> getActivityById(@PathVariable String id) {
        Optional<Activity> activity = activityService.getActivityById(id);
        return activity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/getActivityBySourceID/{sourceID}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<Activity>> getActivityBySourceID(@PathVariable String sourceID) {
        List<Activity> activity = activityService.getActivityBySourceID(sourceID);
        return ResponseEntity.ok(activity);//activity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/getActivityByProviderJobID/{providerJobID}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<Activity>> getActivityByProviderJobID(@PathVariable Integer providerJobID) {
        List<Activity> activity = activityService.getActivityByProviderJobID(providerJobID);
        return ResponseEntity.ok(activity);//activity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/getActivityByCandidateId/{candidateId}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<Activity>> getActivityByCandidateId(@PathVariable String candidateId) {
        List<Activity> activity = activityService.getActivityByCandidateId(candidateId);
        return ResponseEntity.ok(activity);//activity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
    ModelMapper modelMapper = new ModelMapper();
    @PostMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Activity> createActivity(Principal principal, @RequestBody Activity activity) {
        UserDto userDto= userService.findUserByEmail(principal.getName());
        activity.setUserID(  modelMapper.map(UserMapper.toUser(userDto), User.class));
        activity.setDateCreated(new Date());
        Activity createdActivity = activityService.createOrUpdateActivity(activity);
        return new ResponseEntity<>(createdActivity, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Activity> updateActivity(@PathVariable String id, @RequestBody Activity activity) {
        if (!activityService.getActivityById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        activity.setId(id);
        Activity updatedActivity = activityService.createOrUpdateActivity(activity);
        return new ResponseEntity<>(updatedActivity, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Void> deleteActivity(@PathVariable String id) {
        if (!activityService.getActivityById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        activityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }
}
