package com.example.movies_api.exceptions;


import jakarta.validation.ConstraintViolationException;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice // Indicates that this class will handle exceptions globally for all controllers
public class GlobalExceptionHandler {

    // Handles ResourceNotFoundException and returns a 404 NOT FOUND response
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleActorNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());// Add the exception message to the response
        errorResponse.put("timestamp", String.valueOf(Instant.now()));// Add the current timestamp
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);// Return the error response with NOT FOUND status
    }

    // Handles BadRequestException and returns a 400 BAD REQUEST response
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", ex.getMessage());// Add the exception message to the response
        errorDetails.put("timestamp", String.valueOf(Instant.now()));// Add the current timestamp
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);// Return the error details with BAD REQUEST status
    }

    // Handles validation exceptions when method arguments are not valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        // Iterate through field errors and add them to the response
        ex.getBindingResult().getFieldErrors().forEach((error) -> {
            String fieldName = error.getField();// Get the field name
            String errorMessage = error.getDefaultMessage(); // Get the error message
            errors.put(fieldName, errorMessage); // Store the field error in the map
        });
        // Iterate through global errors and add them to the response
        ex.getBindingResult().getGlobalErrors().forEach((error) -> {
            String fieldName = error.getObjectName(); // Get the object name
            String errorMessage = error.getDefaultMessage();// Get the error message
            errors.put(fieldName, errorMessage);// Store the global error in the map
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);// Return the errors with BAD REQUEST status
    }

    // Handles constraint violation exceptions (e.g., validation errors)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString(); // Get the field name
            String errorMessage = violation.getMessage(); // Get the error message
            errors.put(fieldName, errorMessage);// Store the violation in the map
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);// Return the errors with BAD REQUEST status
    }

    // Handles cases where the HTTP message is not readable (e.g., invalid JSON format)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String errorMessage = "Invalid date format (must by: YYYY-MM-DD): " + ex.getMostSpecificCause().getMessage();
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);// Return the error message with BAD REQUEST status
    }
}