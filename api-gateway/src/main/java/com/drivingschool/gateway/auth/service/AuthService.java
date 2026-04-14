package com.drivingschool.gateway.auth.service;

import com.drivingschool.gateway.auth.dto.LoginRequest;
import com.drivingschool.gateway.auth.dto.LoginResponse;
import com.drivingschool.gateway.auth.dto.admin.RegisterAdminRequest;
import com.drivingschool.gateway.auth.dto.instructor.RegisterInstructorRequest;
import com.drivingschool.gateway.auth.dto.RegisterResponse;
import com.drivingschool.gateway.auth.dto.student.RegisterStudentRequest;
import com.drivingschool.gateway.auth.entity.AppRole;
import com.drivingschool.gateway.auth.entity.AppUser;
import com.drivingschool.gateway.auth.entity.ProfileType;
import com.drivingschool.gateway.auth.entity.RoleName;
import com.drivingschool.gateway.auth.repository.AppRoleRepository;
import com.drivingschool.gateway.auth.repository.AppUserRepository;
import com.drivingschool.gateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final ProfileProvisioningService profileProvisioningService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public Mono<RegisterResponse> registerStudent(RegisterStudentRequest request) {
        return Mono.fromCallable(() -> {
                    validateUniqueUsername(request.username());
                    return request;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(req -> profileProvisioningService.createStudentProfile(req.studentProfile()))
                .flatMap(profileId -> Mono.fromCallable(() ->
                                registerWithRole(request.username(), request.password(), RoleName.ROLE_STUDENT, profileId))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<RegisterResponse> registerInstructor(RegisterInstructorRequest request) {
        return Mono.fromCallable(() -> {
                    validateUniqueUsername(request.username());
                    return request;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(req -> profileProvisioningService.createInstructorProfile(req.instructorProfile()))
                .flatMap(profileId -> Mono.fromCallable(() ->
                                registerWithRole(request.username(), request.password(), RoleName.ROLE_INSTRUCTOR, profileId))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public RegisterResponse registerAdmin(RegisterAdminRequest request) {
        validateUniqueUsername(request.username());
        return registerWithRole(request.username(), request.password(), RoleName.ROLE_ADMIN, null);
    }

    private RegisterResponse registerWithRole(String username, String rawPassword, RoleName roleName, Long profileId) {
        AppRole role = appRoleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + roleName));

        AppUser user = new AppUser();
        user.setUsername(username.trim());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setEnabled(true);
        user.getRoles().add(role);
        applyProfileLink(user, roleName, profileId);

        AppUser saved = appUserRepository.save(user);
        return new RegisterResponse(
                saved.getId(),
                saved.getUsername(),
                roleName.name(),
                saved.getProfileType() != null ? saved.getProfileType().name() : null,
                saved.getProfileId()
        );
    }

    private void validateUniqueUsername(String username) {
        if (appUserRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }
    }

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

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getAccessTokenTtlSeconds(),
                user.getUsername(),
                roles,
                user.getProfileType() != null ? user.getProfileType().name() : null,
                user.getProfileId()
        );
    }

    private void applyProfileLink(AppUser user, RoleName roleName, Long profileId) {
        switch (roleName) {
            case ROLE_STUDENT -> {
                user.setProfileType(ProfileType.STUDENT);
                user.setProfileId(profileId);
            }
            case ROLE_INSTRUCTOR -> {
                user.setProfileType(ProfileType.INSTRUCTOR);
                user.setProfileId(profileId);
            }
            case ROLE_ADMIN -> {
                user.setProfileType(ProfileType.ADMIN);
                user.setProfileId(null);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported role");
        }
    }
}

