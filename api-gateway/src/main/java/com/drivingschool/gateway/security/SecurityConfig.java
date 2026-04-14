package com.drivingschool.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.SecurityWebFilterChain;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JwtService jwtService) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .securityContextRepository(org.springframework.security.web.server.context.NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((exchange, e) -> Mono.fromRunnable(() -> {
                            log.warn(
                                    "401 Unauthorized on {} {}: {}",
                                    exchange.getRequest().getMethod(),
                                    exchange.getRequest().getPath(),
                                    e.getMessage()
                            );
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        }))
                        .accessDeniedHandler((exchange, e) -> Mono.fromRunnable(() -> {
                            log.warn(
                                    "403 Forbidden on {} {}: {}",
                                    exchange.getRequest().getMethod(),
                                    exchange.getRequest().getPath(),
                                    e.getMessage()
                            );
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        })))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/register/admin").hasRole("ADMIN")
                        .pathMatchers("/auth/login", "/auth/logout", "/auth/register/student", "/auth/register/instructor")
                        .permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**", "/actuator/health")
                        .permitAll()
                        .pathMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST, "/api/instructors/**", "/api/vehicles/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers("/api/instructors/**").hasAnyRole("INSTRUCTOR", "ADMIN")
                        .pathMatchers("/api/students/**").hasAnyRole("STUDENT", "ADMIN")
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtService.jwtDecoder())
                                .jwtAuthenticationConverter(this::toAuthentication)))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private Mono<JwtAuthenticationToken> toAuthentication(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object claim = jwt.getClaims().get("roles");
        if (!(claim instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream()
                .map(String::valueOf)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}

