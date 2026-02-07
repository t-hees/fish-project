package com.tadeo.fish_project.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static ResponseEntity<ApiError> buildError(String code, String message, HttpServletRequest request, HttpStatus status) {
        ApiError error = new ApiError(
            code,
            message,
            Instant.now(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(UserCredentialsException.class)
    public ResponseEntity<ApiError> handleUserAuthException(UserCredentialsException ex, HttpServletRequest request) {
        return buildError("AUTH_FAIL", ex.getMessage(), request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        return buildError("ENTITY_NOT_FOUND", ex.getMessage(), request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception", ex);
        return buildError("INTERNAL_ERROR", "An unexpected error occurred",
            request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
