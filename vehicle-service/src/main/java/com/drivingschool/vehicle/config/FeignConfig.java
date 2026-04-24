package com.drivingschool.vehicle.config;

import com.drivingschool.common.feign.FeignErrorDecoder;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    @Bean
    public JwtEncoder serviceJwtEncoder(@Value("${app.security.jwt.secret}") String secret) {
        SecretKey secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return new NimbusJwtEncoder(new com.nimbusds.jose.jwk.source.ImmutableSecret<>(secretKey));
    }

    @Bean
    public RequestInterceptor serviceAuthInterceptor(
            JwtEncoder serviceJwtEncoder,
            @Value("${app.security.jwt.issuer}") String issuer,
            @Value("${spring.application.name:vehicle-service}") String subject,
            @Value("${app.security.jwt.access-token-minutes:60}") long accessTokenMinutes) {
        return template -> {
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .issuer(issuer)
                    .subject(subject)
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(accessTokenMinutes * 60))
                    .claim("roles", List.of("ROLE_SERVICE"))
                    .build();
            JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
            String token = serviceJwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
            template.header("Authorization", "Bearer " + token);
        };
    }
}

