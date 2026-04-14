package com.drivingschool.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Objects;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode kind;

    public BusinessException(String message) {
        this(message, ErrorCode.BUSINESS_ERROR);
    }

    public BusinessException(String message, ErrorCode kind) {
        super(message);
        this.kind = Objects.requireNonNull(kind);
    }

    /**
     * Stable string code for API payloads (same as {@link ErrorCode#getCode()}).
     */
    public String getErrorCode() {
        return kind.getCode();
    }

    public HttpStatus getHttpStatus() {
        return kind.getHttpStatus();
    }
}
