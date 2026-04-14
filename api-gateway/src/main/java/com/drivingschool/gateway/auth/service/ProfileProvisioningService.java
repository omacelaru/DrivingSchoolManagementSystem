package com.drivingschool.gateway.auth.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.gateway.auth.dto.instructor.RegisterInstructorProfilePayload;
import com.drivingschool.gateway.auth.dto.student.RegisterStudentProfilePayload;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProfileProvisioningService {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.services.student-base-url}")
    private String studentServiceBaseUrl;

    @Value("${app.services.instructor-base-url}")
    private String instructorServiceBaseUrl;

    public Long createStudentProfile(RegisterStudentProfilePayload payload) {
        ApiResult<Map<String, Object>> result = webClientBuilder.build()
                .post()
                .uri(studentServiceBaseUrl + "/api/students")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResult<Map<String, Object>>>() {
                })
                .block();

        if (result == null || !result.success() || result.data() == null || result.data().get("id") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create student profile");
        }
        return ((Number) result.data().get("id")).longValue();
    }

    public Long createInstructorProfile(RegisterInstructorProfilePayload payload) {
        ApiResult<Map<String, Object>> result = webClientBuilder.build()
                .post()
                .uri(instructorServiceBaseUrl + "/api/instructors")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResult<Map<String, Object>>>() {
                })
                .block();

        if (result == null || !result.success() || result.data() == null || result.data().get("id") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to create instructor profile");
        }
        return ((Number) result.data().get("id")).longValue();
    }
}

