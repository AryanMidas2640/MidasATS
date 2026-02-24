package com.midas.consulting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TenantContext {
    private static final Logger logger = LoggerFactory.getLogger(TenantContext.class);
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        logger.debug("Getting current tenant: {}", tenant);
        return tenant;
    }

    public static void setCurrentTenant(String tenantId) {
        logger.debug("Setting current tenant: {}", tenantId);
        CURRENT_TENANT.set(tenantId);
    }

    public static void clear() {
        logger.debug("Clearing tenant context");
        CURRENT_TENANT.remove();
    }
}