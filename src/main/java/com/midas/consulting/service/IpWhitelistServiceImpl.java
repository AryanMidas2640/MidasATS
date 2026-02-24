package com.midas.consulting.service;

import com.midas.consulting.config.database.MongoTemplateProvider;
import com.midas.consulting.controller.v1.response.BulkDeleteIpResponse;
import com.midas.consulting.controller.v1.response.BulkDeletePreview;
import com.midas.consulting.model.IpWhitelist;
import com.midas.consulting.model.IpWhitelistScope;
import com.midas.consulting.model.IpWhitelistType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of IpWhitelistService for database-per-tenant architecture.
 * TenantId is used ONLY for database routing, NOT in WHERE clauses.
 */
@Service
public class IpWhitelistServiceImpl implements IpWhitelistService {

    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistServiceImpl.class);

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );
    private static final Pattern CIDR_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(3[0-2]|[12]?[0-9])$"
    );

    @Autowired
    private MongoTemplateProvider mongoTemplateProvider;

    @Autowired
    private IpBulkDeleteValidator validator;

    @Autowired(required = false)
    private UserService userService;

    // ============================================================================
    // CORE WHITELIST METHODS
    // ============================================================================

    @Override
    public boolean isIpWhitelisted(String clientIp, String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("ipAddress").is(clientIp)
                    .and("active").is(true));

            return mongoTemplate.exists(query, IpWhitelist.class);
        } catch (Exception e) {
            logger.error("Error checking IP whitelist status for {}: {}", clientIp, e.getMessage());
            return false;
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public boolean isIpWhitelisted(String clientIp, String tenantId, String userId, Set<String> userRoles) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Criteria baseCriteria = Criteria.where("ipAddress").is(clientIp)
                    .and("active").is(true);

            // Check global entries
            Query globalQuery = new Query(baseCriteria.and("scope").is(IpWhitelistScope.GLOBAL));
            if (mongoTemplate.exists(globalQuery, IpWhitelist.class)) {
                return true;
            }

            // Check user-specific entries
            Query userQuery = new Query(baseCriteria.and("allowedUserIds").in(userId));
            if (mongoTemplate.exists(userQuery, IpWhitelist.class)) {
                return true;
            }

            // Check role-specific entries
            if (userRoles != null && !userRoles.isEmpty()) {
                Query roleQuery = new Query(baseCriteria.and("allowedRoleIds").in(userRoles));
                if (mongoTemplate.exists(roleQuery, IpWhitelist.class)) {
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            logger.error("Error checking IP whitelist status for user {}: {}", userId, e.getMessage());
            return false;
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public ValidationResult validateUserIpAccess(String clientIp, String tenantId, String userId, Set<String> userRoles) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            logger.debug("Validating IP access: clientIp={}, userId={}, userRoles={}",
                    clientIp, userId, userRoles);

            Query query = new Query(Criteria.where("isActive").is(true));
            query.with(Sort.by(Sort.Direction.DESC, "priority", "scope"));

            List<IpWhitelist> allActiveEntries = mongoTemplate.find(query, IpWhitelist.class);

            if (allActiveEntries.isEmpty()) {
                logger.warn("No active IP whitelist entries found");
                return ValidationResult.denied(
                        "No active IP whitelist entries configured",
                        "NO_ENTRIES"
                );
            }

            List<IpWhitelist> matchingEntries = allActiveEntries.stream()
                    .filter(entry -> ipMatchesEntry(clientIp, entry.getIpAddress()))
                    .collect(Collectors.toList());

            logger.debug("Found {} matching IP entries", matchingEntries.size());

            if (matchingEntries.isEmpty()) {
                logger.warn("Client IP {} did not match any configured IPs", clientIp);
                return ValidationResult.denied(
                        "No IP whitelist entries found for IP: " + clientIp,
                        "NO_WHITELIST_ENTRY"
                );
            }

            for (IpWhitelist entry : matchingEntries) {
                if (entry.appliesToUser(userId, userRoles)) {
                    updateLastAccess(entry.getId(), tenantId, clientIp, userId);

                    logger.info("Access granted via {} scope", entry.getScope());
                    return ValidationResult.allowed(
                            "Access granted via " + entry.getScope() + " scope",
                            entry,
                            entry.getScope()
                    );
                }
            }

            return ValidationResult.denied(
                    "IP whitelisted but does not match user/role criteria",
                    "USER_ROLE_MISMATCH"
            );

        } catch (Exception e) {
            logger.error("Error validating IP access: {}", e.getMessage(), e);
            return ValidationResult.denied(
                    "Validation error: " + e.getMessage(),
                    "VALIDATION_ERROR"
            );
        } finally {
            TenantContext.clear();
        }
    }

    // ============================================================================
    // CRUD OPERATIONS
    // ============================================================================

    @Override
    public IpWhitelist create(IpWhitelist ipWhitelist, String tenantId, String createdBy) {
        try {
            if (!isValidIpAddress(ipWhitelist.getIpAddress())) {
                throw new IllegalArgumentException("Invalid IP address format: " + ipWhitelist.getIpAddress());
            }

            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            ipWhitelist.setTenantId(tenantId);
            ipWhitelist.setCreatedBy(createdBy);
            ipWhitelist.setCreatedAt(LocalDateTime.now());
            ipWhitelist.setUpdatedAt(LocalDateTime.now());
            ipWhitelist.setActive(true);

            if (ipWhitelist.getAccessCount() == null) {
                ipWhitelist.setAccessCount(0L);
            }

            if (ipWhitelist.getPriority() == null || ipWhitelist.getPriority() == 0) {
                ipWhitelist.setPriority(ipWhitelist.getScope().getPriorityOrder());
            }

            IpWhitelist saved = mongoTemplate.save(ipWhitelist);

            logger.info("Created IP whitelist entry: id={}, ip={}, scope={}",
                    saved.getId(), saved.getIpAddress(), saved.getScope());

            return saved;

        } catch (Exception e) {
            logger.error("Error creating IP whitelist entry: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create IP whitelist entry: " + e.getMessage(), e);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public Optional<IpWhitelist> getById(String id, String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("id").is(id));
            IpWhitelist result = mongoTemplate.findOne(query, IpWhitelist.class);

            return Optional.ofNullable(result);
        } catch (Exception e) {
            logger.error("Error retrieving IP whitelist entry by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public Page<IpWhitelist> getAllByTenant(String tenantId, Pageable pageable) {
        try {
            TenantContext.setCurrentTenant(tenantId);

            logger.debug("Fetching IP whitelist entries - Page: {}, Size: {}",
                    pageable.getPageNumber(), pageable.getPageSize());

            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query();
            query.with(pageable);
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));

            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
            long total = mongoTemplate.count(new Query(), IpWhitelist.class);

            logger.debug("Found {} entries, {} total", entries.size(), total);

            return new PageImpl<>(entries, pageable, total);

        } catch (Exception e) {
            logger.error("Error retrieving IP whitelist entries: {}", e.getMessage(), e);
            return Page.empty(pageable);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public IpWhitelist update(String id, IpWhitelist ipWhitelist, String tenantId, String updatedBy) {
        try {
            if (ipWhitelist.getIpAddress() != null && !isValidIpAddress(ipWhitelist.getIpAddress())) {
                throw new IllegalArgumentException("Invalid IP address format: " + ipWhitelist.getIpAddress());
            }

            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("id").is(id));

            Update update = new Update()
                    .set("description", ipWhitelist.getDescription())
                    .set("isActive", ipWhitelist.isActive())
                    .set("updatedBy", updatedBy)
                    .set("updatedAt", LocalDateTime.now())
                    .set("notes", ipWhitelist.getNotes())
                    .set("ipAddress", ipWhitelist.getIpAddress());

            mongoTemplate.updateFirst(query, update, IpWhitelist.class);

            logger.info("Updated IP whitelist entry {} by user {}", id, updatedBy);

            return getById(id, tenantId)
                    .orElseThrow(() -> new RuntimeException("Entry not found after update"));
        } catch (Exception e) {
            logger.error("Error updating IP whitelist entry {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update IP whitelist entry", e);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public void delete(String id, String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("id").is(id));
            mongoTemplate.remove(query, IpWhitelist.class);

            logger.info("Deleted IP whitelist entry {}", id);
        } catch (Exception e) {
            logger.error("Error deleting IP whitelist entry {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete IP whitelist entry", e);
        } finally {
            TenantContext.clear();
        }
    }

    // ============================================================================
    // BULK OPERATIONS - Add missing methods: bulkSoftDelete, deleteByScope, deleteInactiveEntries, previewBulkDelete
    // ============================================================================

    @Override
    @Transactional
    public BulkDeleteIpResponse bulkDelete(List<String> ids, String tenantId, String userId,
                                           boolean forceDelete, boolean skipValidation, String reason) {
        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
        response.setTotalRequested(ids.size());
        response.setPerformedBy(userId);
        response.setTimestamp(LocalDateTime.now());

        List<String> deletedIds = new ArrayList<>();
        List<BulkDeleteIpResponse.FailedDeletion> failedDeletions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query fetchQuery = new Query(Criteria.where("id").in(ids));
            List<IpWhitelist> entriesToDelete = mongoTemplate.find(fetchQuery, IpWhitelist.class);

            if (entriesToDelete.isEmpty()) {
                response.setFailedToDelete(ids.size());
                for (String id : ids) {
                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
                            id, "unknown", "Entry not found", "NOT_FOUND"));
                }
                response.setFailedDeletions(failedDeletions);
                logger.warn("Bulk delete attempted but no entries found");
                return response;
            }

            if (!skipValidation) {
                String userRole = getUserRole(userId);
                ValidationResult validationResult = validator.validateBulkDelete(
                        entriesToDelete, forceDelete, false, userRole);

                if (validationResult.hasWarnings()) {
                    warnings.addAll(validationResult.getWarnings());
                }

                if (validationResult.hasErrors()) {
                    response.setFailedToDelete(ids.size());
                    for (ValidationResult.ValidationError error : validationResult.getErrors()) {
                        for (IpWhitelist entry : entriesToDelete) {
                            failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
                                    entry.getId(), entry.getIpAddress(),
                                    error.getMessage(), error.getCode()));
                        }
                    }
                    response.setFailedDeletions(failedDeletions);
                    logger.warn("Bulk delete validation failed: {}", validationResult.getErrors());
                    return response;
                }
            }

            for (IpWhitelist entry : entriesToDelete) {
                try {
                    if (!canDeleteEntry(entry, userId, forceDelete)) {
                        failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
                                entry.getId(), entry.getIpAddress(),
                                "Insufficient permissions", "PERMISSION_DENIED"));
                        continue;
                    }

                    Query deleteQuery = new Query(Criteria.where("id").is(entry.getId()));
                    mongoTemplate.remove(deleteQuery, IpWhitelist.class);

                    deletedIds.add(entry.getId());
                    logger.info("Bulk deleted IP whitelist entry {} by user {}",
                            entry.getId(), userId);

                } catch (Exception e) {
                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
                            entry.getId(), entry.getIpAddress(),
                            e.getMessage(), "DELETE_ERROR"));
                    logger.error("Failed to delete entry {}: {}", entry.getId(), e.getMessage(), e);
                }
            }

            response.setSuccessfullyDeleted(deletedIds.size());
            response.setFailedToDelete(failedDeletions.size());
            response.setDeletedIds(deletedIds);
            response.setFailedDeletions(failedDeletions);
            response.setWarnings(warnings);

            Map<String, Object> summary = createDeletionSummary(entriesToDelete, deletedIds, reason);
            response.setDeletionSummary(summary);

            logger.info("Bulk delete completed: {} succeeded, {} failed",
                    deletedIds.size(), failedDeletions.size());

        } catch (Exception e) {
            logger.error("Error during bulk delete operation: {}", e.getMessage(), e);
            response.setFailedToDelete(response.getTotalRequested());
            response.setSuccessfullyDeleted(0);
            throw new RuntimeException("Bulk delete transaction failed", e);
        } finally {
            TenantContext.clear();
        }

        return response;
    }

    @Override
    @Transactional
    public BulkDeleteIpResponse bulkSoftDelete(List<String> ids, String tenantId, String userId, String reason) {
        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
        response.setTotalRequested(ids.size());
        response.setPerformedBy(userId);
        response.setTimestamp(LocalDateTime.now());

        List<String> deactivatedIds = new ArrayList<>();
        List<BulkDeleteIpResponse.FailedDeletion> failedDeletions = new ArrayList<>();

        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            for (String id : ids) {
                try {
                    Query query = new Query(Criteria.where("id").is(id));
                    Update update = new Update()
                            .set("active", false)
                            .set("updatedBy", userId)
                            .set("updatedAt", LocalDateTime.now())
                            .set("notes", (reason != null ? "Deactivated: " + reason : "Bulk deactivated"));

                    com.mongodb.client.result.UpdateResult result = mongoTemplate.updateFirst(query, update, IpWhitelist.class);

                    if (result.getMatchedCount() > 0) {
                        deactivatedIds.add(id);
                        logger.info("Bulk deactivated IP whitelist entry {} by user {}", id, userId);
                    } else {
                        failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
                                id, "unknown", "Entry not found", "NOT_FOUND"));
                    }

                } catch (Exception e) {
                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
                            id, "unknown", e.getMessage(), "UPDATE_ERROR"));
                    logger.error("Failed to deactivate entry {}: {}", id, e.getMessage(), e);
                }
            }

            response.setSuccessfullyDeleted(deactivatedIds.size());
            response.setFailedToDelete(failedDeletions.size());
            response.setDeletedIds(deactivatedIds);
            response.setFailedDeletions(failedDeletions);

            logger.info("Bulk soft delete completed: {} deactivated, {} failed",
                    deactivatedIds.size(), failedDeletions.size());

        } catch (Exception e) {
            logger.error("Error during bulk soft delete operation: {}", e.getMessage(), e);
            response.setFailedToDelete(response.getTotalRequested());
            response.setSuccessfullyDeleted(0);
            throw new RuntimeException("Bulk soft delete transaction failed", e);
        } finally {
            TenantContext.clear();
        }

        return response;
    }

    @Override
    @Transactional
    public BulkDeleteIpResponse deleteByScope(IpWhitelistScope scope, String tenantId, String userId,
                                              boolean forceDelete, String reason) {
        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
        response.setPerformedBy(userId);
        response.setTimestamp(LocalDateTime.now());

        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query findQuery = new Query(Criteria.where("scope").is(scope));
            List<IpWhitelist> entriesToDelete = mongoTemplate.find(findQuery, IpWhitelist.class);

            response.setTotalRequested(entriesToDelete.size());

            if (entriesToDelete.isEmpty()) {
                response.setSuccessfullyDeleted(0);
                response.setFailedToDelete(0);
                logger.info("No entries found for scope {}", scope);
                return response;
            }

            List<String> ids = entriesToDelete.stream()
                    .map(IpWhitelist::getId)
                    .collect(Collectors.toList());

            return bulkDelete(ids, tenantId, userId, forceDelete, false, reason);

        } catch (Exception e) {
            logger.error("Error during delete by scope {}: {}", scope, e.getMessage(), e);
            response.setFailedToDelete(response.getTotalRequested());
            response.setSuccessfullyDeleted(0);
            throw new RuntimeException("Delete by scope transaction failed", e);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public BulkDeleteIpResponse deleteInactiveEntries(String tenantId, int olderThanDays, String userId) {
        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
        response.setPerformedBy(userId);
        response.setTimestamp(LocalDateTime.now());

        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays);
            Query findQuery = new Query(Criteria.where("active").is(false)
                    .and("updatedAt").lt(cutoffDate));

            List<IpWhitelist> entriesToDelete = mongoTemplate.find(findQuery, IpWhitelist.class);
            response.setTotalRequested(entriesToDelete.size());

            if (entriesToDelete.isEmpty()) {
                response.setSuccessfullyDeleted(0);
                response.setFailedToDelete(0);
                logger.info("No inactive entries older than {} days found", olderThanDays);
                return response;
            }

            Query deleteQuery = new Query(Criteria.where("active").is(false)
                    .and("updatedAt").lt(cutoffDate));

            com.mongodb.client.result.DeleteResult result = mongoTemplate.remove(deleteQuery, IpWhitelist.class);

            response.setSuccessfullyDeleted((int) result.getDeletedCount());
            response.setFailedToDelete(0);

            List<String> deletedIds = entriesToDelete.stream()
                    .map(IpWhitelist::getId)
                    .collect(Collectors.toList());
            response.setDeletedIds(deletedIds);

            logger.info("Cleaned up {} inactive IP whitelist entries older than {} days",
                    result.getDeletedCount(), olderThanDays);

        } catch (Exception e) {
            logger.error("Error during cleanup of inactive entries: {}", e.getMessage(), e);
            response.setFailedToDelete(response.getTotalRequested());
            response.setSuccessfullyDeleted(0);
            throw new RuntimeException("Cleanup transaction failed", e);
        } finally {
            TenantContext.clear();
        }

        return response;
    }

    @Override
    public BulkDeletePreview previewBulkDelete(List<String> ids, String tenantId, String userId) {
        BulkDeletePreview preview = new BulkDeletePreview();

        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("id").in(ids));
            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);

            preview.setTotalEntries(entries.size());

            List<BulkDeletePreview.PreviewEntry> previewEntries = entries.stream()
                    .map(this::createPreviewEntry)
                    .collect(Collectors.toList());

            preview.setEntries(previewEntries);

            List<BulkDeletePreview.PreviewEntry> criticalEntries = previewEntries.stream()
                    .filter(BulkDeletePreview.PreviewEntry::isCritical)
                    .collect(Collectors.toList());

            preview.setCriticalEntries(criticalEntries);
            preview.setRequiresForceDelete(!criticalEntries.isEmpty());

            int activeCount = (int) entries.stream().filter(IpWhitelist::isActive).count();
            preview.setActiveEntries(activeCount);
            preview.setInactiveEntries(entries.size() - activeCount);

            Map<IpWhitelistScope, Integer> scopeSummary = entries.stream()
                    .collect(Collectors.groupingBy(IpWhitelist::getScope,
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
            preview.setScopeSummary(scopeSummary);

            Map<IpWhitelistType, Integer> typeSummary = entries.stream()
                    .collect(Collectors.groupingBy(IpWhitelist::getType,
                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
            preview.setTypeSummary(typeSummary);

            String userRole = getUserRole(userId);
            ValidationResult validationResult = validator.validateBulkDelete(entries, false, false, userRole);

            List<String> warnings = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            if (validationResult.hasWarnings()) {
                warnings.addAll(validationResult.getWarnings());
            }

            if (validationResult.hasErrors()) {
                errors.addAll(validationResult.getErrors().stream()
                        .map(error -> error.getCode() + ": " + error.getMessage())
                        .collect(Collectors.toList()));
            }

            preview.setWarnings(warnings);
            preview.setErrors(errors);
            preview.setCanProceed(!validationResult.hasErrors());

            preview.setImpactAssessment(generateImpactAssessment(entries));

        } catch (Exception e) {
            logger.error("Error generating bulk delete preview: {}", e.getMessage(), e);
            preview.setCanProceed(false);
            preview.setErrors(Collections.singletonList("Preview generation failed: " + e.getMessage()));
        } finally {
            TenantContext.clear();
        }

        return preview;
    }

    // ============================================================================
    // STATISTICS
    // ============================================================================

    @Override
    public Map<String, Object> getStatistics(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }

        try {
            TenantContext.setCurrentTenant(tenantId);
            logger.debug("Fetching IP whitelist statistics");

            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.group("isActive").count().as("count"),
                    Aggregation.sort(Sort.Direction.DESC, "_id")
            );

            AggregationResults<ActiveStats> result = mongoTemplateProvider.getMongoTemplate()
                    .aggregate(agg, "ip_whitelist", ActiveStats.class);

            long activeCount = 0;
            long inactiveCount = 0;
            long nullCount = 0;

            for (ActiveStats stat : result.getMappedResults()) {
                Boolean isActive = stat.getId();
                long count = stat.getCount();

                if (Boolean.TRUE.equals(isActive)) {
                    activeCount = count;
                } else if (Boolean.FALSE.equals(isActive)) {
                    inactiveCount = count;
                } else {
                    nullCount = count;
                    logger.warn("Found {} entries with null 'active' status", count);
                }
            }

            long total = activeCount + inactiveCount + nullCount;

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("tenantId", tenantId);
            stats.put("totalEntries", total);
            stats.put("activeEntries", activeCount);
            stats.put("inactiveEntries", inactiveCount);

            if (nullCount > 0) {
                stats.put("undefinedStatusEntries", nullCount);
            }

            logger.info("Statistics: total={}, active={}, inactive={}",
                    total, activeCount, inactiveCount);

            return stats;

        } catch (Exception e) {
            logger.error("Failed to retrieve statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Database error while fetching statistics", e);
        } finally {
            TenantContext.clear();
        }
    }

    // ============================================================================
    // ADVANCED CREATION METHODS
    // ============================================================================

    @Override
    public IpWhitelist createUserSpecific(String tenantId, String ipAddress, String description,
                                          Set<String> userIds, String createdBy, String userSpecString) {
        IpWhitelist ipWhitelist = new IpWhitelist();
        ipWhitelist.setIpAddress(ipAddress);
        ipWhitelist.setDescription(description);
        ipWhitelist.setScope(IpWhitelistScope.USER_SPECIFIC);
//        ipWhitelist.setType(IpWhitelistType.USER_SPECIFIC);

        switch (userSpecString){
            case "WILDCARD":
            {
                ipWhitelist.setType(IpWhitelistType.WILDCARD);
                break;
            }
            case "IP_RANGE" : {
                ipWhitelist.setType(IpWhitelistType.IP_RANGE);
                break;
            }
            case "SINGLE_IP": {
                ipWhitelist.setType(IpWhitelistType.SINGLE_IP);
                break;
            }
        }
        ipWhitelist.setAllowedUserIds(userIds != null ? new HashSet<>(userIds) : new HashSet<>());
        ipWhitelist.setPriority(IpWhitelistScope.USER_SPECIFIC.getPrecedence() / 100);

        return create(ipWhitelist, tenantId, createdBy);
    }

    @Override
    public IpWhitelist createRoleSpecific(String tenantId, String ipAddress, String description,
                                          Set<String> roleIds, String createdBy, String roleSpecString) {
        IpWhitelist ipWhitelist = new IpWhitelist();
        ipWhitelist.setIpAddress(ipAddress);
        ipWhitelist.setDescription(description);
        ipWhitelist.setScope(IpWhitelistScope.ROLE_SPECIFIC);

        switch (roleSpecString){
            case "WILDCARD":
            {
                ipWhitelist.setType(IpWhitelistType.WILDCARD);
                break;
            }
            case "IP_RANGE" : {
                ipWhitelist.setType(IpWhitelistType.IP_RANGE);
                break;
            }
            case "SINGLE_IP": {
                ipWhitelist.setType(IpWhitelistType.SINGLE_IP);
                break;
            }
        }


//        ipWhitelist.setType(IpWhitelistType.ROLE_SPECIFIC);
        ipWhitelist.setAllowedRoleIds(roleIds != null ? new HashSet<>(roleIds) : new HashSet<>());
        ipWhitelist.setPriority(IpWhitelistScope.ROLE_SPECIFIC.getPrecedence() / 100);

        return create(ipWhitelist, tenantId, createdBy);
    }

    @Override
    public IpWhitelist createHybrid(String tenantId, String ipAddress, String description,
                                    Set<String> userIds, Set<String> roleIds, String createdBy) {
        IpWhitelist ipWhitelist = new IpWhitelist();
        ipWhitelist.setIpAddress(ipAddress);
        ipWhitelist.setDescription(description);
        ipWhitelist.setScope(IpWhitelistScope.HYBRID);
        ipWhitelist.setType(IpWhitelistType.HYBRID);
        ipWhitelist.setAllowedUserIds(userIds != null ? new HashSet<>(userIds) : new HashSet<>());
        ipWhitelist.setAllowedRoleIds(roleIds != null ? new HashSet<>(roleIds) : new HashSet<>());
        ipWhitelist.setPriority(IpWhitelistScope.HYBRID.getPrecedence() / 100);

        return create(ipWhitelist, tenantId, createdBy);
    }

    // ============================================================================
    // ADVANCED QUERYING METHODS
    // ============================================================================

    @Override
    public Page<IpWhitelist> getEntriesByScope(String tenantId, IpWhitelistScope scope, Pageable pageable) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("scope").is(scope));
            query.with(pageable);

            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
            long total = mongoTemplate.count(Query.query(Criteria.where("scope").is(scope)), IpWhitelist.class);

            return new PageImpl<>(entries, pageable, total);
        } catch (Exception e) {
            logger.error("Error fetching entries by scope {}: {}", scope, e.getMessage(), e);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public Page<IpWhitelist> getEntriesForUserId(String tenantId, String userId, Pageable pageable) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Criteria criteria = Criteria.where("allowedUserIds").in(userId);

            Query query = new Query(criteria);
            query.with(pageable);

            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
            long total = mongoTemplate.count(Query.query(criteria), IpWhitelist.class);

            return new PageImpl<>(entries, pageable, total);
        } catch (Exception e) {
            logger.error("Error fetching entries for user {}: {}", userId, e.getMessage(), e);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public Page<IpWhitelist> getEntriesForRoleId(String tenantId, String roleId, Pageable pageable) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Criteria criteria = Criteria.where("allowedRoleIds").in(roleId);

            Query query = new Query(criteria);
            query.with(pageable);

            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
            long total = mongoTemplate.count(Query.query(criteria), IpWhitelist.class);

            return new PageImpl<>(entries, pageable, total);
        } catch (Exception e) {
            logger.error("Error fetching entries for role {}: {}", roleId, e.getMessage(), e);
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public List<IpWhitelist> getEntriesForUser(String tenantId, String userId, Set<String> userRoles) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            List<Criteria> criteriaList = new ArrayList<>();

            // Global and Tenant scope entries
            criteriaList.add(Criteria.where("scope").in(IpWhitelistScope.GLOBAL, IpWhitelistScope.TENANT, IpWhitelistScope.ADMIN));

            // User-specific entries
            criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.USER_SPECIFIC)
                    .and("allowedUserIds").in(userId));

            // Role-specific entries
            if (userRoles != null && !userRoles.isEmpty()) {
                criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.ROLE_SPECIFIC)
                        .and("allowedRoleIds").in(userRoles));
            }

            // Hybrid entries
            List<Criteria> hybridCriteria = new ArrayList<>();
            hybridCriteria.add(Criteria.where("allowedUserIds").in(userId));
            if (userRoles != null && !userRoles.isEmpty()) {
                hybridCriteria.add(Criteria.where("allowedRoleIds").in(userRoles));
            }
            criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.HYBRID)
                    .orOperator(hybridCriteria.toArray(new Criteria[0])));

            Criteria mainCriteria = Criteria.where("active").is(true)
                    .orOperator(criteriaList.toArray(new Criteria[0]));

            Query query = new Query(mainCriteria);
            query.with(Sort.by(Sort.Direction.DESC, "priority", "scope"));

            return mongoTemplate.find(query, IpWhitelist.class);

        } catch (Exception e) {
            logger.error("Error fetching entries for user {} with roles {}: {}",
                    userId, userRoles, e.getMessage(), e);
            return Collections.emptyList();
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public Map<String, Object> getAdvancedStatistics(String tenantId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
            Map<String, Object> stats = new HashMap<>();

            long total = mongoTemplate.count(new Query(), IpWhitelist.class);
            long active = mongoTemplate.count(Query.query(Criteria.where("active").is(true)), IpWhitelist.class);

            stats.put("totalEntries", total);
            stats.put("activeEntries", active);
            stats.put("inactiveEntries", total - active);

            Map<String, Long> scopeStats = new HashMap<>();
            for (IpWhitelistScope scope : IpWhitelistScope.values()) {
                long count = mongoTemplate.count(
                        Query.query(Criteria.where("scope").is(scope)),
                        IpWhitelist.class);
                scopeStats.put(scope.name(), count);
            }
            stats.put("byScope", scopeStats);

            Map<String, Long> typeStats = new HashMap<>();
            for (IpWhitelistType type : IpWhitelistType.values()) {
                long count = mongoTemplate.count(
                        Query.query(Criteria.where("type").is(type)),
                        IpWhitelist.class);
                typeStats.put(type.name(), count);
            }
            stats.put("byType", typeStats);

            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.group()
                            .sum("accessCount").as("totalAccess")
                            .avg("accessCount").as("avgAccess")
                            .count().as("entriesWithAccess")
            );

            AggregationResults<Map> accessResults = mongoTemplate.aggregate(aggregation, "ip_whitelist", Map.class);
            Map accessStats = accessResults.getUniqueMappedResult();
            if (accessStats != null) {
                stats.put("accessStats", accessStats);
            }

            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            long recentlyAccessed = mongoTemplate.count(
                    Query.query(Criteria.where("lastAccessedAt").gte(weekAgo)),
                    IpWhitelist.class);
            stats.put("recentlyAccessedCount", recentlyAccessed);

            return stats;

        } catch (Exception e) {
            logger.error("Error retrieving advanced statistics: {}", e.getMessage(), e);
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("error", "Unable to retrieve statistics: " + e.getMessage());
            return errorStats;
        } finally {
            TenantContext.clear();
        }
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    @Override
    public void updateLastAccess(String entryId, String tenantId, String clientIp, String userId) {
        try {
            TenantContext.setCurrentTenant(tenantId);
            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();

            Query query = new Query(Criteria.where("id").is(entryId));

            Update update = new Update()
                    .set("lastAccessedAt", LocalDateTime.now())
                    .set("lastAccessedBy", userId)
                    .set("lastAccessedIp", clientIp)
                    .inc("accessCount", 1);

            mongoTemplate.updateFirst(query, update, IpWhitelist.class);

        } catch (Exception e) {
            logger.warn("Failed to update last access for entry {}: {}", entryId, e.getMessage());
        } finally {
            TenantContext.clear();
        }
    }

    private boolean canDeleteEntry(IpWhitelist entry, String userId, boolean forceDelete) {
        if (entry.getScope() == IpWhitelistScope.ADMIN || entry.getScope() == IpWhitelistScope.GLOBAL) {
            String userRole = getUserRole(userId);
            return "SUPERADMIN".equals(userRole);
        }

        if (entry.isCriticalIpRange() && !forceDelete) {
            return false;
        }

        return true;
    }

    private String getUserRole(String userId) {
        try {
            if (userService != null) {
                return userService.getUserRole(userId);
            }
            logger.warn("UserService not available, using default role");
            return "USER";
        } catch (Exception e) {
            logger.error("Error retrieving user role for {}: {}", userId, e.getMessage(), e);
            return "USER";
        }
    }

    private BulkDeletePreview.PreviewEntry createPreviewEntry(IpWhitelist entry) {
        BulkDeletePreview.PreviewEntry previewEntry = new BulkDeletePreview.PreviewEntry(
                entry.getId(),
                entry.getIpAddress(),
                entry.getDescription(),
                entry.getScope(),
                entry.getType(),
                entry.isActive()
        );

        if (entry.getAllowedUserIds() != null) {
            previewEntry.setAffectedUsers(new ArrayList<>(entry.getAllowedUserIds()));
        }
        if (entry.getAllowedRoleIds() != null) {
            previewEntry.setAffectedRoles(new ArrayList<>(entry.getAllowedRoleIds()));
        }

        return previewEntry;
    }

    private Map<String, Object> createDeletionSummary(List<IpWhitelist> originalEntries, List<String> deletedIds, String reason) {
        Map<String, Object> summary = new HashMap<>();

        List<IpWhitelist> deletedEntries = originalEntries.stream()
                .filter(entry -> deletedIds.contains(entry.getId()))
                .collect(Collectors.toList());

        Map<IpWhitelistScope, Long> scopeCounts = deletedEntries.stream()
                .collect(Collectors.groupingBy(IpWhitelist::getScope, Collectors.counting()));
        summary.put("deletedByScope", scopeCounts);

        Map<IpWhitelistType, Long> typeCounts = deletedEntries.stream()
                .collect(Collectors.groupingBy(IpWhitelist::getType, Collectors.counting()));
        summary.put("deletedByType", typeCounts);

        long criticalCount = deletedEntries.stream()
                .filter(IpWhitelist::isCriticalIpRange)
                .count();
        summary.put("criticalEntriesDeleted", criticalCount);

        summary.put("reason", reason != null ? reason : "Bulk deletion");
        summary.put("timestamp", LocalDateTime.now());

        return summary;
    }

    private String generateImpactAssessment(List<IpWhitelist> entries) {
        int criticalCount = (int) entries.stream().filter(IpWhitelist::isCriticalIpRange).count();
        int activeCount = (int) entries.stream().filter(IpWhitelist::isActive).count();
        int userSpecificCount = (int) entries.stream().filter(e -> e.getScope() == IpWhitelistScope.USER_SPECIFIC).count();

        StringBuilder assessment = new StringBuilder();

        if (criticalCount > 0) {
            assessment.append(String.format("%d critical IP ranges will be deleted. ", criticalCount));
        }

        if (activeCount > 0) {
            assessment.append(String.format("%d active entries will be deleted, potentially affecting current access. ", activeCount));
        }

        if (userSpecificCount > 0) {
            assessment.append(String.format("%d user-specific entries may affect individual user sessions. ", userSpecificCount));
        }

        if (assessment.length() == 0) {
            assessment.append("Low impact - mostly inactive or non-critical entries.");
        } else {
            assessment.append("Review carefully before proceeding.");
        }

        return assessment.toString();
    }

    private boolean ipMatchesEntry(String clientIp, String entryIp) {
        try {
            if ("*".equals(entryIp)) return true;
            if (clientIp.equals(entryIp)) return true;
            if ("0.0.0.0/0".equals(entryIp) || "::/0".equals(entryIp)) return true;

            if (entryIp.contains("/")) {
                return isIpInCidrRange(clientIp, entryIp);
            }

            return false;
        } catch (Exception e) {
            logger.warn("Error matching IP {} against entry {}: {}",
                    clientIp, entryIp, e.getMessage());
            return false;
        }
    }

    private boolean isIpInCidrRange(String ip, String cidr) {
        try {
            SubnetUtils subnet = new SubnetUtils(cidr);
            subnet.setInclusiveHostCount(true);
            return subnet.getInfo().isInRange(ip);
        } catch (Exception e) {
            logger.warn("Error checking CIDR range: {}", e.getMessage());
            return false;
        }
    }

    private boolean isValidIpAddress(String ipAddress) {
        if (ipAddress == null || ipAddress.trim().isEmpty()) return false;
        if ("*".equals(ipAddress)) return true;
        if ("0.0.0.0/0".equals(ipAddress) || "::/0".equals(ipAddress)) return true;
        if (ipAddress.contains("/")) return CIDR_PATTERN.matcher(ipAddress).matches();
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ActiveStats {
        private Boolean id;
        private long count;
    }
}
//package com.midas.consulting.service;
//
//import com.midas.consulting.config.database.MongoTemplateProvider;
//import com.midas.consulting.controller.v1.response.BulkDeleteIpResponse;
//import com.midas.consulting.controller.v1.response.BulkDeletePreview;
//import com.midas.consulting.model.IpWhitelist;
//import com.midas.consulting.model.IpWhitelistScope;
//import com.midas.consulting.model.IpWhitelistType;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.apache.commons.net.util.SubnetUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.aggregation.Aggregation;
//import org.springframework.data.mongodb.core.aggregation.AggregationResults;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.core.query.Update;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
///**
// * Implementation of IpWhitelistService for database-per-tenant architecture.
// * TenantId is used ONLY for database routing, NOT in WHERE clauses.
// */
//@Service
//public class IpWhitelistServiceImpl implements IpWhitelistService {
//
//    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistServiceImpl.class);
//
//    private static final Pattern IPV4_PATTERN = Pattern.compile(
//            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
//    );
//    private static final Pattern CIDR_PATTERN = Pattern.compile(
//            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(3[0-2]|[12]?[0-9])$"
//    );
//
//    @Autowired
//    private MongoTemplateProvider mongoTemplateProvider;
//
//    @Autowired
//    private IpBulkDeleteValidator validator;
//
//    @Autowired(required = false)
//    private UserService userService;
//
//    // ============================================================================
//    // CORE WHITELIST METHODS - REMOVED tenantId from WHERE clauses
//    // ============================================================================
//
//    @Override
//    public boolean isIpWhitelisted(String clientIp, String tenantId) {
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ NO tenantId in query - already in correct database
//            Query query = new Query(Criteria.where("ipAddress").is(clientIp)
//                    .and("active").is(true));
//
//            return mongoTemplate.exists(query, IpWhitelist.class);
//        } catch (Exception e) {
//            logger.error("Error checking IP whitelist status for {}: {}", clientIp, e.getMessage());
//            return false;
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    @Override
//    public boolean isIpWhitelisted(String clientIp, String tenantId, String userId, Set<String> userRoles) {
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ NO tenantId in criteria - database already isolated
//            Criteria baseCriteria = Criteria.where("ipAddress").is(clientIp)
//                    .and("active").is(true);
//
//            // Check global entries
//            Query globalQuery = new Query(baseCriteria.and("scope").is(IpWhitelistScope.GLOBAL));
//            if (mongoTemplate.exists(globalQuery, IpWhitelist.class)) {
//                return true;
//            }
//
//            // Check user-specific entries
//            Query userQuery = new Query(baseCriteria.and("allowedUserIds").in(userId));
//            if (mongoTemplate.exists(userQuery, IpWhitelist.class)) {
//                return true;
//            }
//
//            // Check role-specific entries
//            if (userRoles != null && !userRoles.isEmpty()) {
//                Query roleQuery = new Query(baseCriteria.and("allowedRoleIds").in(userRoles));
//                if (mongoTemplate.exists(roleQuery, IpWhitelist.class)) {
//                    return true;
//                }
//            }
//
//            return false;
//        } catch (Exception e) {
//            logger.error("Error checking IP whitelist status for user {}: {}", userId, e.getMessage());
//            return false;
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    // ============================================================================
//    // VALIDATION METHOD - REMOVED tenantId from queries
//    // ============================================================================
//
//    @Override
//    public ValidationResult validateUserIpAccess(String clientIp, String tenantId, String userId, Set<String> userRoles) {
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            logger.debug("Validating IP access: clientIp={}, userId={}, userRoles={}",
//                    clientIp, userId, userRoles);
//
//            // ✅ Get ALL active entries - NO tenantId filter needed
//            Query query = new Query(Criteria.where("active").is(true));
//            query.with(Sort.by(Sort.Direction.DESC, "priority", "scope"));
//
//            List<IpWhitelist> allActiveEntries = mongoTemplate.find(query, IpWhitelist.class);
//
//            if (allActiveEntries.isEmpty()) {
//                logger.warn("No active IP whitelist entries found");
//                return ValidationResult.denied(
//                        "No active IP whitelist entries configured",
//                        "NO_ENTRIES"
//                );
//            }
//
//            // Filter in Java for IP matching
//            List<IpWhitelist> matchingEntries = allActiveEntries.stream()
//                    .filter(entry -> ipMatchesEntry(clientIp, entry.getIpAddress()))
//                    .collect(Collectors.toList());
//
//            logger.debug("Found {} matching IP entries", matchingEntries.size());
//
//            if (matchingEntries.isEmpty()) {
//                logger.warn("Client IP {} did not match any configured IPs", clientIp);
//                return ValidationResult.denied(
//                        "No IP whitelist entries found for IP: " + clientIp,
//                        "NO_WHITELIST_ENTRY"
//                );
//            }
//
//            // Check user/role permissions
//            for (IpWhitelist entry : matchingEntries) {
//                if (entry.appliesToUser(userId, userRoles)) {
//                    // Update access tracking
//                    updateLastAccess(entry.getId(), tenantId, clientIp, userId);
//
//                    logger.info("Access granted via {} scope", entry.getScope());
//                    return ValidationResult.allowed(
//                            "Access granted via " + entry.getScope() + " scope",
//                            entry,
//                            entry.getScope()
//                    );
//                }
//            }
//
//            return ValidationResult.denied(
//                    "IP whitelisted but does not match user/role criteria",
//                    "USER_ROLE_MISMATCH"
//            );
//
//        } catch (Exception e) {
//            logger.error("Error validating IP access: {}", e.getMessage(), e);
//            return ValidationResult.denied(
//                    "Validation error: " + e.getMessage(),
//                    "VALIDATION_ERROR"
//            );
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    // ============================================================================
//    // CRUD OPERATIONS - REMOVED tenantId from WHERE clauses
//    // ============================================================================
//
//    @Override
//    public IpWhitelist create(IpWhitelist ipWhitelist, String tenantId, String createdBy) {
//        try {
//            // Validate IP address
//            if (!isValidIpAddress(ipWhitelist.getIpAddress())) {
//                throw new IllegalArgumentException("Invalid IP address format: " + ipWhitelist.getIpAddress());
//            }
//
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // Set required fields - tenantId stored for reference only
//            ipWhitelist.setTenantId(tenantId);
//            ipWhitelist.setCreatedBy(createdBy);
//            ipWhitelist.setCreatedAt(LocalDateTime.now());
//            ipWhitelist.setUpdatedAt(LocalDateTime.now());
//            ipWhitelist.setActive(true);
//
//            if (ipWhitelist.getAccessCount() == null) {
//                ipWhitelist.setAccessCount(0L);
//            }
//
//            if (ipWhitelist.getPriority() == null || ipWhitelist.getPriority() == 0) {
//                ipWhitelist.setPriority(ipWhitelist.getScope().getPriorityOrder());
//            }
//
//            IpWhitelist saved = mongoTemplate.save(ipWhitelist);
//
//            logger.info("Created IP whitelist entry: id={}, ip={}, scope={}",
//                    saved.getId(), saved.getIpAddress(), saved.getScope());
//
//            return saved;
//
//        } catch (Exception e) {
//            logger.error("Error creating IP whitelist entry: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to create IP whitelist entry: " + e.getMessage(), e);
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    @Override
//    public Optional<IpWhitelist> getById(String id, String tenantId) {
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ Query by ID only - NO tenantId filter
//            Query query = new Query(Criteria.where("id").is(id));
//            IpWhitelist result = mongoTemplate.findOne(query, IpWhitelist.class);
//
//            return Optional.ofNullable(result);
//        } catch (Exception e) {
//            logger.error("Error retrieving IP whitelist entry by ID {}: {}", id, e.getMessage(), e);
//            return Optional.empty();
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    @Override
//    public Page<IpWhitelist> getAllByTenant(String tenantId, Pageable pageable) {
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            logger.debug("Fetching IP whitelist entries - Page: {}, Size: {}",
//                    pageable.getPageNumber(), pageable.getPageSize());
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ NO tenantId filter - all data in this DB belongs to the tenant
//            Query query = new Query();
//            query.with(pageable);
//            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
//
//            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
//            long total = mongoTemplate.count(new Query(), IpWhitelist.class);
//
//            logger.debug("Found {} entries, {} total", entries.size(), total);
//
//            return new PageImpl<>(entries, pageable, total);
//
//        } catch (Exception e) {
//            logger.error("Error retrieving IP whitelist entries: {}", e.getMessage(), e);
//            return Page.empty(pageable);
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    @Override
//    public IpWhitelist update(String id, IpWhitelist ipWhitelist, String tenantId, String updatedBy) {
//        try {
//            if (ipWhitelist.getIpAddress() != null && !isValidIpAddress(ipWhitelist.getIpAddress())) {
//                throw new IllegalArgumentException("Invalid IP address format: " + ipWhitelist.getIpAddress());
//            }
//
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ Query by ID only - NO tenantId filter
//            Query query = new Query(Criteria.where("id").is(id));
//
//            Update update = new Update()
//                    .set("description", ipWhitelist.getDescription())
//                    .set("active", ipWhitelist.isActive())
//                    .set("updatedBy", updatedBy)
//                    .set("updatedAt", LocalDateTime.now())
//                    .set("notes", ipWhitelist.getNotes())
//                    .set("ipAddress", ipWhitelist.getIpAddress());
//
//            mongoTemplate.updateFirst(query, update, IpWhitelist.class);
//
//            logger.info("Updated IP whitelist entry {} by user {}", id, updatedBy);
//
//            return getById(id, tenantId)
//                    .orElseThrow(() -> new RuntimeException("Entry not found after update"));
//        } catch (Exception e) {
//            logger.error("Error updating IP whitelist entry {}: {}", id, e.getMessage(), e);
//            throw new RuntimeException("Failed to update IP whitelist entry", e);
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    @Override
//    public void delete(String id, String tenantId) {
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ Query by ID only - NO tenantId filter
//            Query query = new Query(Criteria.where("id").is(id));
//            mongoTemplate.remove(query, IpWhitelist.class);
//
//            logger.info("Deleted IP whitelist entry {}", id);
//        } catch (Exception e) {
//            logger.error("Error deleting IP whitelist entry {}: {}", id, e.getMessage(), e);
//            throw new RuntimeException("Failed to delete IP whitelist entry", e);
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    // ============================================================================
//    // BULK OPERATIONS - REMOVED tenantId from WHERE clauses
//    // ============================================================================
//
//    @Override
//    @Transactional
//    public BulkDeleteIpResponse bulkDelete(List<String> ids, String tenantId, String userId,
//                                           boolean forceDelete, boolean skipValidation, String reason) {
//        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
//        response.setTotalRequested(ids.size());
//        response.setPerformedBy(userId);
//        response.setTimestamp(LocalDateTime.now());
//
//        List<String> deletedIds = new ArrayList<>();
//        List<BulkDeleteIpResponse.FailedDeletion> failedDeletions = new ArrayList<>();
//        List<String> warnings = new ArrayList<>();
//
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ Fetch entries by IDs only - NO tenantId filter
//            Query fetchQuery = new Query(Criteria.where("id").in(ids));
//            List<IpWhitelist> entriesToDelete = mongoTemplate.find(fetchQuery, IpWhitelist.class);
//
//            if (entriesToDelete.isEmpty()) {
//                response.setFailedToDelete(ids.size());
//                for (String id : ids) {
//                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
//                            id, "unknown", "Entry not found", "NOT_FOUND"));
//                }
//                response.setFailedDeletions(failedDeletions);
//                logger.warn("Bulk delete attempted but no entries found");
//                return response;
//            }
//
//            // Validate bulk delete if not skipped
//            if (!skipValidation) {
//                String userRole = getUserRole(userId);
//                ValidationResult validationResult = validator.validateBulkDelete(
//                        entriesToDelete, forceDelete, false, userRole);
//
//                if (validationResult.hasWarnings()) {
//                    warnings.addAll(validationResult.getWarnings());
//                }
//
//                if (validationResult.hasErrors()) {
//                    response.setFailedToDelete(ids.size());
//                    for (ValidationResult.ValidationError error : validationResult.getErrors()) {
//                        for (IpWhitelist entry : entriesToDelete) {
//                            failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
//                                    entry.getId(), entry.getIpAddress(),
//                                    error.getMessage(), error.getCode()));
//                        }
//                    }
//                    response.setFailedDeletions(failedDeletions);
//                    logger.warn("Bulk delete validation failed: {}", validationResult.getErrors());
//                    return response;
//                }
//            }
//
//            // Perform deletions
//            for (IpWhitelist entry : entriesToDelete) {
//                try {
//                    if (!canDeleteEntry(entry, userId, forceDelete)) {
//                        failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
//                                entry.getId(), entry.getIpAddress(),
//                                "Insufficient permissions", "PERMISSION_DENIED"));
//                        logger.warn("Permission denied for deleting entry {} by user {}",
//                                entry.getId(), userId);
//                        continue;
//                    }
//
//                    // ✅ Delete by ID only - NO tenantId filter
//                    Query deleteQuery = new Query(Criteria.where("id").is(entry.getId()));
//                    mongoTemplate.remove(deleteQuery, IpWhitelist.class);
//
//                    deletedIds.add(entry.getId());
//                    logger.info("Bulk deleted IP whitelist entry {} by user {}",
//                            entry.getId(), userId);
//
//                } catch (Exception e) {
//                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
//                            entry.getId(), entry.getIpAddress(),
//                            e.getMessage(), "DELETE_ERROR"));
//                    logger.error("Failed to delete entry {}: {}", entry.getId(), e.getMessage(), e);
//                }
//            }
//
//            // Build response
//            response.setSuccessfullyDeleted(deletedIds.size());
//            response.setFailedToDelete(failedDeletions.size());
//            response.setDeletedIds(deletedIds);
//            response.setFailedDeletions(failedDeletions);
//            response.setWarnings(warnings);
//
//            Map<String, Object> summary = createDeletionSummary(entriesToDelete, deletedIds, reason);
//            response.setDeletionSummary(summary);
//
//            logger.info("Bulk delete completed: {} succeeded, {} failed",
//                    deletedIds.size(), failedDeletions.size());
//
//        } catch (Exception e) {
//            logger.error("Error during bulk delete operation: {}", e.getMessage(), e);
//            response.setFailedToDelete(response.getTotalRequested());
//            response.setSuccessfullyDeleted(0);
//            throw new RuntimeException("Bulk delete transaction failed", e);
//        } finally {
//            TenantContext.clear();
//        }
//
//        return response;
//    }
//
//    // ============================================================================
//    // STATISTICS - REMOVED tenantId from WHERE clauses
//    // ============================================================================
//
//    @Override
//    public Map<String, Object> getStatistics(String tenantId) {
//        if (tenantId == null || tenantId.trim().isEmpty()) {
//            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
//        }
//
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            logger.debug("Fetching IP whitelist statistics");
//
//            // ✅ Aggregation WITHOUT tenantId filter
//            Aggregation agg = Aggregation.newAggregation(
//                    Aggregation.group("active").count().as("count"),
//                    Aggregation.sort(Sort.Direction.DESC, "_id")
//            );
//
//            AggregationResults<ActiveStats> result = mongoTemplateProvider.getMongoTemplate()
//                    .aggregate(agg, "ip_whitelist", ActiveStats.class);
//
//            long activeCount = 0;
//            long inactiveCount = 0;
//            long nullCount = 0;
//
//            for (ActiveStats stat : result.getMappedResults()) {
//                Boolean isActive = stat.getId();
//                long count = stat.getCount();
//
//                if (Boolean.TRUE.equals(isActive)) {
//                    activeCount = count;
//                } else if (Boolean.FALSE.equals(isActive)) {
//                    inactiveCount = count;
//                } else {
//                    nullCount = count;
//                    logger.warn("Found {} entries with null 'active' status", count);
//                }
//            }
//
//            long total = activeCount + inactiveCount + nullCount;
//
//            Map<String, Object> stats = new LinkedHashMap<>();
//            stats.put("tenantId", tenantId);
//            stats.put("totalEntries", total);
//            stats.put("activeEntries", activeCount);
//            stats.put("inactiveEntries", inactiveCount);
//
//            if (nullCount > 0) {
//                stats.put("undefinedStatusEntries", nullCount);
//            }
//
//            logger.info("Statistics: total={}, active={}, inactive={}",
//                    total, activeCount, inactiveCount);
//
//            return stats;
//
//        } catch (Exception e) {
//            logger.error("Failed to retrieve statistics: {}", e.getMessage(), e);
//            throw new RuntimeException("Database error while fetching statistics", e);
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    // ============================================================================
//    // HELPER METHODS
//    // ============================================================================
//
//    @Override
//    public void updateLastAccess(String entryId, String tenantId, String clientIp, String userId) {
//        try {
//            // Set tenant context for database routing
//            TenantContext.setCurrentTenant(tenantId);
//
//            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//
//            // ✅ Query by ID only - NO tenantId filter
//            Query query = new Query(Criteria.where("id").is(entryId));
//
//            Update update = new Update()
//                    .set("lastAccessedAt", LocalDateTime.now())
//                    .set("lastAccessedBy", userId)
//                    .set("lastAccessedIp", clientIp)
//                    .inc("accessCount", 1);
//
//            mongoTemplate.updateFirst(query, update, IpWhitelist.class);
//
//        } catch (Exception e) {
//            logger.warn("Failed to update last access for entry {}: {}", entryId, e.getMessage());
//        } finally {
//            TenantContext.clear();
//        }
//    }
//
//    private boolean canDeleteEntry(IpWhitelist entry, String userId, boolean forceDelete) {
//        if (entry.getScope() == IpWhitelistScope.ADMIN || entry.getScope() == IpWhitelistScope.GLOBAL) {
//            String userRole = getUserRole(userId);
//            return "SUPERADMIN".equals(userRole);
//        }
//
//        if (entry.isCriticalIpRange() && !forceDelete) {
//            return false;
//        }
//
//        return true;
//    }
//
//    private String getUserRole(String userId) {
//        try {
//            if (userService != null) {
//                return userService.getUserRole(userId);
//            }
//            logger.warn("UserService not available, using default role");
//            return "USER";
//        } catch (Exception e) {
//            logger.error("Error retrieving user role for {}: {}", userId, e.getMessage(), e);
//            return "USER";
//        }
//    }
//
//    private boolean ipMatchesEntry(String clientIp, String entryIp) {
//        try {
//            if ("*".equals(entryIp)) return true;
//            if (clientIp.equals(entryIp)) return true;
//            if ("0.0.0.0/0".equals(entryIp) || "::/0".equals(entryIp)) return true;
//
//            if (entryIp.contains("/")) {
//                return isIpInCidrRange(clientIp, entryIp);
//            }
//
//            return false;
//        } catch (Exception e) {
//            logger.warn("Error matching IP {} against entry {}: {}",
//                    clientIp, entryIp, e.getMessage());
//            return false;
//        }
//    }
//
//    private boolean isIpInCidrRange(String ip, String cidr) {
//        try {
//            SubnetUtils subnet = new SubnetUtils(cidr);
//            subnet.setInclusiveHostCount(true);
//            return subnet.getInfo().isInRange(ip);
//        } catch (Exception e) {
//            logger.warn("Error checking CIDR range: {}", e.getMessage());
//            return false;
//        }
//    }
//
//    private boolean isValidIpAddress(String ipAddress) {
//        if (ipAddress == null || ipAddress.trim().isEmpty()) return false;
//        if ("*".equals(ipAddress)) return true;
//        if ("0.0.0.0/0".equals(ipAddress) || "::/0".equals(ipAddress)) return true;
//        if (ipAddress.contains("/")) return CIDR_PATTERN.matcher(ipAddress).matches();
//        return IPV4_PATTERN.matcher(ipAddress).matches();
//    }
//
//    // ... (keep other helper methods like createDeletionSummary, etc. - apply same pattern)
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    private static class ActiveStats {
//        private Boolean id;
//        private long count;
//    }
//}
////package com.midas.consulting.service;
////
////import com.midas.consulting.config.database.MongoTemplateProvider;
////import com.midas.consulting.controller.v1.response.BulkDeleteIpResponse;
////import com.midas.consulting.controller.v1.response.BulkDeletePreview;
////import com.midas.consulting.model.IpWhitelist;
////import com.midas.consulting.model.IpWhitelistScope;
////import com.midas.consulting.model.IpWhitelistType;
////import lombok.AllArgsConstructor;
////import lombok.Data;
////import lombok.NoArgsConstructor;
////import org.apache.commons.net.util.SubnetUtils;
////import org.slf4j.Logger;
////import org.slf4j.LoggerFactory;
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.data.domain.Page;
////import org.springframework.data.domain.PageImpl;
////import org.springframework.data.domain.Pageable;
////import org.springframework.data.domain.Sort;
////import org.springframework.data.mongodb.core.MongoTemplate;
////import org.springframework.data.mongodb.core.aggregation.Aggregation;
////import org.springframework.data.mongodb.core.aggregation.AggregationResults;
////import org.springframework.data.mongodb.core.query.Criteria;
////import org.springframework.data.mongodb.core.query.Query;
////import org.springframework.data.mongodb.core.query.Update;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.time.LocalDateTime;
////import java.util.*;
////import java.util.regex.Pattern;
////import java.util.stream.Collectors;
////
/////**
//// * Implementation of IpWhitelistService that provides both basic (WhitelistService)
//// * and advanced IP whitelist functionality
//// */
////@Service
////public class IpWhitelistServiceImpl implements IpWhitelistService {
////
////    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistServiceImpl.class);
////    private static final Pattern IPV4_PATTERN = Pattern.compile(
////            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
////    );
////    private static final Pattern CIDR_PATTERN = Pattern.compile(
////            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/(3[0-2]|[12]?[0-9])$"
////    );
////
////    @Autowired
////    private MongoTemplateProvider mongoTemplateProvider;
////
////    @Autowired
////    private IpBulkDeleteValidator validator;
////
////    @Autowired(required = false)
////    private UserService userService;
////
////    // Critical IP ranges that require special handling
////    private static final Set<String> CRITICAL_IP_RANGES =
////            Collections.unmodifiableSet(new HashSet<String>() {{
////                add("127.0.0.1");
////                add("::1");
////                add("0.0.0.0/0");
////                add("::/0");
////                add("10.0.0.0/8");
////                add("172.16.0.0/12");
////                add("192.168.0.0/16");
////            }});
////
////    // ============================================================================
////    // WHITELIST SERVICE METHODS (Base Interface Implementation)
////    // ============================================================================
////
////    @Override
////    public boolean isIpWhitelisted(String clientIp, String tenantId) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////            Query query = new Query(Criteria.where("tenantId").is(tenantId)
////                    .and("ipAddress").is(clientIp)
////                    .and("active").is(true));
////            return mongoTemplate.exists(query, IpWhitelist.class);
////        } catch (Exception e) {
////            logger.error("Error checking IP whitelist status for {}: {}", clientIp, e.getMessage());
////            return false;
////        }
////    }
////
////    @Override
////    public boolean isIpWhitelisted(String clientIp, String tenantId, String userId, Set<String> userRoles) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            Criteria baseCriteria = Criteria.where("tenantId").is(tenantId)
////                    .and("ipAddress").is(clientIp)
////                    .and("active").is(true);
////
////            // Check global entries
////            Query globalQuery = new Query(baseCriteria.and("scope").is(IpWhitelistScope.GLOBAL));
////            if (mongoTemplate.exists(globalQuery, IpWhitelist.class)) {
////                return true;
////            }
////
////            // Check user-specific entries
////            Query userQuery = new Query(baseCriteria.and("userIds").in(userId));
////            if (mongoTemplate.exists(userQuery, IpWhitelist.class)) {
////                return true;
////            }
////
////            // Check role-specific entries
////            if (userRoles != null && !userRoles.isEmpty()) {
////                Query roleQuery = new Query(baseCriteria.and("roleIds").in(userRoles));
////                if (mongoTemplate.exists(roleQuery, IpWhitelist.class)) {
////                    return true;
////                }
////            }
////
////            return false;
////        } catch (Exception e) {
////            logger.error("Error checking IP whitelist status for user {}: {}", userId, e.getMessage());
////            return false;
////        }
////    }
////
////    // ============================================================================
////    // BULK OPERATIONS IMPLEMENTATION
////    // ============================================================================
////
////    @Override
////    @Transactional
////    public BulkDeleteIpResponse bulkDelete(List<String> ids, String tenantId, String userId,
////                                           boolean forceDelete, boolean skipValidation, String reason) {
////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
////        response.setTotalRequested(ids.size());
////        response.setPerformedBy(userId);
////        response.setTimestamp(LocalDateTime.now());
////
////        List<String> deletedIds = new ArrayList<>();
////        List<BulkDeleteIpResponse.FailedDeletion> failedDeletions = new ArrayList<>();
////        List<String> warnings = new ArrayList<>();
////
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // Fetch entries to be deleted
////            Query fetchQuery = new Query(Criteria.where("id").in(ids).and("tenantId").is(tenantId));
////            List<IpWhitelist> entriesToDelete = mongoTemplate.find(fetchQuery, IpWhitelist.class);
////
////            if (entriesToDelete.isEmpty()) {
////                response.setFailedToDelete(ids.size());
////                for (String id : ids) {
////                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(id, "unknown", "Entry not found", "NOT_FOUND"));
////                }
////                response.setFailedDeletions(failedDeletions);
////                logger.warn("Bulk delete attempted but no entries found for tenant {}", tenantId);
////                return response;
////            }
////
////            // Validate bulk delete if not skipped
////            if (!skipValidation) {
////                String userRole = getUserRole(userId);
////                ValidationResult validationResult = validator.validateBulkDelete(entriesToDelete, forceDelete, false, userRole);
////
////                // FIXED: Separate checks for warnings and errors
////                if (validationResult.hasWarnings()) {
////                    warnings.addAll(validationResult.getWarnings());
////                }
////
////                if (validationResult.hasErrors()) {
////                    response.setFailedToDelete(ids.size());
////                    for (ValidationResult.ValidationError error : validationResult.getErrors()) {
////                        for (IpWhitelist entry : entriesToDelete) {
////                            failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
////                                    entry.getId(), entry.getIpAddress(), error.getMessage(), error.getCode()));
////                        }
////                    }
////                    response.setFailedDeletions(failedDeletions);
////                    logger.warn("Bulk delete validation failed for tenant {}: {}", tenantId, validationResult.getErrors());
////                    return response;
////                }
////            }
////
////            // Perform deletions
////            for (IpWhitelist entry : entriesToDelete) {
////                try {
////                    // Check individual permissions
////                    if (!canDeleteEntry(entry, userId, forceDelete)) {
////                        failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
////                                entry.getId(), entry.getIpAddress(), "Insufficient permissions", "PERMISSION_DENIED"));
////                        logger.warn("Permission denied for deleting entry {} by user {}", entry.getId(), userId);
////                        continue;
////                    }
////
////                    // Perform actual deletion
////                    Query deleteQuery = new Query(Criteria.where("id").is(entry.getId()).and("tenantId").is(tenantId));
////                    mongoTemplate.remove(deleteQuery, IpWhitelist.class);
////
////                    deletedIds.add(entry.getId());
////                    logger.info("Bulk deleted IP whitelist entry {} ({}) by user {}",
////                            entry.getId(), entry.getIpAddress(), userId);
////
////                } catch (Exception e) {
////                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
////                            entry.getId(), entry.getIpAddress(), e.getMessage(), "DELETE_ERROR"));
////                    logger.error("Failed to delete entry {} during bulk operation: {}", entry.getId(), e.getMessage(), e);
////                }
////            }
////
////            // Build response
////            response.setSuccessfullyDeleted(deletedIds.size());
////            response.setFailedToDelete(failedDeletions.size());
////            response.setDeletedIds(deletedIds);
////            response.setFailedDeletions(failedDeletions);
////            response.setWarnings(warnings);
////
////            // Create deletion summary
////            Map<String, Object> summary = createDeletionSummary(entriesToDelete, deletedIds, reason);
////            response.setDeletionSummary(summary);
////
////            logger.info("Bulk delete completed: {} succeeded, {} failed for tenant {}",
////                    deletedIds.size(), failedDeletions.size(), tenantId);
////
////        } catch (Exception e) {
////            logger.error("Error during bulk delete operation for tenant {}: {}", tenantId, e.getMessage(), e);
////            response.setFailedToDelete(response.getTotalRequested());
////            response.setSuccessfullyDeleted(0);
////
////            // FIXED: Better error handling in transactional context
////            throw new RuntimeException("Bulk delete transaction failed", e);
////        }
////
////        return response;
////    }
////
////    @Override
////    @Transactional
////    public BulkDeleteIpResponse bulkSoftDelete(List<String> ids, String tenantId, String userId, String reason) {
////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
////        response.setTotalRequested(ids.size());
////        response.setPerformedBy(userId);
////        response.setTimestamp(LocalDateTime.now());
////
////        List<String> deactivatedIds = new ArrayList<>();
////        List<BulkDeleteIpResponse.FailedDeletion> failedDeletions = new ArrayList<>();
////
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            for (String id : ids) {
////                try {
////                    Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
////                    Update update = new Update()
////                            .set("active", false)
////                            .set("updatedBy", userId)
////                            .set("updatedAt", LocalDateTime.now())
////                            .set("notes", (reason != null ? "Deactivated: " + reason : "Bulk deactivated"));
////
////                    com.mongodb.client.result.UpdateResult result = mongoTemplate.updateFirst(query, update, IpWhitelist.class);
////
////                    if (result.getMatchedCount() > 0) {
////                        deactivatedIds.add(id);
////                        logger.info("Bulk deactivated IP whitelist entry {} by user {}", id, userId);
////                    } else {
////                        failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(id, "unknown", "Entry not found", "NOT_FOUND"));
////                        logger.warn("Entry {} not found for soft delete in tenant {}", id, tenantId);
////                    }
////
////                } catch (Exception e) {
////                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(id, "unknown", e.getMessage(), "UPDATE_ERROR"));
////                    logger.error("Failed to deactivate entry {} during bulk operation: {}", id, e.getMessage(), e);
////                }
////            }
////
////            response.setSuccessfullyDeleted(deactivatedIds.size());
////            response.setFailedToDelete(failedDeletions.size());
////            response.setDeletedIds(deactivatedIds);
////            response.setFailedDeletions(failedDeletions);
////
////            logger.info("Bulk soft delete completed: {} deactivated, {} failed for tenant {}",
////                    deactivatedIds.size(), failedDeletions.size(), tenantId);
////
////        } catch (Exception e) {
////            logger.error("Error during bulk soft delete operation for tenant {}: {}", tenantId, e.getMessage(), e);
////            response.setFailedToDelete(response.getTotalRequested());
////            response.setSuccessfullyDeleted(0);
////
////            // FIXED: Better error handling
////            throw new RuntimeException("Bulk soft delete transaction failed", e);
////        }
////
////        return response;
////    }
////
////    @Override
////    @Transactional
////    public BulkDeleteIpResponse deleteByScope(IpWhitelistScope scope, String tenantId, String userId,
////                                              boolean forceDelete, String reason) {
////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
////        response.setPerformedBy(userId);
////        response.setTimestamp(LocalDateTime.now());
////
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // Find entries to delete by scope
////            Query findQuery = new Query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope));
////            List<IpWhitelist> entriesToDelete = mongoTemplate.find(findQuery, IpWhitelist.class);
////
////            response.setTotalRequested(entriesToDelete.size());
////
////            if (entriesToDelete.isEmpty()) {
////                response.setSuccessfullyDeleted(0);
////                response.setFailedToDelete(0);
////                logger.info("No entries found for scope {} in tenant {}", scope, tenantId);
////                return response;
////            }
////
////            // Extract IDs and use bulk delete
////            List<String> ids = entriesToDelete.stream().map(IpWhitelist::getId).collect(Collectors.toList());
////            return bulkDelete(ids, tenantId, userId, forceDelete, false, reason);
////
////        } catch (Exception e) {
////            logger.error("Error during delete by scope {} for tenant {}: {}", scope, tenantId, e.getMessage(), e);
////            response.setFailedToDelete(response.getTotalRequested());
////            response.setSuccessfullyDeleted(0);
////            throw new RuntimeException("Delete by scope transaction failed", e);
////        }
////    }
////
////    @Override
////    @Transactional
////    public BulkDeleteIpResponse deleteInactiveEntries(String tenantId, int olderThanDays, String userId) {
////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
////        response.setPerformedBy(userId);
////        response.setTimestamp(LocalDateTime.now());
////
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // Find inactive entries older than specified days
////            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays);
////            Query findQuery = new Query(Criteria.where("tenantId").is(tenantId)
////                    .and("active").is(false)
////                    .and("updatedAt").lt(cutoffDate));
////
////            List<IpWhitelist> entriesToDelete = mongoTemplate.find(findQuery, IpWhitelist.class);
////            response.setTotalRequested(entriesToDelete.size());
////
////            if (entriesToDelete.isEmpty()) {
////                response.setSuccessfullyDeleted(0);
////                response.setFailedToDelete(0);
////                logger.info("No inactive entries older than {} days found for tenant {}", olderThanDays, tenantId);
////                return response;
////            }
////
////            // Delete the entries
////            Query deleteQuery = new Query(Criteria.where("tenantId").is(tenantId)
////                    .and("active").is(false)
////                    .and("updatedAt").lt(cutoffDate));
////
////            com.mongodb.client.result.DeleteResult result = mongoTemplate.remove(deleteQuery, IpWhitelist.class);
////
////            response.setSuccessfullyDeleted((int) result.getDeletedCount());
////            response.setFailedToDelete(0);
////
////            List<String> deletedIds = entriesToDelete.stream().map(IpWhitelist::getId).collect(Collectors.toList());
////            response.setDeletedIds(deletedIds);
////
////            logger.info("Cleaned up {} inactive IP whitelist entries older than {} days for tenant {}",
////                    result.getDeletedCount(), olderThanDays, tenantId);
////
////        } catch (Exception e) {
////            logger.error("Error during cleanup of inactive entries for tenant {}: {}", tenantId, e.getMessage(), e);
////            response.setFailedToDelete(response.getTotalRequested());
////            response.setSuccessfullyDeleted(0);
////            throw new RuntimeException("Cleanup transaction failed", e);
////        }
////
////        return response;
////    }
////
////    @Override
////    public BulkDeletePreview previewBulkDelete(List<String> ids, String tenantId, String userId) {
////        BulkDeletePreview preview = new BulkDeletePreview();
////
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // Fetch entries to preview
////            Query query = new Query(Criteria.where("id").in(ids).and("tenantId").is(tenantId));
////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
////
////            preview.setTotalEntries(entries.size());
////
////            // Create preview entries
////            List<BulkDeletePreview.PreviewEntry> previewEntries = entries.stream()
////                    .map(this::createPreviewEntry)
////                    .collect(Collectors.toList());
////
////            preview.setEntries(previewEntries);
////
////            // Identify critical entries
////            List<BulkDeletePreview.PreviewEntry> criticalEntries = previewEntries.stream()
////                    .filter(BulkDeletePreview.PreviewEntry::isCritical)
////                    .collect(Collectors.toList());
////
////            preview.setCriticalEntries(criticalEntries);
////            preview.setRequiresForceDelete(!criticalEntries.isEmpty());
////
////            // Count by status
////            int activeCount = (int) entries.stream().filter(IpWhitelist::isActive).count();
////            preview.setActiveEntries(activeCount);
////            preview.setInactiveEntries(entries.size() - activeCount);
////
////            // Create summaries
////            Map<IpWhitelistScope, Integer> scopeSummary = entries.stream()
////                    .collect(Collectors.groupingBy(IpWhitelist::getScope,
////                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
////            preview.setScopeSummary(scopeSummary);
////
////            Map<IpWhitelistType, Integer> typeSummary = entries.stream()
////                    .collect(Collectors.groupingBy(IpWhitelist::getType,
////                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
////            preview.setTypeSummary(typeSummary);
////
////            // Validate and add warnings
////            String userRole = getUserRole(userId);
////            ValidationResult validationResult = validator.validateBulkDelete(entries, false, false, userRole);
////
////            List<String> warnings = new ArrayList<>();
////            List<String> errors = new ArrayList<>();
////
////            // FIXED: Separate checks for warnings and errors
////            if (validationResult.hasWarnings()) {
////                warnings.addAll(validationResult.getWarnings());
////            }
////
////            if (validationResult.hasErrors()) {
////                errors.addAll(validationResult.getErrors().stream()
////                        .map(error -> error.getCode() + ": " + error.getMessage())
////                        .collect(Collectors.toList()));
////            }
////
////            preview.setWarnings(warnings);
////            preview.setErrors(errors);
////            preview.setCanProceed(!validationResult.hasErrors());
////
////            // Impact assessment
////            preview.setImpactAssessment(generateImpactAssessment(entries));
////
////        } catch (Exception e) {
////            logger.error("Error generating bulk delete preview for tenant {}: {}", tenantId, e.getMessage(), e);
////            preview.setCanProceed(false);
////            preview.setErrors(Collections.singletonList("Preview generation failed: " + e.getMessage()));
////        }
////
////        return preview;
////    }
////
////    // ============================================================================
////    // HELPER METHODS FOR BULK OPERATIONS
////    // ============================================================================
////
////    private boolean canDeleteEntry(IpWhitelist entry, String userId, boolean forceDelete) {
////        // Check if user has permission to delete this entry
////        if (entry.getScope() == IpWhitelistScope.ADMIN || entry.getScope() == IpWhitelistScope.GLOBAL) {
////            // Only superadmin can delete admin/global entries
////            String userRole = getUserRole(userId);
////            return "SUPERADMIN".equals(userRole);
////        }
////
////        if (entry.isCriticalIpRange() && !forceDelete) {
////            return false;
////        }
////
////        return true;
////    }
////
////    // FIXED: Improved getUserRole with proper service integration
////    private String getUserRole(String userId) {
////        try {
////            if (userService != null) {
////                return userService.getUserRole(userId);
////            }
////
////            logger.warn("UserService not available, using default role for user {}", userId);
////            return "USER"; // FIXED: Default to least privileged role
////
////        } catch (Exception e) {
////            logger.error("Error retrieving user role for {}: {}", userId, e.getMessage(), e);
////            return "USER"; // FIXED: Return least privileged role on error
////        }
////    }
////
////    private BulkDeletePreview.PreviewEntry createPreviewEntry(IpWhitelist entry) {
////        BulkDeletePreview.PreviewEntry previewEntry = new BulkDeletePreview.PreviewEntry(
////                entry.getId(),
////                entry.getIpAddress(),
////                entry.getDescription(),
////                entry.getScope(),
////                entry.getType(),
////                entry.isActive()
////        );
////
////        // Set affected users/roles
////        if (entry.getAllowedUserIds() != null) {
////            previewEntry.setAffectedUsers(new ArrayList<>(entry.getAllowedUserIds()));
////        }
////        if (entry.getAllowedRoleIds() != null) {
////            previewEntry.setAffectedRoles(new ArrayList<>(entry.getAllowedRoleIds()));
////        }
////
////        return previewEntry;
////    }
////
////    private Map<String, Object> createDeletionSummary(List<IpWhitelist> originalEntries, List<String> deletedIds, String reason) {
////        Map<String, Object> summary = new HashMap<>();
////
////        List<IpWhitelist> deletedEntries = originalEntries.stream()
////                .filter(entry -> deletedIds.contains(entry.getId()))
////                .collect(Collectors.toList());
////
////        // Summary by scope
////        Map<IpWhitelistScope, Long> scopeCounts = deletedEntries.stream()
////                .collect(Collectors.groupingBy(IpWhitelist::getScope, Collectors.counting()));
////        summary.put("deletedByScope", scopeCounts);
////
////        // Summary by type
////        Map<IpWhitelistType, Long> typeCounts = deletedEntries.stream()
////                .collect(Collectors.groupingBy(IpWhitelist::getType, Collectors.counting()));
////        summary.put("deletedByType", typeCounts);
////
////        // Critical entries count
////        long criticalCount = deletedEntries.stream()
////                .filter(IpWhitelist::isCriticalIpRange)
////                .count();
////        summary.put("criticalEntriesDeleted", criticalCount);
////
////        // Reason
////        summary.put("reason", reason != null ? reason : "Bulk deletion");
////        summary.put("timestamp", LocalDateTime.now());
////
////        return summary;
////    }
////
////    private String generateImpactAssessment(List<IpWhitelist> entries) {
////        int criticalCount = (int) entries.stream().filter(IpWhitelist::isCriticalIpRange).count();
////        int activeCount = (int) entries.stream().filter(IpWhitelist::isActive).count();
////        int userSpecificCount = (int) entries.stream().filter(e -> e.getScope() == IpWhitelistScope.USER_SPECIFIC).count();
////
////        StringBuilder assessment = new StringBuilder();
////
////        if (criticalCount > 0) {
////            assessment.append(String.format("%d critical IP ranges will be deleted. ", criticalCount));
////        }
////
////        if (activeCount > 0) {
////            assessment.append(String.format("%d active entries will be deleted, potentially affecting current access. ", activeCount));
////        }
////
////        if (userSpecificCount > 0) {
////            assessment.append(String.format("%d user-specific entries may affect individual user sessions. ", userSpecificCount));
////        }
////
////        if (assessment.length() == 0) {
////            assessment.append("Low impact - mostly inactive or non-critical entries.");
////        } else {
////            assessment.append("Review carefully before proceeding.");
////        }
////
////        return assessment.toString();
////    }
////
////    @Override
////    public IpWhitelist create(IpWhitelist ipWhitelist, String tenantId, String createdBy) {
////        try {
////            // ✅ Validate IP address before saving
////            if (!isValidIpAddress(ipWhitelist.getIpAddress())) {
////                throw new IllegalArgumentException("Invalid IP address format: " + ipWhitelist.getIpAddress());
////            }
////
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // ✅ Set required fields
////            ipWhitelist.setTenantId(tenantId);
////            ipWhitelist.setCreatedBy(createdBy);
////            ipWhitelist.setCreatedAt(LocalDateTime.now());  // Use createdAt not createdDate
////            ipWhitelist.setUpdatedAt(LocalDateTime.now());
////
////            // ✅ CRITICAL: Ensure active is true
////            ipWhitelist.setActive(true);
////
////            // ✅ Initialize counters
////            if (ipWhitelist.getAccessCount() == null) {
////                ipWhitelist.setAccessCount(0L);
////            }
////
////            // ✅ Set priority if not set
////            if (ipWhitelist.getPriority() == null || ipWhitelist.getPriority() == 0) {
////                ipWhitelist.setPriority(ipWhitelist.getScope().getPriorityOrder());
////            }
////
////            IpWhitelist saved = mongoTemplate.save(ipWhitelist);
////
////            logger.info("✅ Created IP whitelist entry: id={}, tenant={}, ip={}, scope={}, active={}, priority={}",
////                    saved.getId(), tenantId, saved.getIpAddress(), saved.getScope(),
////                    saved.isActive(), saved.getPriority());
////
////            return saved;
////
////        } catch (IllegalArgumentException e) {
////            logger.error("Validation error creating IP whitelist for tenant {}: {}", tenantId, e.getMessage());
////            throw e;
////        } catch (Exception e) {
////            logger.error("Error creating IP whitelist entry for tenant {}: {}", tenantId, e.getMessage(), e);
////            throw new RuntimeException("Failed to create IP whitelist entry: " + e.getMessage(), e);
////        }
////    }
////
////    @Override
////    public Optional<IpWhitelist> getById(String id, String tenantId) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////            Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
////            IpWhitelist result = mongoTemplate.findOne(query, IpWhitelist.class);
////            return Optional.ofNullable(result);
////        } catch (Exception e) {
////            logger.error("Error retrieving IP whitelist entry by ID {} for tenant {}: {}", id, tenantId, e.getMessage(), e);
////            return Optional.empty();
////        }
////    }
////
////    @Override
////    public Page<IpWhitelist> getAllByTenant(String tenantId, Pageable pageable) {
////        try {
////            logger.info("=== getAllByTenant DEBUG ===");
////            logger.info("Requested tenantId: {}", tenantId);
////            logger.info("TenantContext tenantId: {}", TenantContext.getCurrentTenant());
////            logger.info("Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
////
////            // ✅ CRITICAL: Set tenant context if not already set
////            if (TenantContext.getCurrentTenant() == null || !TenantContext.getCurrentTenant().equals(tenantId)) {
////                logger.warn("Tenant context mismatch. Setting to: {}", tenantId);
////                TenantContext.setCurrentTenant(tenantId);
////            }
////
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // Debug: Check collection info
////            String collectionName = mongoTemplate.getCollectionName(IpWhitelist.class);
////            logger.info("MongoDB collection: {}", collectionName);
////            logger.info("MongoDB database: {}", mongoTemplate.getDb().getName());
////
////            // Check total count WITHOUT tenant filter (to see if data exists at all)
////            long allEntriesCount = mongoTemplate.count(new Query(), IpWhitelist.class);
////            logger.info("Total entries in collection (all tenants): {}", allEntriesCount);
////
////            if (allEntriesCount == 0) {
////                logger.error("❌ IP whitelist collection is EMPTY! No entries exist at all.");
////                logger.error("You need to create at least one IP whitelist entry first!");
////                return Page.empty(pageable);
////            }
////
////            // Get distinct tenant IDs to see what's available
////            List<String> distinctTenants = mongoTemplate.findDistinct(
////                    new Query(),
////                    "tenantId",
////                    IpWhitelist.class,
////                    String.class
////            );
////            logger.info("Available tenant IDs in collection: {}", distinctTenants);
////
////            // Check count for THIS tenant
////            Criteria criteria = Criteria.where("tenantId").is(tenantId);
////            long tenantCount = mongoTemplate.count(Query.query(criteria), IpWhitelist.class);
////            logger.info("Entries for tenant '{}': {}", tenantId, tenantCount);
////
////            if (tenantCount == 0) {
////                logger.warn("❌ No entries found for tenant: {}", tenantId);
////                logger.warn("Available tenants: {}", distinctTenants);
////                logger.warn("Make sure you're using the correct tenant ID!");
////                return Page.empty(pageable);
////            }
////
////            // Build paginated query
////            Query query = new Query(criteria);
////            query.with(pageable);
////            query.with(Sort.by(Sort.Direction.DESC, "createdDate"));
////
////            logger.info("Executing query: {}", query);
////
////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
////            logger.info("✅ Found {} entries for current page", entries.size());
////
////            // Log sample entry
////            if (!entries.isEmpty()) {
////                IpWhitelist sample = entries.get(0);
////                logger.info("Sample entry: id={}, ip={}, tenant={}, active={}, scope={}, createdDate={}",
////                        sample.getId(), sample.getIpAddress(), sample.getTenantId(),
////                        sample.isActive(), sample.getScope(), sample.getCreatedAt());
////            }
////
////            PageImpl<IpWhitelist> result = new PageImpl<>(entries, pageable, tenantCount);
////            logger.info("Returning page: {} entries, {} total, {} pages",
////                    result.getNumberOfElements(), result.getTotalElements(), result.getTotalPages());
////
////            return result;
////
////        } catch (Exception e) {
////            logger.error("❌ Error in getAllByTenant for tenant {}: {}", tenantId, e.getMessage());
////            logger.error("Exception details:", e);
////            return Page.empty(pageable);
////        }
////    }
////    @Override
////    public IpWhitelist update(String id, IpWhitelist ipWhitelist, String tenantId, String updatedBy) {
////        try {
////            // FIXED: Validate IP address if changed
////            if (ipWhitelist.getIpAddress() != null && !isValidIpAddress(ipWhitelist.getIpAddress())) {
////                throw new IllegalArgumentException("Invalid IP address format: " + ipWhitelist.getIpAddress());
////            }
////
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // Build query to match by ID and tenant
////            Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
////
////            // FIXED: Corrected field name from "isActive" to "active"
////            Update update = new Update()
////                    .set("description", ipWhitelist.getDescription())
////                    .set("isActive", ipWhitelist.isActive())
////                    .set("updatedBy", updatedBy)
////                    .set("updatedAt", LocalDateTime.now())
////                    .set("notes", ipWhitelist.getNotes())
////                    .set("ipAddress", ipWhitelist.getIpAddress());
////
////            // Apply update
////            mongoTemplate.updateFirst(query, update, IpWhitelist.class);
////
////            logger.info("Updated IP whitelist entry {} in tenant {} by user {}", id, tenantId, updatedBy);
////
////            // Retrieve updated entity
////            return getById(id, tenantId)
////                    .orElseThrow(() -> new RuntimeException("Entry not found after update"));
////        } catch (Exception e) {
////            logger.error("Error updating IP whitelist entry {} for tenant {}: {}", id, tenantId, e.getMessage(), e);
////            throw new RuntimeException("Failed to update IP whitelist entry", e);
////        }
////    }
////
////    @Override
////    public void delete(String id, String tenantId) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////            Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
////            mongoTemplate.remove(query, IpWhitelist.class);
////
////            logger.info("Deleted IP whitelist entry {} from tenant {}", id, tenantId);
////        } catch (Exception e) {
////            logger.error("Error deleting IP whitelist entry {} from tenant {}: {}", id, tenantId, e.getMessage(), e);
////            throw new RuntimeException("Failed to delete IP whitelist entry", e);
////        }
////    }
////
////    @Data
////    @NoArgsConstructor
////    @AllArgsConstructor
////    private static class ActiveStats {
////        private Boolean id; // mapped from "_id" of aggregation
////        private long count;
////    }
////    public Map<String, Object> getStatistics(String tenantId) {
////        // Validate input
////        if (tenantId == null || tenantId.trim().isEmpty()) {
////            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
////        }
////
////        try {
////            logger.debug("Fetching IP whitelist statistics for tenant: {}", tenantId);
////
////            // Use MongoDB aggregation pipeline
////            Aggregation agg = Aggregation.newAggregation(
////                    Aggregation.match(Criteria.where("tenantId").is(tenantId)),
////                    Aggregation.group("isActive").count().as("count"),
////                    Aggregation.sort(Sort.Direction.DESC, "_id") // Sort by active status
////            );
////
////            AggregationResults<ActiveStats> result = mongoTemplateProvider.getMongoTemplate()
////                    .aggregate(agg, "ip_whitelist", ActiveStats.class); // Use explicit collection name
////
////            // Initialize counters
////            long activeCount = 0;
////            long inactiveCount = 0;
////            long nullCount = 0;
////
////            // Process results
////            for (ActiveStats stat : result.getMappedResults()) {
////                Boolean isActive = stat.getId();
////                long count = stat.getCount();
////
////                if (Boolean.TRUE.equals(isActive)) {
////                    activeCount = count;
////                } else if (Boolean.FALSE.equals(isActive)) {
////                    inactiveCount = count;
////                } else {
////                    nullCount = count;
////                    logger.warn("Found {} IP whitelist entries with null 'active' status in tenant: {}",
////                            count, tenantId);
////                }
////            }
////
////            long total = activeCount + inactiveCount + nullCount;
////
////            // Build response
////            Map<String, Object> stats = new LinkedHashMap<>(); // Preserve insertion order
////            stats.put("tenantId", tenantId);
////            stats.put("totalEntries", total);
////            stats.put("activeEntries", activeCount);
////            stats.put("inactiveEntries", inactiveCount);
////
////            if (nullCount > 0) {
////                stats.put("undefinedStatusEntries", nullCount);
////            }
////
////            logger.info("Retrieved statistics for tenant {}: total={}, active={}, inactive={}",
////                    tenantId, total, activeCount, inactiveCount);
////
////            return stats;
////
////        } catch (IllegalArgumentException e) {
////            // Re-throw validation errors
////            throw e;
////        } catch (Exception e) {
////            logger.error("Failed to retrieve IP whitelist statistics for tenant {}", tenantId, e);
////            throw new RuntimeException("Database error while fetching statistics for tenant: " + tenantId, e);
////        }
////    }
////
////    //----------
////
////    @Override
////    public ValidationResult validateUserIpAccess(String clientIp, String tenantId, String userId, Set<String> userRoles) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            logger.info("=== VALIDATION DEBUG ===");
////            logger.info("clientIp: {}, tenantId: {}, userId: {}, userRoles: {}",
////                    clientIp, tenantId, userId, userRoles);
////
////            // ✅ SIMPLIFIED QUERY: Get ALL active entries for the tenant
////            Query query = new Query(
////                    Criteria.where("tenantId").is(tenantId)
////                            .and("isActive").is(true)
////            );
////            query.with(Sort.by(Sort.Direction.DESC, "priority", "scope"));
////
////            List<IpWhitelist> allActiveEntries = mongoTemplate.find(query, IpWhitelist.class);
////
////            logger.info("Found {} active entries for tenant", allActiveEntries.size());
////
////            if (allActiveEntries.isEmpty()) {
////                logger.warn("No active IP whitelist entries found for tenant: {}", tenantId);
////                return ValidationResult.denied(
////                        "No active IP whitelist entries configured for tenant",
////                        "NO_TENANT_ENTRIES"
////                );
////            }
////
////            // ✅ FILTER IN JAVA: Check which entries match the client IP
////            List<IpWhitelist> matchingEntries = allActiveEntries.stream()
////                    .filter(entry -> ipMatchesEntry(clientIp, entry.getIpAddress()))
////                    .collect(Collectors.toList());
////
////            logger.info("After IP filtering: {} matching entries", matchingEntries.size());
////
////            if (matchingEntries.isEmpty()) {
////                // Log all available IPs for debugging
////                logger.warn("Client IP {} did not match any of these configured IPs:", clientIp);
////                allActiveEntries.forEach(entry ->
////                        logger.warn("  - {}", entry.getIpAddress())
////                );
////                return ValidationResult.denied(
////                        "No IP whitelist entries found for IP: " + clientIp,
////                        "NO_WHITELIST_ENTRY"
////                );
////            }
////
////            // ✅ CHECK USER/ROLE PERMISSIONS
////            for (IpWhitelist entry : matchingEntries) {
////                logger.info("Checking entry: id={}, ipAddress={}, scope={}, allowedUserIds={}, allowedRoleIds={}",
////                        entry.getId(), entry.getIpAddress(), entry.getScope(),
////                        entry.getAllowedUserIds(), entry.getAllowedRoleIds());
////
////                boolean applies = entry.appliesToUser(userId, userRoles);
////                logger.info("appliesToUser result: {}", applies);
////
////                if (applies) {
////                    // Update access tracking
////                    updateLastAccess(entry.getId(), tenantId, clientIp, userId);
////
////                    logger.info("✅ ACCESS GRANTED via {} scope", entry.getScope());
////                    return ValidationResult.allowed(
////                            "Access granted via " + entry.getScope() + " scope",
////                            entry,
////                            entry.getScope()
////                    );
////                }
////            }
////
////            logger.warn("IP matched but no entries passed user/role validation");
////            return ValidationResult.denied(
////                    "IP whitelisted but does not match user/role criteria",
////                    "USER_ROLE_MISMATCH"
////            );
////
////        } catch (Exception e) {
////            logger.error("Error validating IP access: {}", e.getMessage(), e);
////            return ValidationResult.denied(
////                    "Validation error: " + e.getMessage(),
////                    "VALIDATION_ERROR"
////            );
////        }
////    }
////
////    // ✅ HELPER METHOD: IP matching logic with proper CIDR support
////
////    // ✅ CIDR validation using Apache Commons Net
////    private boolean isIpInCidrRange(String ip, String cidr) {
////        try {
////            SubnetUtils subnet = new SubnetUtils(cidr);
////            subnet.setInclusiveHostCount(true);
////            return subnet.getInfo().isInRange(ip);
////        } catch (Exception e) {
////            logger.warn("Error checking if IP {} is in CIDR range {}: {}", ip, cidr, e.getMessage());
////            return false;
////        }
////    }
////
////
////    //----------------
////    @Override
////    public void updateLastAccess(String entryId, String tenantId, String clientIp, String userId) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////            Query query = new Query(Criteria.where("id").is(entryId).and("tenantId").is(tenantId));
////
////            Update update = new Update()
////                    .set("lastAccessedAt", LocalDateTime.now())
////                    .set("lastAccessedBy", userId)
////                    .set("lastAccessedIp", clientIp)
////                    .inc("accessCount", 1);
////
////            mongoTemplate.updateFirst(query, update, IpWhitelist.class);
////
////        } catch (Exception e) {
////            logger.warn("Failed to update last access for entry {}: {}", entryId, e.getMessage());
////            // Don't throw exception as this is tracking data
////        }
////    }
////
////    // ============================================================================
////    // ADVANCED CREATION METHODS
////    // ============================================================================
////
////    @Override
////    public IpWhitelist createUserSpecific(String tenantId, String ipAddress, String description,
////                                          Set<String> userIds, String createdBy) {
////        IpWhitelist ipWhitelist = new IpWhitelist();
////        ipWhitelist.setTenantId(tenantId);
////        ipWhitelist.setIpAddress(ipAddress);
////        ipWhitelist.setDescription(description);
////        ipWhitelist.setScope(IpWhitelistScope.USER_SPECIFIC);
////        ipWhitelist.setType(IpWhitelistType.USER_SPECIFIC);
////        ipWhitelist.setAllowedUserIds(userIds != null ? new HashSet<>(userIds) : new HashSet<>());
////        ipWhitelist.setCreatedBy(createdBy);
////        ipWhitelist.setPriority(IpWhitelistScope.USER_SPECIFIC.getPrecedence() / 100);
////
////        return create(ipWhitelist, tenantId, createdBy);
////    }
////
////    @Override
////    public IpWhitelist createRoleSpecific(String tenantId, String ipAddress, String description,
////                                          Set<String> roleIds, String createdBy) {
////        IpWhitelist ipWhitelist = new IpWhitelist();
////        ipWhitelist.setTenantId(tenantId);
////        ipWhitelist.setIpAddress(ipAddress);
////        ipWhitelist.setDescription(description);
////        ipWhitelist.setScope(IpWhitelistScope.ROLE_SPECIFIC);
////        ipWhitelist.setType(IpWhitelistType.ROLE_SPECIFIC);
////        ipWhitelist.setAllowedRoleIds(roleIds != null ? new HashSet<>(roleIds) : new HashSet<>());
////        ipWhitelist.setCreatedBy(createdBy);
////        ipWhitelist.setPriority(IpWhitelistScope.ROLE_SPECIFIC.getPrecedence() / 100);
////
////        return create(ipWhitelist, tenantId, createdBy);
////    }
////
////    @Override
////    public IpWhitelist createHybrid(String tenantId, String ipAddress, String description,
////                                    Set<String> userIds, Set<String> roleIds, String createdBy) {
////        IpWhitelist ipWhitelist = new IpWhitelist();
////        ipWhitelist.setTenantId(tenantId);
////        ipWhitelist.setIpAddress(ipAddress);
////        ipWhitelist.setDescription(description);
////        ipWhitelist.setScope(IpWhitelistScope.HYBRID);
////        ipWhitelist.setType(IpWhitelistType.HYBRID);
////        ipWhitelist.setAllowedUserIds(userIds != null ? new HashSet<>(userIds) : new HashSet<>());
////        ipWhitelist.setAllowedRoleIds(roleIds != null ? new HashSet<>(roleIds) : new HashSet<>());
////        ipWhitelist.setCreatedBy(createdBy);
////        ipWhitelist.setPriority(IpWhitelistScope.HYBRID.getPrecedence() / 100);
////
////        return create(ipWhitelist, tenantId, createdBy);
////    }
////
////    // ============================================================================
////    // ADVANCED QUERYING METHODS
////    // ============================================================================
////
////    @Override
////    public Page<IpWhitelist> getEntriesByScope(String tenantId, IpWhitelistScope scope, Pageable pageable) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////            Query query = new Query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope));
////            query.with(pageable);
////
////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
////            long total = mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope)), IpWhitelist.class);
////
////            return new PageImpl<>(entries, pageable, total);
////        } catch (Exception e) {
////            logger.error("Error fetching entries by scope {} for tenant {}: {}", scope, tenantId, e.getMessage(), e);
////            return new PageImpl<>(Collections.emptyList(), pageable, 0);
////        }
////    }
////
////    @Override
////    public Page<IpWhitelist> getEntriesForUserId(String tenantId, String userId, Pageable pageable) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            Criteria criteria = Criteria.where("tenantId").is(tenantId)
////                    .andOperator(
////                            Criteria.where("allowedUserIds").in(userId)
////                    );
////
////            Query query = new Query(criteria);
////            query.with(pageable);
////
////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
////            long total = mongoTemplate.count(Query.query(criteria), IpWhitelist.class);
////
////            return new PageImpl<>(entries, pageable, total);
////        } catch (Exception e) {
////            logger.error("Error fetching entries for user {} in tenant {}: {}", userId, tenantId, e.getMessage(), e);
////            return new PageImpl<>(Collections.emptyList(), pageable, 0);
////        }
////    }
////
////    @Override
////    public Page<IpWhitelist> getEntriesForRoleId(String tenantId, String roleId, Pageable pageable) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            Criteria criteria = Criteria.where("tenantId").is(tenantId)
////                    .andOperator(
////                            Criteria.where("allowedRoleIds").in(roleId)
////                    );
////
////            Query query = new Query(criteria);
////            query.with(pageable);
////
////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
////            long total = mongoTemplate.count(Query.query(criteria), IpWhitelist.class);
////
////            return new PageImpl<>(entries, pageable, total);
////        } catch (Exception e) {
////            logger.error("Error fetching entries for role {} in tenant {}: {}", roleId, tenantId, e.getMessage(), e);
////            return new PageImpl<>(Collections.emptyList(), pageable, 0);
////        }
////    }
////
////    @Override
////    public List<IpWhitelist> getEntriesForUser(String tenantId, String userId, Set<String> userRoles) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////
////            // Build criteria for entries that apply to this user
////            List<Criteria> criteriaList = new ArrayList<>();
////
////            // Global and Tenant scope entries
////            criteriaList.add(Criteria.where("scope").in(IpWhitelistScope.GLOBAL, IpWhitelistScope.TENANT, IpWhitelistScope.ADMIN));
////
////            // User-specific entries
////            criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.USER_SPECIFIC)
////                    .and("allowedUserIds").in(userId));
////
////            // Role-specific entries
////            if (userRoles != null && !userRoles.isEmpty()) {
////                criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.ROLE_SPECIFIC)
////                        .and("allowedRoleIds").in(userRoles));
////            }
////
////            // Hybrid entries (user OR role match)
////            List<Criteria> hybridCriteria = new ArrayList<>();
////            hybridCriteria.add(Criteria.where("allowedUserIds").in(userId));
////            if (userRoles != null && !userRoles.isEmpty()) {
////                hybridCriteria.add(Criteria.where("allowedRoleIds").in(userRoles));
////            }
////            criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.HYBRID)
////                    .orOperator(hybridCriteria.toArray(new Criteria[0])));
////
////            Criteria mainCriteria = Criteria.where("tenantId").is(tenantId)
////                    .and("active").is(true)
////                    .orOperator(criteriaList.toArray(new Criteria[0]));
////
////            Query query = new Query(mainCriteria);
////            query.with(Sort.by(Sort.Direction.DESC, "priority", "scope"));
////
////            return mongoTemplate.find(query, IpWhitelist.class);
////
////        } catch (Exception e) {
////            logger.error("Error fetching entries for user {} with roles {} in tenant {}: {}",
////                    userId, userRoles, tenantId, e.getMessage(), e);
////            return Collections.emptyList();
////        }
////    }
////
////    @Override
////    public Map<String, Object> getAdvancedStatistics(String tenantId) {
////        try {
////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
////            Map<String, Object> stats = new HashMap<>();
////
////            // Basic counts
////            long total = mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId)), IpWhitelist.class);
////            long active = mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId).and("active").is(true)), IpWhitelist.class);
////
////            stats.put("totalEntries", total);
////            stats.put("activeEntries", active);
////            stats.put("inactiveEntries", total - active);
////
////            // Statistics by scope
////            Map<String, Long> scopeStats = new HashMap<>();
////            for (IpWhitelistScope scope : IpWhitelistScope.values()) {
////                long count = mongoTemplate.count(
////                        Query.query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope)),
////                        IpWhitelist.class);
////                scopeStats.put(scope.name(), count);
////            }
////            stats.put("byScope", scopeStats);
////
////            // Statistics by type
////            Map<String, Long> typeStats = new HashMap<>();
////            for (IpWhitelistType type : IpWhitelistType.values()) {
////                long count = mongoTemplate.count(
////                        Query.query(Criteria.where("tenantId").is(tenantId).and("type").is(type)),
////                        IpWhitelist.class);
////                typeStats.put(type.name(), count);
////            }
////            stats.put("byType", typeStats);
////
////            // Access statistics
////            Aggregation aggregation = Aggregation.newAggregation(
////                    Aggregation.match(Criteria.where("tenantId").is(tenantId)),
////                    Aggregation.group()
////                            .sum("accessCount").as("totalAccess")
////                            .avg("accessCount").as("avgAccess")
////                            .count().as("entriesWithAccess")
////            );
////
////            AggregationResults<Map> accessResults = mongoTemplate.aggregate(aggregation, "ip_whitelist", Map.class);
////            Map accessStats = accessResults.getUniqueMappedResult();
////            if (accessStats != null) {
////                stats.put("accessStats", accessStats);
////            }
////
////            // Recent activity
////            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
////            long recentlyAccessed = mongoTemplate.count(
////                    Query.query(Criteria.where("tenantId").is(tenantId).and("lastAccessedAt").gte(weekAgo)),
////                    IpWhitelist.class);
////            stats.put("recentlyAccessedCount", recentlyAccessed);
////
////            return stats;
////
////        } catch (Exception e) {
////            logger.error("Error retrieving advanced statistics for tenant {}: {}", tenantId, e.getMessage(), e);
////            Map<String, Object> errorStats = new HashMap<>();
////            errorStats.put("error", "Unable to retrieve statistics: " + e.getMessage());
////            return errorStats;
////        }
////    }
////
////    // ============================================================================
////    // HELPER METHODS - CIDR AND IP VALIDATION
////    // ============================================================================
////
////    // FIXED: Proper CIDR range matching using Apache Commons Net
////    /**
////     * Build MongoDB criteria for matching IP addresses
////     * Handles exact matches, wildcards, and CIDR ranges
////     *
////     * IMPROVEMENTS:
////     * 1. Better CIDR range matching using regex patterns
////     * 2. Support for common private network ranges
////     * 3. IPv4 and IPv6 wildcard support
////     *
////     * @param clientIp the client IP address to match
////     * @param tenantId the tenant ID
////     * @return MongoDB Criteria for IP matching
////     */
////    private Criteria buildIpMatchingCriteria(String clientIp, String tenantId) {
////        List<Criteria> ipCriteriaList = new ArrayList<>();
////
////        // 1. Exact IP match (highest priority)
////        ipCriteriaList.add(Criteria.where("ipAddress").is(clientIp));
////
////        // 2. Wildcard match (matches everything)
////        ipCriteriaList.add(Criteria.where("ipAddress").is("*"));
////
////        // 3. Global IPv4 wildcard (0.0.0.0/0)
////        ipCriteriaList.add(Criteria.where("ipAddress").is("0.0.0.0/0"));
////
////        // 4. Global IPv6 wildcard (::/0)
////        ipCriteriaList.add(Criteria.where("ipAddress").is("::/0"));
////
////        // 5. CIDR range matching - check common subnet patterns
////        // For a client IP like 192.168.1.100, check these patterns:
////        // - 192.168.1.0/24 (subnet)
////        // - 192.168.0.0/16 (larger subnet)
////        // - 192.0.0.0/8 (even larger)
////
////        try {
////            String[] ipParts = clientIp.split("\\.");
////            if (ipParts.length == 4) {
////                // /24 subnet (e.g., 192.168.1.0/24)
////                String subnet24 = String.format("%s.%s.%s.0/24", ipParts[0], ipParts[1], ipParts[2]);
////                ipCriteriaList.add(Criteria.where("ipAddress").is(subnet24));
////
////                // /16 subnet (e.g., 192.168.0.0/16)
////                String subnet16 = String.format("%s.%s.0.0/16", ipParts[0], ipParts[1]);
////                ipCriteriaList.add(Criteria.where("ipAddress").is(subnet16));
////
////                // /8 subnet (e.g., 192.0.0.0/8, 10.0.0.0/8)
////                String subnet8 = String.format("%s.0.0.0/8", ipParts[0]);
////                ipCriteriaList.add(Criteria.where("ipAddress").is(subnet8));
////
////                // Also check for any CIDR range that contains this IP
////                // This uses a regex pattern to find CIDR entries
////                String cidrPattern = "^" + Pattern.quote(ipParts[0] + "." + ipParts[1] + "." + ipParts[2]) + "\\.\\d+/\\d+$";
////                ipCriteriaList.add(Criteria.where("ipAddress").regex(cidrPattern));
////            }
////        } catch (Exception e) {
////            logger.warn("Error building CIDR criteria for IP {}: {}", clientIp, e.getMessage());
////        }
////
////        // 6. Check common private network ranges if applicable
////        if (clientIp.startsWith("10.")) {
////            ipCriteriaList.add(Criteria.where("ipAddress").is("10.0.0.0/8"));
////        } else if (clientIp.startsWith("172.")) {
////            // Check if it's in 172.16.0.0/12 range
////            String[] parts = clientIp.split("\\.");
////            if (parts.length >= 2) {
////                int secondOctet = Integer.parseInt(parts[1]);
////                if (secondOctet >= 16 && secondOctet <= 31) {
////                    ipCriteriaList.add(Criteria.where("ipAddress").is("172.16.0.0/12"));
////                }
////            }
////        } else if (clientIp.startsWith("192.168.")) {
////            ipCriteriaList.add(Criteria.where("ipAddress").is("192.168.0.0/16"));
////        }
////
////        // Combine all criteria with OR operator and include tenant + active filters
////        return Criteria.where("tenantId").is(tenantId)
////                .and("active").is(true)
////                .orOperator(ipCriteriaList.toArray(new Criteria[0]));
////    }
////    // FIXED: Proper IP matching logic with CIDR support
////    private boolean ipMatchesEntry(String clientIp, String entryIp) {
////        try {
////            // Wildcard match
////            if ("*".equals(entryIp)) {
////                return true;
////            }
////
////            // Exact match
////            if (clientIp.equals(entryIp)) {
////                return true;
////            }
////
////            // Global access
////            if ("0.0.0.0/0".equals(entryIp) || "::/0".equals(entryIp)) {
////                return true;
////            }
////
////            // CIDR range match
////            if (entryIp.contains("/")) {
////                return isIpInCidrRange(clientIp, entryIp);
////            }
////
////            return false;
////
////        } catch (Exception e) {
////            logger.warn("Error matching IP {} against entry {}: {}", clientIp, entryIp, e.getMessage());
////            return false;
////        }
////    }
////
////
////    // FIXED: Proper IP address validation
////    private boolean isValidIpAddress(String ipAddress) {
////        if (ipAddress == null || ipAddress.trim().isEmpty()) {
////            return false;
////        }
////
////        // Allow wildcard
////        if ("*".equals(ipAddress)) {
////            return true;
////        }
////
////        // Allow global access
////        if ("0.0.0.0/0".equals(ipAddress) || "::/0".equals(ipAddress)) {
////            return true;
////        }
////
////        // Check CIDR notation
////        if (ipAddress.contains("/")) {
////            return CIDR_PATTERN.matcher(ipAddress).matches();
////        }
////
////        // Check simple IPv4
////        return IPV4_PATTERN.matcher(ipAddress).matches();
////    }
////}
//////package com.midas.consulting.service;
//////
//////import com.midas.consulting.config.database.MongoTemplateProvider;
//////import com.midas.consulting.controller.v1.response.BulkDeleteIpResponse;
//////import com.midas.consulting.controller.v1.response.BulkDeletePreview;
//////import com.midas.consulting.model.IpWhitelist;
//////import com.midas.consulting.model.IpWhitelistScope;
//////import com.midas.consulting.model.IpWhitelistType;
//////import lombok.AllArgsConstructor;
//////import lombok.Data;
//////import lombok.NoArgsConstructor;
//////import org.slf4j.Logger;
//////import org.slf4j.LoggerFactory;
//////import org.springframework.beans.factory.annotation.Autowired;
//////import org.springframework.data.domain.Page;
//////import org.springframework.data.domain.PageImpl;
//////import org.springframework.data.domain.Pageable;
//////import org.springframework.data.domain.Sort;
//////import org.springframework.data.mongodb.core.MongoTemplate;
//////import org.springframework.data.mongodb.core.aggregation.Aggregation;
//////import org.springframework.data.mongodb.core.aggregation.AggregationResults;
//////import org.springframework.data.mongodb.core.query.Criteria;
//////import org.springframework.data.mongodb.core.query.Query;
//////import org.springframework.data.mongodb.core.query.Update;
//////import org.springframework.stereotype.Service;
//////import org.springframework.transaction.annotation.Transactional;
//////
//////import java.time.LocalDateTime;
//////import java.util.*;
//////import java.util.stream.Collectors;
//////
///////**
////// * Implementation of IpWhitelistService that provides both basic (WhitelistService)
////// * and advanced IP whitelist functionality
////// */
//////@Service
//////public class IpWhitelistServiceImpl implements IpWhitelistService {
//////
//////    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistServiceImpl.class);
//////
//////    @Autowired
//////    private MongoTemplateProvider mongoTemplateProvider;
//////
//////    @Autowired
//////    private IpBulkDeleteValidator validator;
//////
//////    // Critical IP ranges that require special handling
//////    private static final Set<String> CRITICAL_IP_RANGES =
//////            Collections.unmodifiableSet(new HashSet<String>() {{
//////                add("127.0.0.1");
//////                add("::1");
//////                add("0.0.0.0/0");
//////                add("::/0");
//////                add("10.0.0.0/8");
//////                add("172.16.0.0/12");
//////                add("192.168.0.0/16");
//////            }});
//////
//////    // ============================================================================
//////    // WHITELIST SERVICE METHODS (Base Interface Implementation)
//////    // ============================================================================
//////
//////    @Override
//////    public boolean isIpWhitelisted(String clientIp, String tenantId) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////            Query query = new Query(Criteria.where("tenantId").is(tenantId)
//////                    .and("ipAddress").is(clientIp)
//////                    .and("active").is(true));
//////            return mongoTemplate.exists(query, IpWhitelist.class);
//////        } catch (Exception e) {
//////            logger.error("Error checking IP whitelist status for {}: {}", clientIp, e.getMessage());
//////            return false;
//////        }
//////    }
//////
//////    @Override
//////    public boolean isIpWhitelisted(String clientIp, String tenantId, String userId, Set<String> userRoles) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            Criteria baseCriteria = Criteria.where("tenantId").is(tenantId)
//////                    .and("ipAddress").is(clientIp)
//////                    .and("active").is(true);
//////
//////            // Check global entries
//////            Query globalQuery = new Query(baseCriteria.and("scope").is(IpWhitelistScope.GLOBAL));
//////            if (mongoTemplate.exists(globalQuery, IpWhitelist.class)) {
//////                return true;
//////            }
//////
//////            // Check user-specific entries
//////            Query userQuery = new Query(baseCriteria.and("userIds").in(userId));
//////            if (mongoTemplate.exists(userQuery, IpWhitelist.class)) {
//////                return true;
//////            }
//////
//////            // Check role-specific entries
//////            if (userRoles != null && !userRoles.isEmpty()) {
//////                Query roleQuery = new Query(baseCriteria.and("roleIds").in(userRoles));
//////                if (mongoTemplate.exists(roleQuery, IpWhitelist.class)) {
//////                    return true;
//////                }
//////            }
//////
//////            return false;
//////        } catch (Exception e) {
//////            logger.error("Error checking IP whitelist status for user {}: {}", userId, e.getMessage());
//////            return false;
//////        }
//////    }
//////
//////
//////    // ============================================================================
//////    // BULK OPERATIONS IMPLEMENTATION
//////    // ============================================================================
//////
//////    @Override
//////    @Transactional
//////    public BulkDeleteIpResponse bulkDelete(List<String> ids, String tenantId, String userId,
//////                                           boolean forceDelete, boolean skipValidation, String reason) {
//////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
//////        response.setTotalRequested(ids.size());
//////        response.setPerformedBy(userId);
//////        response.setTimestamp(LocalDateTime.now());
//////
//////        List<String> deletedIds = new ArrayList<>();
//////        List<BulkDeleteIpResponse.FailedDeletion> failedDeletions = new ArrayList<>();
//////        List<String> warnings = new ArrayList<>();
//////
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            // Fetch entries to be deleted
//////            Query fetchQuery = new Query(Criteria.where("id").in(ids).and("tenantId").is(tenantId));
//////            List<IpWhitelist> entriesToDelete = mongoTemplate.find(fetchQuery, IpWhitelist.class);
//////
//////            if (entriesToDelete.isEmpty()) {
//////                response.setFailedToDelete(ids.size());
//////                for (String id : ids) {
//////                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(id, "unknown", "Entry not found", "NOT_FOUND"));
//////                }
//////                response.setFailedDeletions(failedDeletions);
//////                return response;
//////            }
//////
//////            // Validate bulk delete if not skipped
//////            if (!skipValidation) {
//////                String userRole = getUserRole(userId); // You'll need to implement this
//////           ValidationResult validationResult = validator.validateBulkDelete(entriesToDelete, forceDelete, false, userRole);
//////
//////                if (validationResult.hasErrors()) {
//////                    response.setFailedToDelete(ids.size());
//////                    for (ValidationResult.ValidationError error : validationResult.getErrors()) {
//////                        for (IpWhitelist entry : entriesToDelete) {
//////                            failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
//////                                    entry.getId(), entry.getIpAddress(), error.getMessage(), error.getCode()));
//////                        }
//////                    }
//////                    response.setFailedDeletions(failedDeletions);
//////                    return response;
//////                }
//////
//////                if (validationResult.hasErrors()) {
//////                    warnings.addAll(validationResult.getWarnings());
//////                }
//////            }
//////
//////            // Perform deletions
//////            for (IpWhitelist entry : entriesToDelete) {
//////                try {
//////                    // Check individual permissions
//////                    if (!canDeleteEntry(entry, userId, forceDelete)) {
//////                        failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
//////                                entry.getId(), entry.getIpAddress(), "Insufficient permissions", "PERMISSION_DENIED"));
//////                        continue;
//////                    }
//////
//////                    // Perform actual deletion
//////                    Query deleteQuery = new Query(Criteria.where("id").is(entry.getId()).and("tenantId").is(tenantId));
//////                    mongoTemplate.remove(deleteQuery, IpWhitelist.class);
//////
//////                    deletedIds.add(entry.getId());
//////                    logger.info("Bulk deleted IP whitelist entry {} ({}) by user {}",
//////                            entry.getId(), entry.getIpAddress(), userId);
//////
//////                } catch (Exception e) {
//////                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(
//////                            entry.getId(), entry.getIpAddress(), e.getMessage(), "DELETE_ERROR"));
//////                    logger.error("Failed to delete entry {} during bulk operation: {}", entry.getId(), e.getMessage());
//////                }
//////            }
//////
//////            // Build response
//////            response.setSuccessfullyDeleted(deletedIds.size());
//////            response.setFailedToDelete(failedDeletions.size());
//////            response.setDeletedIds(deletedIds);
//////            response.setFailedDeletions(failedDeletions);
//////            response.setWarnings(warnings);
//////
//////            // Create deletion summary
//////            Map<String, Object> summary = createDeletionSummary(entriesToDelete, deletedIds, reason);
//////            response.setDeletionSummary(summary);
//////
//////        } catch (Exception e) {
//////            logger.error("Error during bulk delete operation: {}", e.getMessage(), e);
//////            response.setFailedToDelete(response.getTotalRequested());
//////            response.setSuccessfullyDeleted(0);
//////        }
//////
//////        return response;
//////    }
//////
//////    @Override
//////    @Transactional
//////    public BulkDeleteIpResponse bulkSoftDelete(List<String> ids, String tenantId, String userId, String reason) {
//////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
//////        response.setTotalRequested(ids.size());
//////        response.setPerformedBy(userId);
//////        response.setTimestamp(LocalDateTime.now());
//////
//////        List<String> deactivatedIds = new ArrayList<>();
//////        List<BulkDeleteIpResponse.FailedDeletion> failedDeletions = new ArrayList<>();
//////
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            for (String id : ids) {
//////                try {
//////                    Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
//////                    Update update = new Update()
//////                            .set("active", false)
//////                            .set("updatedBy", userId)
//////                            .set("updatedAt", LocalDateTime.now())
//////                            .set("notes", (reason != null ? "Deactivated: " + reason : "Bulk deactivated"));
//////
//////                    com.mongodb.client.result.UpdateResult result = mongoTemplate.updateFirst(query, update, IpWhitelist.class);
//////
//////                    if (result.getMatchedCount() > 0) {
//////                        deactivatedIds.add(id);
//////                        logger.info("Bulk deactivated IP whitelist entry {} by user {}", id, userId);
//////                    } else {
//////                        failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(id, "unknown", "Entry not found", "NOT_FOUND"));
//////                    }
//////
//////                } catch (Exception e) {
//////                    failedDeletions.add(new BulkDeleteIpResponse.FailedDeletion(id, "unknown", e.getMessage(), "UPDATE_ERROR"));
//////                    logger.error("Failed to deactivate entry {} during bulk operation: {}", id, e.getMessage());
//////                }
//////            }
//////
//////            response.setSuccessfullyDeleted(deactivatedIds.size());
//////            response.setFailedToDelete(failedDeletions.size());
//////            response.setDeletedIds(deactivatedIds);
//////            response.setFailedDeletions(failedDeletions);
//////
//////        } catch (Exception e) {
//////            logger.error("Error during bulk soft delete operation: {}", e.getMessage(), e);
//////            response.setFailedToDelete(response.getTotalRequested());
//////            response.setSuccessfullyDeleted(0);
//////        }
//////
//////        return response;
//////    }
//////
//////    @Override
//////    @Transactional
//////    public BulkDeleteIpResponse deleteByScope(IpWhitelistScope scope, String tenantId, String userId,
//////                                              boolean forceDelete, String reason) {
//////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
//////        response.setPerformedBy(userId);
//////        response.setTimestamp(LocalDateTime.now());
//////
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            // Find entries to delete by scope
//////            Query findQuery = new Query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope));
//////            List<IpWhitelist> entriesToDelete = mongoTemplate.find(findQuery, IpWhitelist.class);
//////
//////            response.setTotalRequested(entriesToDelete.size());
//////
//////            if (entriesToDelete.isEmpty()) {
//////                response.setSuccessfullyDeleted(0);
//////                response.setFailedToDelete(0);
//////                return response;
//////            }
//////
//////            // Extract IDs and use bulk delete
//////            List<String> ids = entriesToDelete.stream().map(IpWhitelist::getId).collect(Collectors.toList());
//////            return bulkDelete(ids, tenantId, userId, forceDelete, false, reason);
//////
//////        } catch (Exception e) {
//////            logger.error("Error during delete by scope operation: {}", e.getMessage(), e);
//////            response.setFailedToDelete(response.getTotalRequested());
//////            response.setSuccessfullyDeleted(0);
//////        }
//////
//////        return response;
//////    }
//////
//////    @Override
//////    @Transactional
//////    public BulkDeleteIpResponse deleteInactiveEntries(String tenantId, int olderThanDays, String userId) {
//////        BulkDeleteIpResponse response = new BulkDeleteIpResponse();
//////        response.setPerformedBy(userId);
//////        response.setTimestamp(LocalDateTime.now());
//////
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            // Find inactive entries older than specified days
//////            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays);
//////            Query findQuery = new Query(Criteria.where("tenantId").is(tenantId)
//////                    .and("active").is(false)
//////                    .and("updatedAt").lt(cutoffDate));
//////
//////            List<IpWhitelist> entriesToDelete = mongoTemplate.find(findQuery, IpWhitelist.class);
//////            response.setTotalRequested(entriesToDelete.size());
//////
//////            if (entriesToDelete.isEmpty()) {
//////                response.setSuccessfullyDeleted(0);
//////                response.setFailedToDelete(0);
//////                return response;
//////            }
//////
//////            // Delete the entries
//////            Query deleteQuery = new Query(Criteria.where("tenantId").is(tenantId)
//////                    .and("active").is(false)
//////                    .and("updatedAt").lt(cutoffDate));
//////
//////            com.mongodb.client.result.DeleteResult result = mongoTemplate.remove(deleteQuery, IpWhitelist.class);
//////
//////            response.setSuccessfullyDeleted((int) result.getDeletedCount());
//////            response.setFailedToDelete(0);
//////
//////            List<String> deletedIds = entriesToDelete.stream().map(IpWhitelist::getId).collect(Collectors.toList());
//////            response.setDeletedIds(deletedIds);
//////
//////            logger.info("Cleaned up {} inactive IP whitelist entries older than {} days for tenant {}",
//////                    result.getDeletedCount(), olderThanDays, tenantId);
//////
//////        } catch (Exception e) {
//////            logger.error("Error during cleanup of inactive entries: {}", e.getMessage(), e);
//////            response.setFailedToDelete(response.getTotalRequested());
//////            response.setSuccessfullyDeleted(0);
//////        }
//////
//////        return response;
//////    }
//////
//////    @Override
//////    public BulkDeletePreview previewBulkDelete(List<String> ids, String tenantId, String userId) {
//////        BulkDeletePreview preview = new BulkDeletePreview();
//////
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            // Fetch entries to preview
//////            Query query = new Query(Criteria.where("id").in(ids).and("tenantId").is(tenantId));
//////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
//////
//////            preview.setTotalEntries(entries.size());
//////
//////            // Create preview entries
//////            List<BulkDeletePreview.PreviewEntry> previewEntries = entries.stream()
//////                    .map(this::createPreviewEntry)
//////                    .collect(Collectors.toList());
//////
//////            preview.setEntries(previewEntries);
//////
//////            // Identify critical entries
//////            List<BulkDeletePreview.PreviewEntry> criticalEntries = previewEntries.stream()
//////                    .filter(BulkDeletePreview.PreviewEntry::isCritical)
//////                    .collect(Collectors.toList());
//////
//////            preview.setCriticalEntries(criticalEntries);
//////            preview.setRequiresForceDelete(!criticalEntries.isEmpty());
//////
//////            // Count by status
//////            int activeCount = (int) entries.stream().filter(IpWhitelist::isActive).count();
//////            preview.setActiveEntries(activeCount);
//////            preview.setInactiveEntries(entries.size() - activeCount);
//////
//////            // Create summaries
//////            Map<IpWhitelistScope, Integer> scopeSummary = entries.stream()
//////                    .collect(Collectors.groupingBy(IpWhitelist::getScope,
//////                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
//////            preview.setScopeSummary(scopeSummary);
//////
//////            Map<IpWhitelistType, Integer> typeSummary = entries.stream()
//////                    .collect(Collectors.groupingBy(IpWhitelist::getType,
//////                            Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)));
//////            preview.setTypeSummary(typeSummary);
//////
//////            // Validate and add warnings
//////            String userRole = getUserRole(userId);
//////            ValidationResult validationResult = validator.validateBulkDelete(entries, false, false, userRole);
//////
//////            List<String> warnings = new ArrayList<>();
//////            List<String> errors = new ArrayList<>();
//////
//////            if (validationResult.hasErrors()) {
//////                warnings.addAll(validationResult.getWarnings());
//////            }
//////
//////            if (validationResult.hasErrors()) {
//////                errors.addAll(validationResult.getErrors().stream()
//////                        .map(error -> error.getCode() + ": " + error.getMessage())
//////                        .collect(Collectors.toList()));
//////            }
//////
//////            preview.setWarnings(warnings);
//////            preview.setErrors(errors);
//////            preview.setCanProceed(!validationResult.hasErrors());
//////
//////            // Impact assessment
//////            preview.setImpactAssessment(generateImpactAssessment(entries));
//////
//////        } catch (Exception e) {
//////            logger.error("Error generating bulk delete preview: {}", e.getMessage(), e);
//////            preview.setCanProceed(false);
//////            preview.setErrors(Collections.singletonList("Preview generation failed: " + e.getMessage()));
//////        }
//////
//////        return preview;
//////    }
//////
//////    // ============================================================================
//////    // HELPER METHODS FOR BULK OPERATIONS
//////    // ============================================================================
//////
//////    private boolean canDeleteEntry(IpWhitelist entry, String userId, boolean forceDelete) {
//////        // Check if user has permission to delete this entry
//////        if (entry.getScope() == IpWhitelistScope.ADMIN || entry.getScope() == IpWhitelistScope.GLOBAL) {
//////            // Only superadmin can delete admin/global entries
//////            return getUserRole(userId).equals("SUPERADMIN");
//////        }
//////
//////        if (entry.isCriticalIpRange() && !forceDelete) {
//////            return false;
//////        }
//////
//////        return true;
//////    }
//////
//////    private String getUserRole(String userId) {
//////        // This should integrate with your user service to get actual roles
//////        // For now, returning a default - implement based on your UserService
//////        try {
//////            // Example integration with UserService
//////            // return userService.getUserRole(userId);
//////            return "ADMIN"; // Default fallback
//////        } catch (Exception e) {
//////            logger.warn("Could not determine user role for {}: {}", userId, e.getMessage());
//////            return "USER";
//////        }
//////    }
//////
//////    private BulkDeletePreview.PreviewEntry createPreviewEntry(IpWhitelist entry) {
//////        BulkDeletePreview.PreviewEntry previewEntry = new BulkDeletePreview.PreviewEntry(
//////                entry.getId(),
//////                entry.getIpAddress(),
//////                entry.getDescription(),
//////                entry.getScope(),
//////                entry.getType(),
//////                entry.isActive()
//////        );
//////
//////        // Set affected users/roles
//////        if (entry.getAllowedUserIds() != null) {
//////            previewEntry.setAffectedUsers(new ArrayList<>(entry.getAllowedUserIds()));
//////        }
//////        if (entry.getAllowedRoleIds() != null) {
//////            previewEntry.setAffectedRoles(new ArrayList<>(entry.getAllowedRoleIds()));
//////        }
//////
//////        return previewEntry;
//////    }
//////
//////    private Map<String, Object> createDeletionSummary(List<IpWhitelist> originalEntries, List<String> deletedIds, String reason) {
//////        Map<String, Object> summary = new HashMap<>();
//////
//////        List<IpWhitelist> deletedEntries = originalEntries.stream()
//////                .filter(entry -> deletedIds.contains(entry.getId()))
//////                .collect(Collectors.toList());
//////
//////        // Summary by scope
//////        Map<IpWhitelistScope, Long> scopeCounts = deletedEntries.stream()
//////                .collect(Collectors.groupingBy(IpWhitelist::getScope, Collectors.counting()));
//////        summary.put("deletedByScope", scopeCounts);
//////
//////        // Summary by type
//////        Map<IpWhitelistType, Long> typeCounts = deletedEntries.stream()
//////                .collect(Collectors.groupingBy(IpWhitelist::getType, Collectors.counting()));
//////        summary.put("deletedByType", typeCounts);
//////
//////        // Critical entries count
//////        long criticalCount = deletedEntries.stream()
//////                .filter(IpWhitelist::isCriticalIpRange)
//////                .count();
//////        summary.put("criticalEntriesDeleted", criticalCount);
//////
//////        // Reason
//////        summary.put("reason", reason != null ? reason : "Bulk deletion");
//////        summary.put("timestamp", LocalDateTime.now());
//////
//////        return summary;
//////    }
//////
//////    private String generateImpactAssessment(List<IpWhitelist> entries) {
//////        int criticalCount = (int) entries.stream().filter(IpWhitelist::isCriticalIpRange).count();
//////        int activeCount = (int) entries.stream().filter(IpWhitelist::isActive).count();
//////        int userSpecificCount = (int) entries.stream().filter(e -> e.getScope() == IpWhitelistScope.USER_SPECIFIC).count();
//////
//////        StringBuilder assessment = new StringBuilder();
//////
//////        if (criticalCount > 0) {
//////            assessment.append(String.format("%d critical IP ranges will be deleted. ", criticalCount));
//////        }
//////
//////        if (activeCount > 0) {
//////            assessment.append(String.format("%d active entries will be deleted, potentially affecting current access. ", activeCount));
//////        }
//////
//////        if (userSpecificCount > 0) {
//////            assessment.append(String.format("%d user-specific entries may affect individual user sessions. ", userSpecificCount));
//////        }
//////
//////        if (assessment.length() == 0) {
//////            assessment.append("Low impact - mostly inactive or non-critical entries.");
//////        } else {
//////            assessment.append("Review carefully before proceeding.");
//////        }
//////
//////        return assessment.toString();
//////    }
////////    @Override
////////    public ValidationResult validateUserIpAccess(String clientIp, String tenantId, String userId, Set<String> userRoles) {
////////        try {
////////            if (isIpWhitelisted(clientIp, tenantId, userId, userRoles)) {
////////                return ValidationResult.success("IP access granted");
////////            }
////////            return ValidationResult.failure("IP address not whitelisted for this user/tenant");
////////        } catch (Exception e) {
////////            logger.error("Error validating IP access: {}", e.getMessage());
////////            return ValidationResult.failure("Validation error occurred");
////////        }
////////    }
//////
//////    @Override
//////    public IpWhitelist create(IpWhitelist ipWhitelist, String tenantId, String createdBy) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            ipWhitelist.setTenantId(tenantId);
//////            ipWhitelist.setCreatedBy(createdBy);
//////            ipWhitelist.setCreatedDate(LocalDateTime.now());
//////            ipWhitelist.setActive(true);
//////
//////            return mongoTemplate.save(ipWhitelist);
//////        } catch (Exception e) {
//////            logger.error("Error creating IP whitelist entry: {}", e.getMessage());
//////            throw new RuntimeException("Failed to create IP whitelist entry", e);
//////        }
//////    }
//////
//////    @Override
//////    public Optional<IpWhitelist> getById(String id, String tenantId) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////            Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
//////            IpWhitelist result = mongoTemplate.findOne(query, IpWhitelist.class);
//////            return Optional.ofNullable(result);
//////        } catch (Exception e) {
//////            logger.error("Error retrieving IP whitelist entry by ID {}: {}", id, e.getMessage());
//////            return Optional.empty();
//////        }
//////    }
//////
//////    @Override
//////    public Page<IpWhitelist> getAllByTenant(String tenantId, Pageable pageable) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////            Query query = new Query(Criteria.where("tenantId").is(tenantId))
//////                    .with(pageable);
//////
//////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
//////            long total = mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId)), IpWhitelist.class);
//////
//////            return new PageImpl<>(entries, pageable, total);
//////        } catch (Exception e) {
//////            logger.error("Error retrieving IP whitelist entries for tenant {}: {}", tenantId, e.getMessage());
//////            return Page.empty(pageable);
//////        }
//////    }
//////
//////
//////
//////    @Override
//////    public IpWhitelist update(String id, IpWhitelist ipWhitelist, String tenantId, String updatedBy) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            // Build query to match by ID and tenant
//////            Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
//////
//////            // Build update object
//////            Update update = new Update()
//////                    .set("description", ipWhitelist.getDescription())
//////                    .set("isActive", ipWhitelist.isActive()) // <--- corrected
//////                    .set("updatedBy", updatedBy)
//////                    .set("updatedAt", LocalDateTime.now())
//////                    .set("notes", ipWhitelist.getNotes())
//////                    .set("ipAddress", ipWhitelist.getIpAddress());
//////
//////
//////            // Apply update
//////            mongoTemplate.updateFirst(query, update, IpWhitelist.class);
//////
//////            // Retrieve updated entity
//////            return getById(id, tenantId)
//////                    .orElseThrow(() -> new RuntimeException("Entry not found after update"));
//////        } catch (Exception e) {
//////            logger.error("Error updating IP whitelist entry {}: {}", id, e.getMessage(), e);
//////            throw new RuntimeException("Failed to update IP whitelist entry", e);
//////        }
//////    }
//////
//////    @Override
//////    public void delete(String id, String tenantId) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////            Query query = new Query(Criteria.where("id").is(id).and("tenantId").is(tenantId));
//////            mongoTemplate.remove(query, IpWhitelist.class);
//////        } catch (Exception e) {
//////            logger.error("Error deleting IP whitelist entry {}: {}", id, e.getMessage());
//////            throw new RuntimeException("Failed to delete IP whitelist entry", e);
//////        }
//////    }
//////    @Data
//////    @NoArgsConstructor
//////    @AllArgsConstructor
//////    private static class ActiveStats {
//////        private Boolean id; // mapped from "_id" of aggregation
//////        private long count;
//////    }
//////
//////    public Map<String, Object> getStatistics(String tenantId) {
//////        try {
//////            // Aggregation: filter by tenant, then group by "isActive"
//////            Aggregation agg = Aggregation.newAggregation(
//////                    Aggregation.match(Criteria.where("tenantId").is(tenantId)),
//////                    Aggregation.group("isActive").count().as("count")
//////            );
//////
//////            AggregationResults<ActiveStats> result =
//////                    mongoTemplateProvider.getMongoTemplate().aggregate(agg, IpWhitelist.class, ActiveStats.class);
//////
//////            long total = 0;
//////            long activeCount = 0;
//////            long inactiveCount = 0;
//////
//////            for (ActiveStats stat : result.getMappedResults()) {
//////                Boolean isActive = stat.getId();
//////                long count = stat.getCount();
//////
//////                total += count;
//////
//////                if (Boolean.TRUE.equals(isActive)) {
//////                    activeCount += count;
//////                } else if (Boolean.FALSE.equals(isActive)) {
//////                    inactiveCount += count;
//////                } else {
//////                    logger.warn("Unexpected value for isActive in aggregation: {}", isActive);
//////                }
//////            }
//////
//////            Map<String, Object> stats = new HashMap<>();
//////            stats.put("totalEntries", total);
//////            stats.put("activeEntries", activeCount);
//////            stats.put("inactiveEntries", inactiveCount);
//////
//////            return stats;
//////
//////        } catch (Exception e) {
//////            logger.error("Error retrieving statistics for tenant {}: {}", tenantId, e.getMessage(), e);
//////            return new HashMap<>();
//////        }
//////    }
//////
//////    @Override
//////    public ValidationResult validateUserIpAccess(String clientIp, String tenantId, String userId, Set<String> userRoles) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            // Build query for IP matching with active entries only
//////            Criteria ipCriteria = buildIpMatchingCriteria(clientIp, tenantId);
//////            Query query = new Query(ipCriteria);
//////            query.with(Sort.by(Sort.Direction.DESC, "priority", "scope"));
//////
//////            List<IpWhitelist> matchingEntries = mongoTemplate.find(query, IpWhitelist.class);
//////
//////            if (matchingEntries.isEmpty()) {
//////                return ValidationResult.denied("No IP whitelist entries found for IP: " + clientIp, "NO_WHITELIST_ENTRY");
//////            }
//////
//////            // Check entries in priority order
//////            for (IpWhitelist entry : matchingEntries) {
//////                if (entry.appliesToUser(userId, userRoles)) {
//////                    // Update access tracking asynchronously
//////                    updateLastAccess(entry.getId(), tenantId, clientIp, userId);
//////
//////                    return ValidationResult.allowed(
//////                            "Access granted via " + entry.getScope() + " scope",
//////                            entry,
//////                            entry.getScope()
//////                    );
//////                }
//////            }
//////
//////            return ValidationResult.denied("IP allowed but user/role access denied", "USER_ROLE_ACCESS_DENIED");
//////
//////        } catch (Exception e) {
//////            logger.error("Error validating user IP access for IP {} and user {}: {}", clientIp, userId, e.getMessage(), e);
//////            return ValidationResult.error("Validation error: " + e.getMessage(), "VALIDATION_ERROR");
//////        }
//////    }
//////
//////    @Override
//////    public void updateLastAccess(String entryId, String tenantId, String clientIp, String userId) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////            Query query = new Query(Criteria.where("id").is(entryId).and("tenantId").is(tenantId));
//////
//////            Update update = new Update()
//////                    .set("lastAccessedAt", LocalDateTime.now())
//////                    .set("lastAccessedBy", userId)
//////                    .set("lastAccessedIp", clientIp)
//////                    .inc("accessCount", 1);
//////
//////            mongoTemplate.updateFirst(query, update, IpWhitelist.class);
//////
//////        } catch (Exception e) {
//////            logger.warn("Failed to update last access for entry {}: {}", entryId, e.getMessage());
//////            // Don't throw exception as this is tracking data
//////        }
//////    }
//////
//////    // ============================================================================
//////    // ADVANCED CREATION METHODS
//////    // ============================================================================
//////
//////    @Override
//////    public IpWhitelist createUserSpecific(String tenantId, String ipAddress, String description,
//////                                          Set<String> userIds, String createdBy) {
//////        IpWhitelist ipWhitelist = new IpWhitelist();
//////        ipWhitelist.setTenantId(tenantId);
//////        ipWhitelist.setIpAddress(ipAddress);
//////        ipWhitelist.setDescription(description);
//////        ipWhitelist.setScope(IpWhitelistScope.USER_SPECIFIC);
//////        ipWhitelist.setType(IpWhitelistType.USER_SPECIFIC);
//////        ipWhitelist.setAllowedUserIds(userIds != null ? new HashSet<>(userIds) : new HashSet<>());
//////        ipWhitelist.setCreatedBy(createdBy);
//////        ipWhitelist.setPriority(IpWhitelistScope.USER_SPECIFIC.getPrecedence() / 100);
//////
//////        return create(ipWhitelist, tenantId, createdBy);
//////    }
//////
//////    @Override
//////    public IpWhitelist createHybrid(String tenantId, String ipAddress, String description,
//////                                    Set<String> userIds, Set<String> roleIds, String createdBy) {
//////        IpWhitelist ipWhitelist = new IpWhitelist();
//////        ipWhitelist.setTenantId(tenantId);
//////        ipWhitelist.setIpAddress(ipAddress);
//////        ipWhitelist.setDescription(description);
//////        ipWhitelist.setScope(IpWhitelistScope.HYBRID);
//////        ipWhitelist.setType(IpWhitelistType.HYBRID);
//////        ipWhitelist.setAllowedUserIds(userIds != null ? new HashSet<>(userIds) : new HashSet<>());
//////        ipWhitelist.setAllowedRoleIds(roleIds != null ? new HashSet<>(roleIds) : new HashSet<>());
//////        ipWhitelist.setCreatedBy(createdBy);
//////        ipWhitelist.setPriority(IpWhitelistScope.HYBRID.getPrecedence() / 100);
//////
//////        return create(ipWhitelist, tenantId, createdBy);
//////    }
//////
//////    // ============================================================================
//////    // ADVANCED QUERYING METHODS
//////    // ============================================================================
//////
//////    @Override
//////    public Page<IpWhitelist> getEntriesByScope(String tenantId, IpWhitelistScope scope, Pageable pageable) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////            Query query = new Query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope));
//////            query.with(pageable);
//////
//////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
//////            long total = mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope)), IpWhitelist.class);
//////
//////            return new PageImpl<>(entries, pageable, total);
//////        } catch (Exception e) {
//////            logger.error("Error fetching entries by scope {} for tenant {}: {}", scope, tenantId, e.getMessage());
//////            return new PageImpl<>(Collections.emptyList(), pageable, 0);
//////        }
//////    }
//////
//////    @Override
//////    public Page<IpWhitelist> getEntriesForUserId(String tenantId, String userId, Pageable pageable) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            Criteria criteria = Criteria.where("tenantId").is(tenantId)
//////                    .andOperator(
//////                            Criteria.where("allowedUserIds").in(userId)
//////                    );
//////
//////            Query query = new Query(criteria);
//////            query.with(pageable);
//////
//////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
//////            long total = mongoTemplate.count(Query.query(criteria), IpWhitelist.class);
//////
//////            return new PageImpl<>(entries, pageable, total);
//////        } catch (Exception e) {
//////            logger.error("Error fetching entries for user {} in tenant {}: {}", userId, tenantId, e.getMessage());
//////            return new PageImpl<>(Collections.emptyList(), pageable, 0);
//////        }
//////    }
//////
//////    @Override
//////    public Page<IpWhitelist> getEntriesForRoleId(String tenantId, String roleId, Pageable pageable) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            Criteria criteria = Criteria.where("tenantId").is(tenantId)
//////                    .andOperator(
//////                            Criteria.where("allowedRoleIds").in(roleId)
//////                    );
//////
//////            Query query = new Query(criteria);
//////            query.with(pageable);
//////
//////            List<IpWhitelist> entries = mongoTemplate.find(query, IpWhitelist.class);
//////            long total = mongoTemplate.count(Query.query(criteria), IpWhitelist.class);
//////
//////            return new PageImpl<>(entries, pageable, total);
//////        } catch (Exception e) {
//////            logger.error("Error fetching entries for role {} in tenant {}: {}", roleId, tenantId, e.getMessage());
//////            return new PageImpl<>(Collections.emptyList(), pageable, 0);
//////        }
//////    }
//////
//////    @Override
//////    public List<IpWhitelist> getEntriesForUser(String tenantId, String userId, Set<String> userRoles) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////
//////            // Build criteria for entries that apply to this user
//////            List<Criteria> criteriaList = new ArrayList<>();
//////
//////            // Global and Tenant scope entries
//////            criteriaList.add(Criteria.where("scope").in(IpWhitelistScope.GLOBAL, IpWhitelistScope.TENANT, IpWhitelistScope.ADMIN));
//////
//////            // User-specific entries
//////            criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.USER_SPECIFIC)
//////                    .and("allowedUserIds").in(userId));
//////
//////            // Role-specific entries
//////            if (userRoles != null && !userRoles.isEmpty()) {
//////                criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.ROLE_SPECIFIC)
//////                        .and("allowedRoleIds").in(userRoles));
//////            }
//////
//////            // Hybrid entries (user OR role match)
//////            List<Criteria> hybridCriteria = new ArrayList<>();
//////            hybridCriteria.add(Criteria.where("allowedUserIds").in(userId));
//////            if (userRoles != null && !userRoles.isEmpty()) {
//////                hybridCriteria.add(Criteria.where("allowedRoleIds").in(userRoles));
//////            }
//////            criteriaList.add(Criteria.where("scope").is(IpWhitelistScope.HYBRID)
//////                    .orOperator(hybridCriteria.toArray(new Criteria[0])));
//////
//////            Criteria mainCriteria = Criteria.where("tenantId").is(tenantId)
//////                    .and("active").is(true)
//////                    .orOperator(criteriaList.toArray(new Criteria[0]));
//////
//////            Query query = new Query(mainCriteria);
//////            query.with(Sort.by(Sort.Direction.DESC, "priority", "scope"));
//////
//////            return mongoTemplate.find(query, IpWhitelist.class);
//////
//////        } catch (Exception e) {
//////            logger.error("Error fetching entries for user {} with roles {} in tenant {}: {}",
//////                    userId, userRoles, tenantId, e.getMessage());
//////            return Collections.emptyList();
//////        }
//////    }
//////
//////    @Override
//////    public Map<String, Object> getAdvancedStatistics(String tenantId) {
//////        try {
//////            MongoTemplate mongoTemplate = mongoTemplateProvider.getMongoTemplate();
//////            Map<String, Object> stats = new HashMap<>();
//////
//////            // Basic counts
//////            long total = mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId)), IpWhitelist.class);
//////            long active = mongoTemplate.count(Query.query(Criteria.where("tenantId").is(tenantId).and("active").is(true)), IpWhitelist.class);
//////
//////            stats.put("totalEntries", total);
//////            stats.put("activeEntries", active);
//////            stats.put("inactiveEntries", total - active);
//////
//////            // Statistics by scope
//////            Map<String, Long> scopeStats = new HashMap<>();
//////            for (IpWhitelistScope scope : IpWhitelistScope.values()) {
//////                long count = mongoTemplate.count(
//////                        Query.query(Criteria.where("tenantId").is(tenantId).and("scope").is(scope)),
//////                        IpWhitelist.class);
//////                scopeStats.put(scope.name(), count);
//////            }
//////            stats.put("byScope", scopeStats);
//////
//////            // Statistics by type
//////            Map<String, Long> typeStats = new HashMap<>();
//////            for (IpWhitelistType type : IpWhitelistType.values()) {
//////                long count = mongoTemplate.count(
//////                        Query.query(Criteria.where("tenantId").is(tenantId).and("type").is(type)),
//////                        IpWhitelist.class);
//////                typeStats.put(type.name(), count);
//////            }
//////            stats.put("byType", typeStats);
//////
//////            // Access statistics
//////            Aggregation aggregation = Aggregation.newAggregation(
//////                    Aggregation.match(Criteria.where("tenantId").is(tenantId)),
//////                    Aggregation.group()
//////                            .sum("accessCount").as("totalAccess")
//////                            .avg("accessCount").as("avgAccess")
//////                            .count().as("entriesWithAccess")
//////            );
//////
//////            AggregationResults<Map> accessResults = mongoTemplate.aggregate(aggregation, "ip_whitelist", Map.class);
//////            Map accessStats = accessResults.getUniqueMappedResult();
//////            if (accessStats != null) {
//////                stats.put("accessStats", accessStats);
//////            }
//////
//////            // Recent activity
//////            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
//////            long recentlyAccessed = mongoTemplate.count(
//////                    Query.query(Criteria.where("tenantId").is(tenantId).and("lastAccessedAt").gte(weekAgo)),
//////                    IpWhitelist.class);
//////            stats.put("recentlyAccessedCount", recentlyAccessed);
//////
//////            return stats;
//////
//////        } catch (Exception e) {
//////            logger.error("Error retrieving advanced statistics for tenant {}: {}", tenantId, e.getMessage());
//////            Map<String, Object> errorStats = new HashMap<>();
//////            errorStats.put("error", "Unable to retrieve statistics: " + e.getMessage());
//////            return errorStats;
//////        }
//////    }
//////
//////    // ============================================================================
//////    // HELPER METHODS
//////    // ============================================================================
//////
//////    private Criteria buildIpMatchingCriteria(String clientIp, String tenantId) {
//////        List<Criteria> ipCriteriaList = new ArrayList<>();
//////
//////        // Exact IP match
//////        ipCriteriaList.add(Criteria.where("ipAddress").is(clientIp));
//////
//////        // Wildcard match
//////        ipCriteriaList.add(Criteria.where("ipAddress").is("*"));
//////
//////        // CIDR range matching (simplified - for production, use proper CIDR matching)
//////        ipCriteriaList.add(Criteria.where("ipAddress").regex("^" + clientIp.substring(0, clientIp.lastIndexOf('.')) + "\\.\\d+/\\d+$"));
//////
//////        // Global access (0.0.0.0/0)
//////        ipCriteriaList.add(Criteria.where("ipAddress").in("0.0.0.0/0", "::/0"));
//////
//////        return Criteria.where("tenantId").is(tenantId)
//////                .and("active").is(true)
//////                .orOperator(ipCriteriaList.toArray(new Criteria[0]));
//////    }
//////
//////
//////    @Override
//////    public IpWhitelist createRoleSpecific(String tenantId, String ipAddress, String description,
//////                                          Set<String> roleIds, String createdBy) {
//////        IpWhitelist ipWhitelist = new IpWhitelist();
//////        ipWhitelist.setTenantId(tenantId);
//////        ipWhitelist.setIpAddress(ipAddress);
//////        ipWhitelist.setDescription(description);
//////        ipWhitelist.setScope(IpWhitelistScope.ROLE_SPECIFIC);
//////        ipWhitelist.setType(IpWhitelistType.ROLE_SPECIFIC);
//////        ipWhitelist.setAllowedRoleIds(roleIds != null ? new HashSet<>(roleIds) : new HashSet<>());
//////        ipWhitelist.setCreatedBy(createdBy);
//////        ipWhitelist.setPriority(IpWhitelistScope.ROLE_SPECIFIC.getPrecedence() / 100);
//////
//////        return create(ipWhitelist, tenantId, createdBy);
//////    }
//////}