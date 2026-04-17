package com.drivingschool.gateway.auth.service;

import com.drivingschool.gateway.auth.dto.LoginRequest;
import com.drivingschool.gateway.auth.dto.LoginResponse;
import com.drivingschool.gateway.auth.dto.admin.RegisterAdminRequest;
import com.drivingschool.gateway.auth.dto.instructor.RegisterInstructorProfileInput;
import com.drivingschool.gateway.auth.dto.instructor.RegisterInstructorProfilePayload;
import com.drivingschool.gateway.auth.dto.instructor.RegisterInstructorRequest;
import com.drivingschool.gateway.auth.dto.RegisterResponse;
import com.drivingschool.gateway.auth.dto.student.RegisterStudentProfileInput;
import com.drivingschool.gateway.auth.dto.student.RegisterStudentProfilePayload;
import com.drivingschool.gateway.auth.dto.student.RegisterStudentRequest;
import com.drivingschool.gateway.auth.entity.AppRole;
import com.drivingschool.gateway.auth.entity.AppUser;
import com.drivingschool.gateway.auth.entity.ProfileType;
import com.drivingschool.gateway.auth.entity.RoleName;
import com.drivingschool.gateway.auth.repository.AppRoleRepository;
import com.drivingschool.gateway.auth.repository.AppUserRepository;
import com.drivingschool.gateway.auth.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;
import java.util.UUID;
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
                    validateUniqueEmail(request.email());
                    return request;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(req -> profileProvisioningService.createStudentProfile(
                        toStudentProfilePayload(req.email(), req.studentProfile())))
                .flatMap(profileId -> Mono.fromCallable(() ->
                                registerWithRole(request.email(), request.password(), RoleName.ROLE_STUDENT, profileId))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public Mono<RegisterResponse> registerInstructor(RegisterInstructorRequest request) {
        return Mono.fromCallable(() -> {
                    validateUniqueEmail(request.email());
                    return request;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(req -> profileProvisioningService.createInstructorProfile(
                        toInstructorProfilePayload(req.email(), req.instructorProfile())))
                .flatMap(profileId -> Mono.fromCallable(() ->
                                registerWithRole(request.email(), request.password(), RoleName.ROLE_INSTRUCTOR, profileId))
                        .subscribeOn(Schedulers.boundedElastic()));
    }

    public RegisterResponse registerAdmin(RegisterAdminRequest request) {
        validateUniqueEmail(request.email());
        return registerWithRole(request.email(), request.password(), RoleName.ROLE_ADMIN, null);
    }

    private RegisterResponse registerWithRole(String email, String rawPassword, RoleName roleName, Long profileId) {
        AppRole role = appRoleRepository.findByName(roleName)
                .orElseGet(() -> {
                    AppRole newRole = new AppRole();
                    newRole.setName(roleName);
                    return appRoleRepository.save(newRole);
                });

        String normalizedEmail = normalizeEmail(email);
        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setUsername(generateUniqueUsername(normalizedEmail));
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

    private void validateUniqueEmail(String email) {
        if (appUserRepository.findByEmailIgnoreCase(normalizeEmail(email)).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }
    }

    public LoginResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private RegisterStudentProfilePayload toStudentProfilePayload(String accountEmail, RegisterStudentProfileInput request) {
        return new RegisterStudentProfilePayload(
                request.firstName(),
                request.lastName(),
                request.cnp(),
                normalizeEmail(accountEmail),
                request.phone(),
                request.address(),
                request.profile(),
                request.targetDrivingCategoryCodes()
        );
    }

    private RegisterInstructorProfilePayload toInstructorProfilePayload(String accountEmail, RegisterInstructorProfileInput request) {
        return new RegisterInstructorProfilePayload(
                request.firstName(),
                request.lastName(),
                request.licenseNumber(),
                normalizeEmail(accountEmail),
                request.phone(),
                request.specialization()
        );
    }

    private String generateUniqueUsername(String email) {
        String localPart = email.split("@")[0];
        String base = localPart.replaceAll("[^a-z0-9._-]", "").trim();
        if (base.isEmpty()) {
            base = "user";
        }
        if (base.length() > 80) {
            base = base.substring(0, 80);
        }

        String candidate = base;
        while (appUserRepository.existsByUsernameIgnoreCase(candidate)) {
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            candidate = base + "_" + suffix;
            if (candidate.length() > 120) {
                candidate = candidate.substring(0, 120);
            }
        }
        return candidate;
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

