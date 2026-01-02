package com.drivingschool.scheduling.client;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.InstructorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "instructor-service", url = "http://localhost:8086")
public interface InstructorClient {
    @GetMapping("/api/instructors/{id}")
    ApiResult<InstructorResponse> getInstructorById(@PathVariable Long id);
}

