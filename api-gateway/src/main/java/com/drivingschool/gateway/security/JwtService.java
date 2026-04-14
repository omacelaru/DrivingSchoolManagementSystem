package com.drivingschool.gateway.security;

import com.drivingschool.gateway.auth.entity.AppRole;
import com.drivingschool.gateway.auth.entity.AppUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JwtService {

    private final SecretKey secretKey;
    @Getter
    private final String issuer;
    private final long accessTokenMinutes;

    public JwtService(
            @Value("${app.security.jwt.secret}") String jwtSecret,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${app.security.jwt.access-token-minutes}") long accessTokenMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public String generateAccessToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenMinutes * 60);
        Set<String> roles = user.getRoles().stream()
                .map(AppRole::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());

        return Jwts.builder()
                .subject(user.getUsername())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("roles", roles)
                .claim("profileType", user.getProfileType() != null ? user.getProfileType().name() : null)
                .claim("profileId", user.getProfileId())
                .signWith(secretKey)
                .compact();
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenMinutes * 60;
    }

    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }

}

