package com.tadeo.fish_project.exception;

import java.time.Instant;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
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
        return buildError("AUTH_FAIL", ex.getMessage(), request, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DuplicateUsernameException.class)
    public ResponseEntity<ApiError> handleDuplicateUsernameException(DuplicateUsernameException ex, HttpServletRequest request) {
        return buildError("USERNAME_TAKEN", ex.getMessage(), request, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFoundException(EntityNotFoundException ex, HttpServletRequest request) {
        return buildError("ENTITY_NOT_FOUND", ex.getMessage(), request, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiError> handleInvalidImageException(InvalidImageException ex, HttpServletRequest request) {
        return buildError("UNSUPPORTED_IMAGE", ex.getMessage(), request, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining(", "));
        return buildError("VALIDATION_FAILED", message, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildError("MALFORMED_REQUEST", "Request body is missing or malformed", request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildError("MALFORMED_REQUEST", "Invalid value for " + ex.getName(), request, HttpStatus.BAD_REQUEST);
    }

    // Spring's own errors like unknown paths (404), wrong methods (405), missing parameters (400)
    // or too large uploads (413)
    @ExceptionHandler({NoResourceFoundException.class, HttpRequestMethodNotSupportedException.class,
        HttpMediaTypeNotSupportedException.class, MissingServletRequestParameterException.class,
        MissingServletRequestPartException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ApiError> handleSpringErrorResponse(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = (ErrorResponse) ex;
        HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
        return buildError(status.name(), errorResponse.getBody().getDetail(), request, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception", ex);
        return buildError("INTERNAL_ERROR", "An unexpected error occurred",
            request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
