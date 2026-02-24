package com.midas.consulting.controller.v1.api;

import com.midas.consulting.model.TenantPoolConfig;
import com.midas.consulting.service.ConnectionMetricsService;
import com.midas.consulting.service.TenantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/admin/connections")
@Api(value = "Database Connection Management", description = "Operations for managing database connections")
public class ConnectionManagementController {

    @Autowired
    private ConnectionMetricsService metricsService;
    
    @Autowired
    private TenantService tenantService;

    @GetMapping("/metrics")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(value = "Get connection metrics", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<?> getConnectionMetrics(@RequestParam(required = false) String tenantId) {
        if (tenantId != null) {
            return ResponseEntity.ok(metricsService.getMetricsForTenant(tenantId));
        } else {
            return ResponseEntity.ok(metricsService.getAllMetrics());
        }
    }
    
    @GetMapping("/config")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(value = "Get connection pool configuration", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<?> getConnectionConfig(@RequestParam String tenantId) {
        return ResponseEntity.ok(tenantService.getTenantPoolConfig(tenantId));
    }
    
    @PutMapping("/config")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(value = "Update connection pool configuration", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<?> updateConnectionConfig(
            @RequestParam String tenantId, 
            @RequestBody TenantPoolConfig config) {
        
        tenantService.updateTenantPoolConfig(tenantId, config);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Connection pool configuration updated for tenant: " + tenantId);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-metrics")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(value = "Reset connection metrics", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<?> resetMetrics(@RequestParam(required = false) String tenantId) {
        if (tenantId != null) {
            metricsService.resetMetrics(tenantId);
        } else {
            metricsService.resetAllMetrics();
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", tenantId != null 
                ? "Metrics reset for tenant: " + tenantId 
                : "All metrics reset");
        
        return ResponseEntity.ok(response);
    }
}