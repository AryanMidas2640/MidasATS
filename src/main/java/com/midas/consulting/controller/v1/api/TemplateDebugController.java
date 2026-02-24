package com.midas.consulting.controller.v1.api;

import com.midas.consulting.config.database.MongoTemplateProvider;
//import com.midas.consulting.dto.response.Response;
import com.midas.consulting.controller.v1.response.Response;
import com.midas.consulting.model.tenant.EmailTemplate;
import com.midas.consulting.service.EmailTemplateService;
import com.midas.consulting.service.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(maxAge = 36000, origins = "*", allowedHeaders = "*")
@RequestMapping("/api/v1/debug/templates")
public class TemplateDebugController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateDebugController.class);

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    @GetMapping("/status")
    public Response getTemplateStatus() {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            
            Map<String, Object> status = new HashMap<>();
            status.put("tenantId", tenantId);
            
            // Check templates in database
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            List<EmailTemplate> templates = mongoTemplate.find(query, EmailTemplate.class);
            
            status.put("totalTemplates", templates.size());
            status.put("templates", templates.stream().map(new Function<EmailTemplate, Map<String, Object>>() {
                @Override
                public Map<String, Object> apply(EmailTemplate t) {
                    Map<String, Object> templateInfo = new HashMap<>();
                    templateInfo.put("id", t.getId());
                    templateInfo.put("name", t.getTemplateName());
                    templateInfo.put("category", t.getCategory());
                    templateInfo.put("active", t.isActive());
                    templateInfo.put("system", t.isSystem());
                    templateInfo.put("dateCreated", t.getDateCreated());
                    return templateInfo;
                }
            }).collect(Collectors.toList()));
            
            // Check specific system templates
            String[] systemTemplates = {"password-reset", "welcome-user", "application-received", "interview-invitation"};
            Map<String, Boolean> systemTemplateStatus = new HashMap<>();
            
            for (String templateName : systemTemplates) {
                EmailTemplate template = emailTemplateService.getTemplateByName(templateName);
                systemTemplateStatus.put(templateName, template != null);
            }
            
            status.put("systemTemplates", systemTemplateStatus);
            
            return Response.ok().setPayload(status);
            
        } catch (Exception e) {
            logger.error("Error getting template status: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to get template status", e);
        }
    }

    @PostMapping("/recreate-defaults")
    public Response recreateDefaultTemplates() {
        try {
            String tenantId = TenantContext.getCurrentTenant();
            
            logger.info("Recreating default templates for tenant: {}", tenantId);
            
            // Delete existing system templates first
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Query deleteQuery = new Query(Criteria.where("tenantId").is(tenantId)
                    .and("isSystem").is(true));
            mongoTemplate.remove(deleteQuery, EmailTemplate.class);
            
            // Create default templates
            emailTemplateService.createDefaultTemplates(tenantId);
            
            // Verify creation
            Query query = new Query(Criteria.where("tenantId").is(tenantId));
            List<EmailTemplate> templates = mongoTemplate.find(query, EmailTemplate.class);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Default templates recreated successfully");
            result.put("templatesCreated", templates.size());
            result.put("templates", templates.stream().map(new Function<EmailTemplate, String>() {
                @Override
                public String apply(EmailTemplate template) {
                    return template.getTemplateName();
                }
            }).collect(Collectors.toList()));
            
            return Response.ok().setPayload(result);
            
        } catch (Exception e) {
            logger.error("Error recreating default templates: {}", e.getMessage(), e);
            return Response.exception().addErrorMsgToResponse("Failed to recreate default templates", e);
        }
    }

    @ModelAttribute
    public void setTenantContext(HttpServletRequest request) {
        String tenantId = request.getHeader("X-Tenant");
        logger.debug("Setting tenant context in debug controller: {}", tenantId);
        TenantContext.setCurrentTenant(tenantId);
    }
}