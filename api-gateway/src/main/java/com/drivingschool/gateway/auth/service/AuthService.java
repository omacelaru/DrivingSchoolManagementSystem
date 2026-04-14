package com.drivingschool.gateway.auth.service;

import com.drivingschool.gateway.auth.dto.LoginRequest;
import com.drivingschool.gateway.auth.dto.LoginResponse;
import com.drivingschool.gateway.auth.entity.AppRole;
import com.drivingschool.gateway.auth.entity.AppUser;
import com.drivingschool.gateway.auth.repository.AppUserRepository;
import com.drivingschool.gateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsernameIgnoreCase(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtService.generateAccessToken(user);
        Set<String> roles = user.getRoles().stream()
                .map(AppRole::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new LoginResponse(token, "Bearer", jwtService.getAccessTokenTtlSeconds(), user.getUsername(), roles);
    }
}

