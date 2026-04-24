package com.drivingschool.gateway.auth.security;

import com.drivingschool.gateway.auth.entity.AppRole;
import com.drivingschool.common.security.RoleName;
import com.drivingschool.gateway.auth.repository.AppRoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class AuthRoleDataInitializer implements ApplicationRunner {

    private final AppRoleRepository appRoleRepository;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        if (appRoleRepository.count() > 0) {
            return;
        }

        appRoleRepository.saveAll(
                Arrays.stream(RoleName.values())
                        .map(roleName -> {
                            AppRole role = new AppRole();
                            role.setName(roleName);
                            return role;
                        })
                        .toList()
        );
    }
}
