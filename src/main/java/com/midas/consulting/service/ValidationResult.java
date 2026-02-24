package com.midas.consulting.service;

import com.midas.consulting.model.IpWhitelist;
import com.midas.consulting.model.IpWhitelistScope;
import java.util.List;
import java.util.ArrayList;

/**
 * Consolidated validation result class for IP access validation
 * and bulk operation validation results
 */
public class ValidationResult {
    private boolean valid;
    private boolean allowed;
    private String message;
    private String errorCode;
    private IpWhitelist matchedEntry;
    private IpWhitelistScope scope;

    // For bulk operations
    private List<ValidationError> errors;
    private List<String> warnings;

    // Private constructor to enforce factory methods
    private ValidationResult(boolean valid, boolean allowed, String message, String errorCode,
                             IpWhitelist matchedEntry, IpWhitelistScope scope) {
        this.valid = valid;
        this.allowed = allowed;
        this.message = message;
        this.errorCode = errorCode;
        this.matchedEntry = matchedEntry;
        this.scope = scope;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    // Factory method for allowed access
    public static ValidationResult allowed(String message, IpWhitelist matchedEntry, IpWhitelistScope scope) {
        return new ValidationResult(true, true, message, null, matchedEntry, scope);
    }

    // Factory method for denied access
    public static ValidationResult denied(String message) {
        return new ValidationResult(false, false, message, "ACCESS_DENIED", null, null);
    }

    // Factory method for denied access with error code
    public static ValidationResult denied(String message, String errorCode) {
        return new ValidationResult(false, false, message, errorCode, null, null);
    }

    // Factory method for validation errors
    public static ValidationResult error(String message, String errorCode) {
        return new ValidationResult(false, false, message, errorCode, null, null);
    }

    // Factory method for validation errors without code
    public static ValidationResult error(String message) {
        return error(message, "VALIDATION_ERROR");
    }

    // Factory method for bulk operations
    public static ValidationResult forBulkOperation() {
        return new ValidationResult(true, true, null, null, null, null);
    }

    // Factory methods for scope validation
    public static ValidationResult success(String message) {
        return new ValidationResult(true, true, message, null, null, null);
    }

    public static ValidationResult warning(String message) {
        ValidationResult result = new ValidationResult(true, true, message, "WARNING", null, null);
        result.addWarning(message);
        return result;
    }

    public static ValidationResult info(String message) {
        return new ValidationResult(true, true, message, "INFO", null, null);
    }

    // CRITICAL: Uncommented getters - these MUST be active
    public boolean isValid() {
        return valid;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public IpWhitelist getMatchedEntry() {
        return matchedEntry;
    }

    public IpWhitelistScope getScope() {
        return scope;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    // Setters for cases where you need to modify the result
    public void setValid(boolean valid) {
        this.valid = valid;
        if (!valid && this.allowed) {
            this.allowed = false;
        }
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
        if (allowed && !this.valid) {
            this.valid = true;
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setMatchedEntry(IpWhitelist matchedEntry) {
        this.matchedEntry = matchedEntry;
    }

    public void setScope(IpWhitelistScope scope) {
        this.scope = scope;
    }

    // Bulk operation methods
    public void addError(String code, String message) {
        this.valid = false;
        this.allowed = false;
        this.errors.add(new ValidationError(code, message));
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public void addWarning(String code, String message) {
        this.warnings.add(code + ": " + message);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    // Convenience methods
    public boolean hasError() {
        return errorCode != null || hasErrors();
    }

    public boolean isSuccess() {
        return valid && allowed;
    }

    public boolean isFailure() {
        return !valid || !allowed;
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", allowed=" + allowed +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", matchedEntry=" + (matchedEntry != null ? matchedEntry.getId() : null) +
                ", scope=" + scope +
                ", errors=" + errors.size() +
                ", warnings=" + warnings.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationResult that = (ValidationResult) o;

        if (valid != that.valid) return false;
        if (allowed != that.allowed) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (errorCode != null ? !errorCode.equals(that.errorCode) : that.errorCode != null) return false;
        if (matchedEntry != null ? !matchedEntry.equals(that.matchedEntry) : that.matchedEntry != null) return false;
        return scope == that.scope;
    }

    @Override
    public int hashCode() {
        int result = (valid ? 1 : 0);
        result = 31 * result + (allowed ? 1 : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
        result = 31 * result + (matchedEntry != null ? matchedEntry.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }

    // Inner class for validation errors in bulk operations
    public static class ValidationError {
        private String code;
        private String message;

        public ValidationError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return code + ": " + message;
        }
    }
}