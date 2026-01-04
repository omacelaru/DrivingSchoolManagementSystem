package com.drivingschool.scheduling.client;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.StudentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "student-service", url = "${student.service.url}")
public interface StudentClient {
    @GetMapping("/api/students/{id}")
    ApiResult<StudentResponse> getStudentById(@PathVariable Long id);
}

