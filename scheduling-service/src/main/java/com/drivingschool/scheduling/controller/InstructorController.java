package com.drivingschool.scheduling.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.scheduling.dto.InstructorRequest;
import com.drivingschool.scheduling.dto.InstructorResponse;
import com.drivingschool.scheduling.service.SchedulingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
@Tag(name = "Instructor Management", description = "APIs for managing instructors, including registration and retrieval")
public class InstructorController {
    private final SchedulingService schedulingService;

    @PostMapping
    @Operation(summary = "Register a new instructor", 
              description = "Creates a new instructor with the provided information. Validates license number and email uniqueness.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Instructor created successfully",
                    content = @Content(schema = @Schema(implementation = InstructorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation failed"),
        @ApiResponse(responseCode = "409", description = "Instructor with this license number or email already exists")
    })
    public ResponseEntity<ApiResult<InstructorResponse>> createInstructor(
            @Valid @RequestBody InstructorRequest request) {
        InstructorResponse response = schedulingService.createInstructor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResult.success("Instructor registered successfully", response));
    }
}

