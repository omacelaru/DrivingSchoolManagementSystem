package com.drivingschool.gateway.auth.repository;

import com.drivingschool.gateway.auth.entity.AppRole;
import com.drivingschool.gateway.auth.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByName(RoleName name);
}

