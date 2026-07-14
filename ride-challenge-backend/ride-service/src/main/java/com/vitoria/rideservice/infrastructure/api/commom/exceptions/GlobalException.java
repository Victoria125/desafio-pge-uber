package com.vitoria.rideservice.infrastructure.api.commom.exceptions;

import com.vitoria.rideservice.domain.exceptions.ForbiddenOperationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalException {

    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomErrorResponse> handle(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors()
                .forEach(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errors.put(fieldName, errorMessage);
                });
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse(errors));
    }

    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomErrorResponse> handle(HttpMessageNotReadableException e) {
        Map<String, String> error = new HashMap<>();
        error.put("Malformed request", "Request body is invalid or has invalid values");
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse(error));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<CustomErrorResponse> handle(MissingRequestHeaderException e) {
        Map<String, String> error = new HashMap<>();
        error.put("Unauthorized", "Missing authenticated identity header '" + e.getHeaderName() + "'");
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new CustomErrorResponse(error));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<CustomErrorResponse> handle(ForbiddenOperationException e) {
        Map<String, String> error = new HashMap<>();
        error.put("Forbidden", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new CustomErrorResponse(error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomErrorResponse> handle(IllegalArgumentException e) {
        Map<String, String> error = new HashMap<>();
        error.put("Argument Exception", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new CustomErrorResponse(error));
    }

    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomErrorResponse> handle(IllegalStateException e) {
        Map<String, String> error = new HashMap<>();
        error.put("State Conflict", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new CustomErrorResponse(error));
    }

    
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<CustomErrorResponse> handle(NoSuchElementException e) {
        Map<String, String> error = new HashMap<>();
        error.put("Not Found", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new CustomErrorResponse(error));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CustomErrorResponse> handle(RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("Runtime exception", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new CustomErrorResponse(error));
    }

}
