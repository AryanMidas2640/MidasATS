package com.midas.consulting.controller.v1.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.midas.consulting.util.DateUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Generic API Response wrapper for consistent response structure across all endpoints
 * 
 * @author Dheeraj Singh
 * @param <T> Type of the payload data
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response<T> {

    private Status status;
    private T payload;
    private Object errors;
    private Object metadata;
    private String message;
    private String timestamp;
    private String requestId;

    // ============================================================================
    // EXISTING STATIC FACTORY METHODS
    // ============================================================================

    public static <T> Response<T> badRequest() {
        Response<T> response = new Response<>();
        response.setStatus(Status.BAD_REQUEST);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> ok() {
        Response<T> response = new Response<>();
        response.setStatus(Status.OK);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> unauthorized() {
        Response<T> response = new Response<>();
        response.setStatus(Status.UNAUTHORIZED);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> validationException() {
        Response<T> response = new Response<>();
        response.setStatus(Status.VALIDATION_EXCEPTION);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> wrongCredentials() {
        Response<T> response = new Response<>();
        response.setStatus(Status.WRONG_CREDENTIALS);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> accessDenied() {
        Response<T> response = new Response<>();
        response.setStatus(Status.ACCESS_DENIED);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> exception() {
        Response<T> response = new Response<>();
        response.setStatus(Status.EXCEPTION);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> notFound() {
        Response<T> response = new Response<>();
        response.setStatus(Status.NOT_FOUND);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    public static <T> Response<T> duplicateEntity() {
        Response<T> response = new Response<>();
        response.setStatus(Status.DUPLICATE_ENTITY);
        response.setTimestamp(DateUtils.today().toString());
        return response;
    }

    // ============================================================================
    // ADDITIONAL STATIC FACTORY METHODS
    // ============================================================================

    /**
     * Create a successful response with payload
     */
    public static <T> Response<T> success(T payload) {
        Response<T> response = ok();
        response.setPayload(payload);
        return response;
    }

    /**
     * Create a successful response with payload and message
     */
    public static <T> Response<T> success(T payload, String message) {
        Response<T> response = success(payload);
        response.setMessage(message);
        return response;
    }

    /**
     * Create a created (201) response
     */
    public static <T> Response<T> created() {
        Response<T> response = new Response<>();
        response.setStatus(Status.CREATED);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create a created response with payload
     */
    public static <T> Response<T> created(T payload) {
        Response<T> response = created();
        response.setPayload(payload);
        return response;
    }

    /**
     * Create a created response with payload and message
     */
    public static <T> Response<T> created(T payload, String message) {
        Response<T> response = created(payload);
        response.setMessage(message);
        return response;
    }

    /**
     * Create an accepted (202) response
     */
    public static <T> Response<T> accepted() {
        Response<T> response = new Response<>();
        response.setStatus(Status.ACCEPTED);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create an accepted response with message
     */
    public static <T> Response<T> accepted(String message) {
        Response<T> response = accepted();
        response.setMessage(message);
        return response;
    }

    /**
     * Create a no content (204) response
     */
    public static <T> Response<T> noContent() {
        Response<T> response = new Response<>();
        response.setStatus(Status.NO_CONTENT);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create a forbidden (403) response
     */
    public static <T> Response<T> forbidden() {
        Response<T> response = new Response<>();
        response.setStatus(Status.FORBIDDEN);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create a forbidden response with message
     */
    public static <T> Response<T> forbidden(String message) {
        Response<T> response = forbidden();
        response.setMessage(message);
        return response;
    }

    /**
     * Create a conflict (409) response
     */
    public static <T> Response<T> conflict() {
        Response<T> response = new Response<>();
        response.setStatus(Status.CONFLICT);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create a conflict response with message
     */
    public static <T> Response<T> conflict(String message) {
        Response<T> response = conflict();
        response.setMessage(message);
        return response;
    }

    /**
     * Create an internal server error (500) response
     */
    public static <T> Response<T> internalServerError() {
        Response<T> response = new Response<>();
        response.setStatus(Status.INTERNAL_SERVER_ERROR);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create an internal server error response with message
     */
    public static <T> Response<T> internalServerError(String message) {
        Response<T> response = internalServerError();
        response.setMessage(message);
        return response;
    }

    /**
     * Create a service unavailable (503) response
     */
    public static <T> Response<T> serviceUnavailable() {
        Response<T> response = new Response<>();
        response.setStatus(Status.SERVICE_UNAVAILABLE);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create a service unavailable response with message
     */
    public static <T> Response<T> serviceUnavailable(String message) {
        Response<T> response = serviceUnavailable();
        response.setMessage(message);
        return response;
    }

    /**
     * Create a too many requests (429) response
     */
    public static <T> Response<T> tooManyRequests() {
        Response<T> response = new Response<>();
        response.setStatus(Status.TOO_MANY_REQUESTS);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create a too many requests response with message
     */
    public static <T> Response<T> tooManyRequests(String message) {
        Response<T> response = tooManyRequests();
        response.setMessage(message);
        return response;
    }

    /**
     * Create an unprocessable entity (422) response
     */
    public static <T> Response<T> unprocessableEntity() {
        Response<T> response = new Response<>();
        response.setStatus(Status.UNPROCESSABLE_ENTITY);
        response.setTimestamp(String.valueOf(DateUtils.today()));
        return response;
    }

    /**
     * Create an unprocessable entity response with message
     */
    public static <T> Response<T> unprocessableEntity(String message) {
        Response<T> response = unprocessableEntity();
        response.setMessage(message);
        return response;
    }

    // ============================================================================
    // FLUENT BUILDER METHODS
    // ============================================================================

    /**
     * Set the payload and return this instance for method chaining
     */
    public Response<T> setPayload(T payload) {
        this.payload = payload;
        return this;
    }

    /**
     * Set the message and return this instance for method chaining
     */
    public Response<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    /**
     * Set the errors and return this instance for method chaining
     */
    public Response<T> setErrors(Object errors) {
        this.errors = errors;
        return this;
    }

    /**
     * Set the metadata and return this instance for method chaining
     */
    public Response<T> setMetadata(Object metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Set the request ID and return this instance for method chaining
     */
    public Response<T> setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * Build the final response
     */
    public Response<T> build() {
        if (this.timestamp == null) {
            this.timestamp = String.valueOf(DateUtils.today());
        }
        return this;
    }

    // ============================================================================
    // PAGINATION HELPER METHODS
    // ============================================================================

    /**
     * Create a paginated response from Spring Data Page
     */
    public static <T> Response<List<T>> paginated(Page<T> page) {
        Response<List<T>> response = success(page.getContent());
        response.setMetadata(new PageMetadata(
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.hasNext(),
                page.hasPrevious(),
                page.isFirst(),
                page.isLast()
        ));
        return response;
    }

    /**
     * Create a paginated response with custom message
     */
    public static <T> Response<List<T>> paginated(Page<T> page, String message) {
        Response<List<T>> response = paginated(page);
        response.setMessage(message);
        return response;
    }

    // ============================================================================
    // ERROR HANDLING METHODS
    // ============================================================================

    /**
     * Add error message to response with exception details
     */
    public Response<T> addErrorMsgToResponse(String errorMsg, Exception ex) {
        ResponseError error = new ResponseError()
                .setDetails(errorMsg)
                .setMessage(ex.getMessage())
                .setTimestamp(String.valueOf(DateUtils.today()))
                .setException(ex.getClass().getSimpleName());
        setErrors(error);
        return this;
    }

    /**
     * Add simple error message to response
     */
    public Response<T> addErrorMsgToResponse(String errorMsg) {
        ResponseError error = new ResponseError()
                .setDetails(errorMsg)
                .setMessage(errorMsg)
                .setTimestamp(String.valueOf(DateUtils.today()));
        setErrors(error);
        return this;
    }

    /**
     * Add validation errors to response
     */
    public Response<T> addValidationErrors(Map<String, String> fieldErrors) {
        ValidationError validationError = new ValidationError()
                .setMessage("Validation failed")
                .setFieldErrors(fieldErrors)
                .setTimestamp(String.valueOf(DateUtils.today()));
        setErrors(validationError);
        return this;
    }

    /**
     * Add validation errors with custom message
     */
    public Response<T> addValidationErrors(String message, Map<String, String> fieldErrors) {
        ValidationError validationError = new ValidationError()
                .setMessage(message)
                .setFieldErrors(fieldErrors)
                .setTimestamp(String.valueOf(DateUtils.today()));
        setErrors(validationError);
        return this;
    }

    // ============================================================================
    // UTILITY METHODS
    // ============================================================================

    /**
     * Check if the response represents a successful operation
     */
    public boolean isSuccessful() {
        return status != null && (
                status == Status.OK || 
                status == Status.CREATED || 
                status == Status.ACCEPTED || 
                status == Status.NO_CONTENT
        );
    }

    /**
     * Check if the response has errors
     */
    public boolean hasErrors() {
        return errors != null;
    }

    /**
     * Check if the response has payload
     */
    public boolean hasPayload() {
        return payload != null;
    }

    /**
     * Get a simple summary of the response
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Status: ").append(status);
        if (message != null) {
            summary.append(", Message: ").append(message);
        }
        if (hasPayload()) {
            summary.append(", Has Payload: true");
        }
        if (hasErrors()) {
            summary.append(", Has Errors: true");
        }
        return summary.toString();
    }

    // ============================================================================
    // ENUMS AND INNER CLASSES
    // ============================================================================

    public enum Status {
        // Success responses (2xx)
        OK, CREATED, ACCEPTED, NO_CONTENT,
        
        // Client error responses (4xx)
        BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, CONFLICT, 
        UNPROCESSABLE_ENTITY, TOO_MANY_REQUESTS,
        
        // Application specific errors
        VALIDATION_EXCEPTION, WRONG_CREDENTIALS, ACCESS_DENIED, DUPLICATE_ENTITY,
        
        // Server error responses (5xx)
        INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, EXCEPTION
    }

    /**
     * Enhanced page metadata with additional pagination info
     */
    @Getter
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PageMetadata {
        private final int size;
        private final long totalElements;
        private final int totalPages;
        private final int number;
        private final boolean hasNext;
        private final boolean hasPrevious;
        private final boolean isFirst;
        private final boolean isLast;

        public PageMetadata(int size, long totalElements, int totalPages, int number,
                           boolean hasNext, boolean hasPrevious, boolean isFirst, boolean isLast) {
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.number = number;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
            this.isFirst = isFirst;
            this.isLast = isLast;
        }

        // Backward compatibility constructor
        public PageMetadata(int size, long totalElements, int totalPages, int number) {
            this(size, totalElements, totalPages, number, false, false, false, false);
        }
    }

    /**
     * Enhanced response error with additional details
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResponseError {
        private String message;
        private String details;
        private String timestamp;
        private String exception;
        private String code;
        private String path;

        public ResponseError() {
            this.timestamp = DateUtils.today().toString();
        }
    }

    /**
     * Validation error with field-specific error messages
     */
    @Getter
    @Setter
    @Accessors(chain = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValidationError {
        private String message;
        private Map<String, String> fieldErrors;
        private String timestamp;
        private int errorCount;

        public ValidationError() {
            this.timestamp = DateUtils.today().toString();
        }

        public ValidationError setFieldErrors(Map<String, String> fieldErrors) {
            this.fieldErrors = fieldErrors;
            this.errorCount = fieldErrors != null ? fieldErrors.size() : 0;
            return this;
        }
    }
}