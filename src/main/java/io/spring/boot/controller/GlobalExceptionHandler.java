package io.spring.boot.controller;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.spring.boot.controller.wrappers.ErrorsBodyResponse;
import io.spring.boot.dto.ErrorsResponse;


@RestControllerAdvice
public class GlobalExceptionHandler {

	// Helper to reduce duplication
    private ResponseEntity<ErrorsResponse> errorStatus(HttpStatus status, String... messages) {
        var response = new ErrorsResponse(new ErrorsBodyResponse(List.of(messages)));
        return ResponseEntity.status(status).body(response);
    }

    // 422 Unprocessable Entity — validation / business rules
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorsResponse> handleValidationError(IllegalArgumentException e) {
        return errorStatus(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    // 401 Unauthorized — bad login, missing/invalid token
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorsResponse> handleAuthenticationError(AuthenticationException e) {
        return errorStatus(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    // 403 Forbidden — logged in, but not allowed (e.g. edit/delete other's article)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorsResponse> handleAccessDenied(AccessDeniedException e) {
        return errorStatus(HttpStatus.FORBIDDEN, "Access denied");
    }

    // 404 Not Found — user, article, comment not found
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorsResponse> handleNotFound(NoSuchElementException e) {
        return errorStatus(HttpStatus.NOT_FOUND, "Resource not found");
    }
    
 // Optional: Catch-all for 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorsResponse> handleUnexpectedError(Exception e) {
        e.printStackTrace();
        return errorStatus(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
