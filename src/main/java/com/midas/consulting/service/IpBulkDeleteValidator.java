package com.midas.consulting.service;

import com.midas.consulting.model.IpWhitelist;
import com.midas.consulting.model.IpWhitelistScope;
import com.midas.consulting.model.IpWhitelistType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class IpBulkDeleteValidator {

    // Critical IP ranges that should never be deleted without explicit confirmation
    private static final Set<String> CRITICAL_IP_RANGES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    "127.0.0.1",      // Localhost
                    "0.0.0.0/0",      // All IPs
                    "10.0.0.0/8",     // Private network A
                    "172.16.0.0/12",  // Private network B
                    "192.168.0.0/16"  // Private network C
            )));

    // Maximum number of entries that can be deleted in a single operation
    private static final int MAX_BULK_DELETE_SIZE = 100;

    /**
     * Validate bulk delete operation
     */
    public ValidationResult validateBulkDelete(
            List<IpWhitelist> entriesToDelete,
            boolean forceDelete,
            boolean skipValidation,
            String userRole) {

        ValidationResult result = ValidationResult.forBulkOperation();

        // Rule 1: Check maximum bulk delete limit
        if (entriesToDelete.size() > MAX_BULK_DELETE_SIZE) {
            result.addError("BULK_LIMIT_EXCEEDED",
                    "Cannot delete more than " + MAX_BULK_DELETE_SIZE + " entries at once");
            return result;
        }

        // Rule 2: Check for critical IP ranges
        if (!skipValidation) {
            List<IpWhitelist> criticalEntries = entriesToDelete.stream()
                    .filter(entry -> isCriticalIpRange(entry.getIpAddress()))
                    .collect(Collectors.toList());

            if (!criticalEntries.isEmpty() && !forceDelete) {
                result.addError("CRITICAL_IP_DETECTED",
                        "Critical IP ranges detected. Use forceDelete=true to proceed");
                criticalEntries.forEach(entry ->
                        result.addWarning("Critical IP: " + entry.getIpAddress()));
            }
        }

        // Rule 3: Check for currently active entries
        if (!forceDelete) {
            List<IpWhitelist> activeEntries = entriesToDelete.stream()
                    .filter(IpWhitelist::isActive)
                    .collect(Collectors.toList());

            if (!activeEntries.isEmpty()) {
                result.addWarning("ACTIVE_ENTRIES_DETECTED",
                        activeEntries.size() + " active entries will be deleted");
            }
        }

        // Rule 4: Check for admin-only entries (only SUPERADMIN can delete)
        if (!"SUPERADMIN".equals(userRole)) {
            List<IpWhitelist> adminEntries = entriesToDelete.stream()
                    .filter(entry -> IpWhitelistScope.ADMIN.equals(entry.getScope()) ||
                            IpWhitelistScope.GLOBAL.equals(entry.getScope()))
                    .collect(Collectors.toList());

            if (!adminEntries.isEmpty()) {
                result.addError("INSUFFICIENT_PERMISSIONS",
                        "Cannot delete admin/global scoped entries. SUPERADMIN role required");
            }
        }

        // Rule 5: Check for user-specific entries that might affect active sessions
        List<IpWhitelist> userSpecificEntries = entriesToDelete.stream()
                .filter(entry -> IpWhitelistType.USER_SPECIFIC.equals(entry.getType())
                        || IpWhitelistType.HYBRID.equals(entry.getType()))
                .collect(Collectors.toList());

        if (!userSpecificEntries.isEmpty()) {
            result.addWarning("USER_SESSIONS_WARNING",
                    "Deleting user-specific entries may affect active user sessions");
        }

        // Rule 6: Check for last remaining whitelist entry per tenant
        Map<String, List<IpWhitelist>> entriesByTenant = entriesToDelete.stream()
                .collect(Collectors.groupingBy(IpWhitelist::getTenantId));

        // Add warning for large deletions
        if (entriesToDelete.size() > 50) {
            result.addWarning("LARGE_DELETION_WARNING",
                    "Large number of entries being deleted. Ensure tenant access is maintained");
        }

        // Rule 7: Check for entries that grant broad access
        List<IpWhitelist> broadAccessEntries = entriesToDelete.stream()
                .filter(entry -> entry.getScope() == IpWhitelistScope.TENANT ||
                        entry.getScope() == IpWhitelistScope.GLOBAL ||
                        entry.getIpAddress().contains("/0"))
                .collect(Collectors.toList());

        if (!broadAccessEntries.isEmpty()) {
            result.addWarning("BROAD_ACCESS_WARNING",
                    broadAccessEntries.size() + " entries grant broad access and will be deleted");
        }

        // Rule 8: Validate dependencies (simplified - in real implementation you'd check actual usage)
        validateDependencies(entriesToDelete, result);

        return result;
    }

    /**
     * Validate individual entry for deletion
     */
    public ValidationResult validateSingleDelete(IpWhitelist entry, String userRole, boolean forceDelete) {
        ValidationResult result = ValidationResult.forBulkOperation();

        // Check permissions
        if (!canUserDeleteEntry(entry, userRole)) {
            result.addError("INSUFFICIENT_PERMISSIONS",
                    "User does not have permission to delete this entry");
            return result;
        }

        // Check if critical and force delete not specified
        if (isCriticalIpRange(entry.getIpAddress()) && !forceDelete) {
            result.addError("CRITICAL_IP_REQUIRES_FORCE",
                    "Critical IP range requires forceDelete=true");
        }

        // Check if active
        if (entry.isActive()) {
            result.addWarning("DELETING_ACTIVE_ENTRY",
                    "Deleting an active IP whitelist entry");
        }

        return result;
    }

    /**
     * Check if IP range is considered critical
     */
    private boolean isCriticalIpRange(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }

        return CRITICAL_IP_RANGES.contains(ipAddress) ||
                ipAddress.equals("0.0.0.0") ||
                ipAddress.startsWith("127.") ||
                ipAddress.contains("/0") ||
                ipAddress.equals("*");
    }

    /**
     * Check if user can delete a specific entry
     */
    private boolean canUserDeleteEntry(IpWhitelist entry, String userRole) {
        if ("SUPERADMIN".equals(userRole)) {
            return true; // Superadmin can delete anything
        }

        if ("ADMIN".equals(userRole)) {
            // Admin cannot delete global or admin scoped entries
            return entry.getScope() != IpWhitelistScope.GLOBAL &&
                    entry.getScope() != IpWhitelistScope.ADMIN;
        }

        if ("IP_MANAGER".equals(userRole)) {
            // IP Manager can only delete tenant and user/role specific entries
            return entry.getScope() == IpWhitelistScope.TENANT ||
                    entry.getScope() == IpWhitelistScope.USER_SPECIFIC ||
                    entry.getScope() == IpWhitelistScope.ROLE_SPECIFIC ||
                    entry.getScope() == IpWhitelistScope.HYBRID;
        }

        return false; // Default deny
    }

    /**
     * Validate dependencies and potential impact
     */
    private void validateDependencies(List<IpWhitelist> entriesToDelete, ValidationResult result) {
        // Check for potential tenant lockout
        Map<String, List<IpWhitelist>> byTenant = entriesToDelete.stream()
                .collect(Collectors.groupingBy(IpWhitelist::getTenantId));

        for (Map.Entry<String, List<IpWhitelist>> tenantEntry : byTenant.entrySet()) {
            String tenantId = tenantEntry.getKey();
            List<IpWhitelist> tenantEntries = tenantEntry.getValue();

            // Check if all tenant entries are being deleted
            long tenantScopeCount = tenantEntries.stream()
                    .filter(entry -> entry.getScope() == IpWhitelistScope.TENANT)
                    .count();

            if (tenantScopeCount > 0) {
                result.addWarning("TENANT_ACCESS_WARNING",
                        "Deleting tenant-wide entries for tenant " + tenantId +
                                " - ensure alternative access methods exist");
            }
        }

        // Check for user impact
        Set<String> affectedUsers = entriesToDelete.stream()
                .filter(entry -> entry.getAllowedUserIds() != null)
                .flatMap(entry -> entry.getAllowedUserIds().stream())
                .collect(Collectors.toSet());

        if (!affectedUsers.isEmpty()) {
            result.addWarning("USER_ACCESS_WARNING",
                    affectedUsers.size() + " users may lose specific IP access");
        }

        // Check for role impact
        Set<String> affectedRoles = entriesToDelete.stream()
                .filter(entry -> entry.getAllowedRoleIds() != null)
                .flatMap(entry -> entry.getAllowedRoleIds().stream())
                .collect(Collectors.toSet());

        if (!affectedRoles.isEmpty()) {
            result.addWarning("ROLE_ACCESS_WARNING",
                    affectedRoles.size() + " roles may lose specific IP access");
        }
    }

    /**
     * Get maximum allowed bulk delete size
     */
    public int getMaxBulkDeleteSize() {
        return MAX_BULK_DELETE_SIZE;
    }

    /**
     * Check if an IP address is in critical ranges
     */
    public boolean isCriticalIp(String ipAddress) {
        return isCriticalIpRange(ipAddress);
    }
}