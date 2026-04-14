package com.drivingschool.gateway.auth.dto;

import java.util.Set;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        Set<String> roles,
        String profileType,
        Long profileId
) {
}

