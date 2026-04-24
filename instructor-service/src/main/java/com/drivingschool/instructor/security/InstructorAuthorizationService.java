package com.drivingschool.instructor.security;

import com.drivingschool.common.security.ProfileType;
import com.drivingschool.common.security.RoleName;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("instructorAuthz")
public class InstructorAuthorizationService {
    public boolean isInstructor(Authentication authentication) {
        return hasRole(authentication, RoleName.ROLE_INSTRUCTOR)
                && ProfileType.INSTRUCTOR.name().equals(claim(authentication, "profileType"));
    }

    public boolean isAdminOrService(Authentication authentication) {
        return hasRole(authentication, RoleName.ROLE_ADMIN) || hasRole(authentication, RoleName.ROLE_SERVICE);
    }

    public Long profileId(Authentication authentication) {
        Jwt jwt = jwt(authentication);
        if (jwt == null) {
            return null;
        }
        Number profileId = jwt.getClaim("profileId");
        return profileId != null ? profileId.longValue() : null;
    }

    private boolean hasRole(Authentication authentication, RoleName role) {
        Jwt jwt = jwt(authentication);
        if (jwt == null) {
            return false;
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(role.name());
    }

    private String claim(Authentication authentication, String key) {
        Jwt jwt = jwt(authentication);
        return jwt != null ? jwt.getClaimAsString(key) : null;
    }

    private Jwt jwt(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return null;
        }
        return jwt;
    }
}
