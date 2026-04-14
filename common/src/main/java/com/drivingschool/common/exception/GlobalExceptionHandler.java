package com.drivingschool.common.exception;

import com.drivingschool.common.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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
    public ResponseEntity<ApiResult<Object>> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ErrorCode.RESOURCE_NOT_FOUND.getCode()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Object>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = ex.getHttpStatus();
        logByStatus(
                status,
                request.getMethod(),
                request.getRequestURI(),
                ex.getErrorCode(),
                ex.getMessage()
        );
        return ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });
        String errorMessages = String.join(", ", errors.values());
        log.warn("Validation failed on {} {}: {}", request.getMethod(), request.getRequestURI(), errorMessages);
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(ApiResult.error(errorMessages, ErrorCode.VALIDATION_FAILED.getCode()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Object>> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        log.warn("Constraint violation on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ErrorCode.CONSTRAINT_VIOLATION.getCode()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        ex.getMostSpecificCause();
        String rootMessage = ex.getMostSpecificCause().getMessage();
        log.warn("Malformed JSON request on {} {}: {}", request.getMethod(), request.getRequestURI(), rootMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error("Malformed JSON request body", ErrorCode.VALIDATION_FAILED.getCode()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResult<Object>> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String parameterName = ex.getName();
        String providedValue = ex.getValue() != null ? String.valueOf(ex.getValue()) : "null";
        String message = "Invalid value '" + providedValue + "' for parameter '" + parameterName + "'";
        log.warn("Request parameter type mismatch on {} {}: {}", request.getMethod(), request.getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error(message, ErrorCode.VALIDATION_FAILED.getCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Object>> handleGenericException(
            @NotNull Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResult.error("An unexpected error occurred", ErrorCode.INTERNAL_ERROR.getCode()));
    }

    private void logByStatus(HttpStatus status, Object... args) {
        if (status.is5xxServerError()) {
            log.error("Business exception on {} {}: code={}, message={}", args);
            return;
        }
        if (status.is4xxClientError()) {
            log.warn("Business exception on {} {}: code={}, message={}", args);
            return;
        }
        log.info("Business exception on {} {}: code={}, message={}", args);
    }
}
