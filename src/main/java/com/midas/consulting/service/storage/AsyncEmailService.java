package com.midas.consulting.service.storage;

//import com.midas.consulting.controller.v1.api.EmailRequest;
import com.midas.consulting.controller.v1.request.EmailRequest;
import com.midas.consulting.service.EnhancedTenantEmailService;
import com.midas.consulting.service.TenantContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

// 8. Async Email Service with MongoTemplate
@Service
public class AsyncEmailService {
    
    private final EnhancedTenantEmailService tenantEmailService;
    private final TaskExecutor emailTaskExecutor;
    
    public AsyncEmailService(EnhancedTenantEmailService tenantEmailService,
                             @Qualifier("emailTaskExecutor") TaskExecutor emailTaskExecutor) {
        this.tenantEmailService = tenantEmailService;
        this.emailTaskExecutor = emailTaskExecutor;
    }
    
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendEmailAsync(String tenantId, EmailRequest emailRequest) {
        // Set tenant context for async execution
        TenantContext.setCurrentTenant(tenantId);
        try {
            tenantEmailService.sendEmail(emailRequest);
            return CompletableFuture.completedFuture(null);
        } finally {
            TenantContext.clear();
        }
    }
    
    @Async("emailTaskExecutor")
    public CompletableFuture<Void> sendBulkEmailAsync(String tenantId, List<EmailRequest> emailRequests) {
        TenantContext.setCurrentTenant(tenantId);
        try {

//            tenantEmailService.sendBulkEmail(emailRequests);
            return CompletableFuture.completedFuture(null);
        } finally {
            TenantContext.clear();
        }
    }
}