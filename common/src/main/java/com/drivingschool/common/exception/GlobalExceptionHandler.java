package com.drivingschool.common.exception;

import com.drivingschool.common.dto.ApiResult;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Central exception-to-HTTP mapping for all services that include this module.
 *
 * <p>Responses use {@link ApiResult} with {@code success=false}, a human-readable {@code message},
 * and a stable machine-readable {@code errorCode}. Typical mappings:
 *
 * <table border="1" summary="Exception handling">
 * <tr><th>Source</th><th>HTTP</th><th>{@code errorCode}</th></tr>
 * <tr><td>{@link ResourceNotFoundException}</td><td>404</td><td>{@link ErrorCode#RESOURCE_NOT_FOUND}</td></tr>
 * <tr><td>{@link BusinessException}</td><td>from {@link ErrorCode#getHttpStatus()}</td><td>{@link BusinessException#getErrorCode()}</td></tr>
 * <tr><td>{@link MethodArgumentNotValidException}</td><td>400</td><td>{@link ErrorCode#VALIDATION_FAILED}</td></tr>
 * <tr><td>{@link ConstraintViolationException}</td><td>400</td><td>{@link ErrorCode#CONSTRAINT_VIOLATION}</td></tr>
 * <tr><td>any other {@link Exception}</td><td>500</td><td>{@link ErrorCode#INTERNAL_ERROR}</td></tr>
 * </table>
 *
 * <p>Operation-specific domain rules are expressed by throwing {@link BusinessException} with the appropriate
 * {@link ErrorCode} (e.g. payment refund rules, scheduling conflicts); this class does not branch on operation type.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResult<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ErrorCode.RESOURCE_NOT_FOUND.getCode()));
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
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });
        String errorMessages = String.join(", ", errors.values());
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(ApiResult.error(errorMessages, ErrorCode.VALIDATION_FAILED.getCode()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ErrorCode.CONSTRAINT_VIOLATION.getCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Object>> handleGenericException(@NonNull Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResult.error("An unexpected error occurred", ErrorCode.INTERNAL_ERROR.getCode()));
    }
}
