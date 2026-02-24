package com.midas.consulting.controller.v1.api;

import com.midas.consulting.model.RoutePermission;
import com.midas.consulting.service.RoutePermissionService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/route-permissions")
@CrossOrigin(maxAge = 36000, origins = "*" , allowedHeaders = "*")
public class RoutePermissionController {
    @Autowired
    private RoutePermissionService service;

    @PostMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<RoutePermission> saveRoutePermission(@RequestBody RoutePermission routePermission) {
        return ResponseEntity.ok(service.saveRoutePermission(routePermission));
    }

    @GetMapping
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<List<RoutePermission>> getAllRoutePermissions() {
        return ResponseEntity.ok(service.getAllRoutePermissions());
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<RoutePermission> getRoutePermissionById(@PathVariable String id) {
        RoutePermission permission = service.getRoutePermissionById(id);
        if (permission == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(permission);
        }
    }

    @PutMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<RoutePermission> updateRoutePermission(@PathVariable String id, @RequestBody RoutePermission routePermission) {
        RoutePermission updatedRoutePermission = service.updateRoutePermission(id, routePermission);
        if (updatedRoutePermission != null) {
            return ResponseEntity.ok(updatedRoutePermission);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<Void> deleteRoutePermission(@PathVariable String id) {
        service.deleteRoutePermissionById(id);
        return ResponseEntity.noContent().build();
    }
}


