package com.drivingschool.gateway.auth.repository;

import com.drivingschool.common.security.RoleName;
import com.drivingschool.gateway.auth.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByRoles_Name(RoleName roleName);
}

