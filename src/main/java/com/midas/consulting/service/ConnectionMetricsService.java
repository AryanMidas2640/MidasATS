package com.midas.consulting.service;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ConnectionMetricsService {
    
    private final Map<String, TenantMetrics> tenantMetrics = new ConcurrentHashMap<>();
    
    public void recordConnectionRequest(String tenantId) {
        getTenantMetrics(tenantId).connectionRequests.incrementAndGet();
    }
    
    public void recordConnectionCreation(String tenantId) {
        getTenantMetrics(tenantId).connectionsCreated.incrementAndGet();
    }
    
    public void recordQueryExecution(String tenantId, long executionTimeMs) {
        TenantMetrics metrics = getTenantMetrics(tenantId);
        metrics.queriesExecuted.incrementAndGet();
        metrics.totalQueryTimeMs.addAndGet(executionTimeMs);
    }
    
    public TenantMetrics getMetricsForTenant(String tenantId) {
        return getTenantMetrics(tenantId);
    }
    
    public Map<String, TenantMetrics> getAllMetrics() {
        return tenantMetrics;
    }
    
    public void resetMetrics(String tenantId) {
        tenantMetrics.remove(tenantId);
    }
    
    public void resetAllMetrics() {
        tenantMetrics.clear();
    }
    
    private TenantMetrics getTenantMetrics(String tenantId) {
        return tenantMetrics.computeIfAbsent(tenantId, id -> new TenantMetrics());
    }
    
    public static class TenantMetrics {
        private final AtomicInteger connectionRequests = new AtomicInteger(0);
        private final AtomicInteger connectionsCreated = new AtomicInteger(0);
        private final AtomicInteger queriesExecuted = new AtomicInteger(0);
        private final AtomicLong totalQueryTimeMs = new AtomicLong(0);
        
        public int getConnectionRequests() {
            return connectionRequests.get();
        }
        
        public int getConnectionsCreated() {
            return connectionsCreated.get();
        }
        
        public int getQueriesExecuted() {
            return queriesExecuted.get();
        }
        
        public long getTotalQueryTimeMs() {
            return totalQueryTimeMs.get();
        }
        
        public double getAverageQueryTimeMs() {
            int queries = queriesExecuted.get();
            return queries > 0 ? (double) totalQueryTimeMs.get() / queries : 0;
        }
    }
}