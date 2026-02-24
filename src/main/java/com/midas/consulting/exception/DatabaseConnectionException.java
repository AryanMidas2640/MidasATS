package com.midas.consulting.exception;

public class DatabaseConnectionException extends RuntimeException {

    private final String tenantId;
    
    public DatabaseConnectionException(String tenantId, String message) {
        super(message);
        this.tenantId = tenantId;
    }
    
    public DatabaseConnectionException(String tenantId, String message, Throwable cause) {
        super(message, cause);
        this.tenantId = tenantId;
    }
    
    public String getTenantId() {
        return tenantId;
    }
}