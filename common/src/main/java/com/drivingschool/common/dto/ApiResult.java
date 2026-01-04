package com.drivingschool.common.dto;

import java.time.LocalDateTime;

public record ApiResult<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp,
    String errorCode
) {
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(
                true,
                null,
                data,
                LocalDateTime.now(),
                null
        );
    }

    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(
                true,
                message,
                data,
                LocalDateTime.now(),
                null
        );
    }

    public static <T> ApiResult<T> error(String message, String errorCode) {
        return new ApiResult<>(
                false,
                message,
                null,
                LocalDateTime.now(),
                errorCode
        );
    }
}

