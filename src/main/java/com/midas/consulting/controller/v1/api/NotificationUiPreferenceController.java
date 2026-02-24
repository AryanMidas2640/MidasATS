package com.midas.consulting.controller.v1.api;

import com.midas.consulting.model.user.NotificationUiPreference;
import com.midas.consulting.service.NotificationUiPreferenceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/notification-preferences")
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@Api(value = "hrms-application", description = "Notification UI Preference CRUD Operations")
public class NotificationUiPreferenceController {

    private final NotificationUiPreferenceService preferenceService;

    @Autowired
    public NotificationUiPreferenceController(NotificationUiPreferenceService preferenceService) {
        this.preferenceService = preferenceService;
    }

    @GetMapping
    @ApiOperation(value = "Get all notification UI preferences", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<NotificationUiPreference>> getAll() {
        return ResponseEntity.ok(preferenceService.getAllPreferences());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "Get notification preference by ID", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<NotificationUiPreference> getById(@PathVariable String id) {
        Optional<NotificationUiPreference> preference = preferenceService.getById(id);
        return preference.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @ApiOperation(value = "Get notification preference by user ID", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<NotificationUiPreference> getByUserId(@PathVariable String userId) {
        Optional<NotificationUiPreference> preference = preferenceService.getByUserId(userId);
        return preference.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @ApiOperation(value = "Create or update notification UI preference", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<NotificationUiPreference> createOrUpdate(@RequestBody NotificationUiPreference preference) {
        NotificationUiPreference saved = preferenceService.save(preference);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "Update notification UI preference", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<NotificationUiPreference> update(@PathVariable String id, @RequestBody NotificationUiPreference preference) {
        if (!preferenceService.getById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        preference.setId(id);
        NotificationUiPreference updated = preferenceService.save(preference);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete notification UI preference", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!preferenceService.getById(id).isPresent()) {
            return ResponseEntity.notFound().build();
        }
        preferenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
