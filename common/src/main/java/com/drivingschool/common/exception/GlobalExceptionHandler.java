package com.drivingschool.common.exception;

import com.drivingschool.common.dto.ApiResult;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResult<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResult.error(ex.getMessage(), "RESOURCE_NOT_FOUND"));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        String errorMessages = String.join(", ", errors.values());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error(errorMessages, "VALIDATION_FAILED"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error(ex.getMessage(), "CONSTRAINT_VIOLATION"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Object>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResult.error(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred", "INTERNAL_ERROR"));
    }
}

