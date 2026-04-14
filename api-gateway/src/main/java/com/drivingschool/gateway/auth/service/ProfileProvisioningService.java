package com.drivingschool.gateway.auth.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.gateway.auth.dto.instructor.RegisterInstructorProfilePayload;
import com.drivingschool.gateway.auth.dto.student.RegisterStudentProfilePayload;
import com.drivingschool.gateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileProvisioningService {

    private final WebClient.Builder webClientBuilder;
    private final JwtService jwtService;

    @Value("${app.services.student-base-url}")
    private String studentServiceBaseUrl;

    @Value("${app.services.instructor-base-url}")
    private String instructorServiceBaseUrl;

    public Mono<Long> createStudentProfile(RegisterStudentProfilePayload payload) {
        String serviceToken = jwtService.generateServiceToken();
        return webClientBuilder.build()
                .post()
                .uri(studentServiceBaseUrl + "/api/students")
                .headers(headers -> headers.setBearerAuth(serviceToken))
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResult<Map<String, Object>>>() {
                })
                .map(this::extractProfileIdOrThrow);
    }

    public Mono<Long> createInstructorProfile(RegisterInstructorProfilePayload payload) {
        String serviceToken = jwtService.generateServiceToken();
        return webClientBuilder.build()
                .post()
                .uri(instructorServiceBaseUrl + "/api/instructors")
                .headers(headers -> headers.setBearerAuth(serviceToken))
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResult<Map<String, Object>>>() {
                })
                .map(this::extractProfileIdOrThrow);
    }

    private Long extractProfileIdOrThrow(ApiResult<Map<String, Object>> result) {
        if (result == null || !result.success() || result.data() == null || result.data().get("id") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create profile");
        }
        return ((Number) result.data().get("id")).longValue();
    }
}

