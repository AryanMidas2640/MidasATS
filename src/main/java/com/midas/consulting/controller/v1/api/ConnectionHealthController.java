package com.midas.consulting.controller.v1.api;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.model.Tenant;
import com.midas.consulting.service.ConnectionMetricsService;
import com.midas.consulting.service.TenantContext;
import com.midas.consulting.service.TenantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/admin/health")
@Api(value = "Connection Health Dashboard", description = "Operations for monitoring connection health")
public class ConnectionHealthController {

    @Autowired
    private TenantService tenantService;
    
    @Autowired
    private ConnectionMetricsService metricsService;
    
    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(value = "Get connection health status for all tenants", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<?> getConnectionHealthStatus() {
        List<Tenant> tenants = tenantService.getAllTenants();
        
        // Check health for each tenant in parallel
        List<CompletableFuture<Map<String, Object>>> futures = tenants.stream()
                .map(tenant -> CompletableFuture.supplyAsync(() -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("tenantId", tenant.getId());
                    status.put("tenantName", tenant.getTenantName());
                    
                    try {
                        // Test connection by executing a simple query

                                // Test connection by executing a simple query
                        boolean isConnected = checkTenantConnection(tenant.getId());
                        status.put("connected", isConnected);
                        status.put("status", isConnected ? "ACTIVE" : "DISCONNECTED");

                        // Add metrics data if available
                        ConnectionMetricsService.TenantMetrics metrics =
                                metricsService.getMetricsForTenant(tenant.getId());
                        status.put("connectionRequests", metrics.getConnectionRequests());
                        status.put("connectionsCreated", metrics.getConnectionsCreated());
                        status.put("queriesExecuted", metrics.getQueriesExecuted());
                        status.put("avgQueryTimeMs", metrics.getAverageQueryTimeMs());

                    } catch (Exception e) {
                        status.put("connected", false);
                        status.put("status", "ERROR");
                        status.put("errorMessage", e.getMessage());
                    }

                    return status;
                }, executorService))
                .collect(Collectors.toList());

        // Wait for all health checks to complete
        List<Map<String, Object>> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // Add summary information
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", new Date());
        response.put("tenantCount", tenants.size());
        response.put("activeCount", results.stream()
                .filter(m -> Boolean.TRUE.equals(m.get("connected")))
                .count());
        response.put("tenants", results);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{tenantId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(value = "Get connection health status for a specific tenant", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<?> getTenantConnectionHealth(@PathVariable String tenantId) {
        Map<String, Object> status = new HashMap<>();
        status.put("tenantId", tenantId);

        try {
            Tenant tenant = tenantService.getTenantById(tenantId);
            status.put("tenantName", tenant.getTenantName());

            // Test connection
            boolean isConnected = checkTenantConnection(tenantId);
            status.put("connected", isConnected);
            status.put("status", isConnected ? "ACTIVE" : "DISCONNECTED");

            // Add detailed metrics
            ConnectionMetricsService.TenantMetrics metrics =
                    metricsService.getMetricsForTenant(tenantId);
            status.put("metrics", metrics);

        } catch (Exception e) {
            status.put("connected", false);
            status.put("status", "ERROR");
            status.put("errorMessage", e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

    @PostMapping("/test-connection/{tenantId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ApiOperation(value = "Test connection for a specific tenant", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity<?> testTenantConnection(@PathVariable String tenantId) {
        Map<String, Object> result = new HashMap<>();
        result.put("tenantId", tenantId);
        result.put("timestamp", new Date());

        try {
            long startTime = System.currentTimeMillis();
            boolean isConnected = checkTenantConnection(tenantId);
            long endTime = System.currentTimeMillis();

            result.put("connected", isConnected);
            result.put("responseTimeMs", endTime - startTime);
            result.put("status", isConnected ? "SUCCESS" : "FAILED");

        } catch (Exception e) {
            result.put("connected", false);
            result.put("status", "ERROR");
            result.put("errorMessage", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    private boolean checkTenantConnection(String tenantId) {
        try {
            // Set the tenant context
            TenantContext.setCurrentTenant(tenantId);

            // Try to execute a simple query
            mongoTemplateProvider.getMongoTemplate().getCollectionNames();

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            // Always clear the tenant context
            TenantContext.clear();
        }
    }
}