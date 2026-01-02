package com.drivingschool.instructor.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@FeignClient(name = "scheduling-service", url = "${scheduling.service.url}")
public interface SchedulingClient {
    
    /**
     * Checks if an instructor has conflicting lessons for a given time slot.
     * 
     * @param instructorId instructor ID
     * @param startTime lesson start time
     * @param endTime lesson end time
     * @return true if instructor is available (no conflicts), false otherwise
     */
    @GetMapping("/api/lessons/instructors/{instructorId}/available")
    Boolean isInstructorAvailable(
            @PathVariable Long instructorId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime
    );
}

