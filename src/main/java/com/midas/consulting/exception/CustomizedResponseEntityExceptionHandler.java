package com.midas.consulting.exception;

import com.midas.consulting.controller.v1.response.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Created by Dheeraj Singh.
 */
@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(MidasCustomException.EntityNotFoundException.class)
    public final ResponseEntity handleNotFountExceptions(Exception ex, WebRequest request) {
        Response response = Response.notFound();
        response.addErrorMsgToResponse(ex.getMessage(), ex);
        return new ResponseEntity(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DatabaseConnectionException.class)
    public final ResponseEntity handleDatabaseConnectionException(DatabaseConnectionException ex, WebRequest request) {
        Response response = Response.exception();
        response.addErrorMsgToResponse("Database connection error for tenant: " + ex.getTenantId(), ex);
        return new ResponseEntity(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(MidasCustomException.DuplicateEntityException.class)
    public final ResponseEntity handleNotFountExceptions1(Exception ex, WebRequest request) {
        Response response = Response.duplicateEntity();
        response.addErrorMsgToResponse(ex.getMessage(), ex);
        return new ResponseEntity(response, HttpStatus.CONFLICT);
    }
}
