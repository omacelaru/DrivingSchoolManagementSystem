package com.drivingschool.gateway.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GatewayGlobalExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResult<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found in gateway: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ErrorCode.RESOURCE_NOT_FOUND.getCode()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResult<Object>> handleBusinessException(BusinessException ex) {
        HttpStatus status = ex.getHttpStatus();
        if (status.is5xxServerError()) {
            log.error("Business exception in gateway: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        } else {
            log.warn("Business exception in gateway: code={}, message={}", ex.getErrorCode(), ex.getMessage());
        }
        return ResponseEntity.status(status)
                .body(ApiResult.error(ex.getMessage(), ex.getErrorCode()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResult<Map<String, String>>> handleWebExchangeBindException(WebExchangeBindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError fieldError) {
                errors.put(fieldError.getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        });
        String message = String.join(", ", errors.values());
        log.warn("Gateway request validation failed: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error(message, ErrorCode.VALIDATION_FAILED.getCode()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Gateway constraint violation: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus())
                .body(ApiResult.error(ex.getMessage(), ErrorCode.CONSTRAINT_VIOLATION.getCode()));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ApiResult<Object>> handleServerWebInputException(ServerWebInputException ex) {
        log.warn("Malformed or invalid gateway request body: {}", ex.getReason());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResult.error("Malformed JSON request body", ErrorCode.VALIDATION_FAILED.getCode()));
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResult<Object>> handleWebClientResponseException(WebClientResponseException ex) {
        HttpStatus downstreamStatus = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("Downstream call failed with status {}: {}", downstreamStatus.value(), ex.getMessage());
        if (downstreamStatus == HttpStatus.UNAUTHORIZED || downstreamStatus == HttpStatus.FORBIDDEN) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(ApiResult.error(
                            "Internal service authorization failed while processing request",
                            ErrorCode.INTERNAL_ERROR.getCode()
                    ));
        }
        ApiResult<Object> downstreamApiError = extractApiResultError(ex);
        if (downstreamApiError != null) {
            return ResponseEntity.status(downstreamStatus).body(downstreamApiError);
        }
        return ResponseEntity.status(downstreamStatus)
                .body(ApiResult.error("Downstream service error", ErrorCode.INTERNAL_ERROR.getCode()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResult<Object>> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("Gateway response status exception [{}]: {}", status.value(), ex.getReason());
        return ResponseEntity.status(status)
                .body(ApiResult.error(
                        ex.getReason() != null ? ex.getReason() : "Request could not be processed",
                        ErrorCode.INTERNAL_ERROR.getCode()
                ));
    }

    private ApiResult<Object> extractApiResultError(WebClientResponseException ex) {
        try {
            JsonNode root = objectMapper.readTree(ex.getResponseBodyAsString());
            String message = root.path("message").asText(null);
            String errorCode = root.path("errorCode").asText(null);
            boolean hasApiResultShape = root.has("success") || message != null || errorCode != null;
            if (!hasApiResultShape) {
                return null;
            }
            return ApiResult.error(
                    message != null ? message : "Downstream service error",
                    errorCode != null ? errorCode : ErrorCode.INTERNAL_ERROR.getCode()
            );
        } catch (Exception parsingError) {
            return null;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Object>> handleGenericException(Exception ex) {
        log.error("Unhandled gateway exception", ex);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ApiResult.error("An unexpected error occurred", ErrorCode.INTERNAL_ERROR.getCode()));
    }
}