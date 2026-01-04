package com.drivingschool.scheduling.service;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.scheduling.client.InstructorClient;
import com.drivingschool.scheduling.dto.InstructorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Helper service for instructor operations with Redis caching.
 * 
 * <p>Uses self-injection to make Spring cache work when methods call each other.
 * Without it, calling getInstructorOrThrow() directly from getInstructorName() 
 * would bypass the cache because Spring can't intercept internal method calls.</p>
 */
@Service
@Slf4j
public class InstructorHelperService {
    private final InstructorClient instructorClient;
    private final InstructorHelperService self;

    /**
     * Constructor with self-injection to enable cache on internal method calls.
     * 
     * @param instructorClient Feign client for instructor-service
     * @param self Spring proxy of this service (enables cache interception)
     */
    public InstructorHelperService(InstructorClient instructorClient, @Lazy InstructorHelperService self) {
        this.instructorClient = instructorClient;
        this.self = self;
    }

    /**
     * Gets instructor data from instructor-service with Redis caching (10 min TTL).
     * 
     * <p>Must be called via 'self' proxy from other methods in this class to enable cache.</p>
     * 
     * @param instructorId instructor ID
     * @return instructor data
     * @throws ResourceNotFoundException if instructor not found
     */
    @Cacheable(value = "instructorCache", key = "#instructorId")
    public InstructorResponse getInstructorOrThrow(Long instructorId) {
        log.debug("Fetching instructor with ID: {} from instructor-service", instructorId);
        ApiResult<InstructorResponse> instructorResult = instructorClient.getInstructorById(instructorId);
        
        if (instructorResult == null || instructorResult.data() == null) {
            log.warn("Instructor with ID {} not found", instructorId);
            throw new ResourceNotFoundException("Instructor", instructorId);
        }
        
        return instructorResult.data();
    }

    /**
     * Gets instructor full name as "FirstName LastName".
     * 
     * <p>Uses cached instructor data via self.getInstructorOrThrow() to avoid API calls.</p>
     * 
     * @param instructorId instructor ID
     * @return formatted name "FirstName LastName"
     * @throws ResourceNotFoundException if instructor not found
     */
    public String getInstructorName(Long instructorId) {
        InstructorResponse instructor = self.getInstructorOrThrow(instructorId);
        return instructor.firstName() + " " + instructor.lastName();
    }
}

