package com.drivingschool.gateway.auth.dto;

public record RegisterResponse(
        Long userId,
        String username,
        String role,
        String profileType,
        Long profileId
) {
}

