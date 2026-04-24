package com.drivingschool.gateway.auth.security;

import com.drivingschool.common.security.ProfileType;
import com.drivingschool.common.security.RoleName;
import com.drivingschool.gateway.auth.entity.AppRole;
import com.drivingschool.gateway.auth.entity.AppUser;
import com.drivingschool.gateway.auth.repository.AppRoleRepository;
import com.drivingschool.gateway.auth.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapInitializer implements ApplicationRunner {
    private final AdminBootstrapProperties properties;
    private final AppUserRepository appUserRepository;
    private final AppRoleRepository appRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }
        if (appUserRepository.existsByRoles_Name(RoleName.ROLE_ADMIN)) {
            log.info("Admin bootstrap skipped: at least one admin account already exists.");
            return;
        }

        String normalizedEmail = normalizeEmail(properties.getEmail());
        String rawPassword = properties.getPassword() != null ? properties.getPassword().trim() : "";
        validateBootstrapInput(normalizedEmail, rawPassword);

        AppRole adminRole = appRoleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException("Admin role not found. Ensure role initializer ran first."));

        AppUser admin = new AppUser();
        admin.setEmail(normalizedEmail);
        admin.setUsername(buildUsernameFromEmail(normalizedEmail));
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setEnabled(true);
        admin.setProfileType(ProfileType.ADMIN);
        admin.setProfileId(null);
        admin.getRoles().add(adminRole);

        appUserRepository.save(admin);
        log.info("Bootstrap admin account created successfully. Disable app.bootstrap.admin.enabled after first run.");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private void validateBootstrapInput(String email, String password) {
        if (email.isBlank() || !email.contains("@")) {
            throw new IllegalStateException("Invalid bootstrap admin email. Set BOOTSTRAP_ADMIN_EMAIL correctly.");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("Bootstrap admin password must be at least 12 characters.");
        }
        if (!password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-z0-9].*")) {
            throw new IllegalStateException("Bootstrap admin password must include upper, lower, digit, and special char.");
        }
    }

    private String buildUsernameFromEmail(String email) {
        String localPart = email.split("@")[0];
        String base = localPart.replaceAll("[^a-z0-9._-]", "").trim();
        if (base.isEmpty()) {
            base = "admin";
        }
        String candidate = base;
        int suffix = 1;
        while (appUserRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = base + "_" + suffix++;
        }
        return candidate;
    }
}
