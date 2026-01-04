package com.drivingschool.instructor.mapper;

import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {
    public Instructor toEntity(InstructorRequest request) {
        return Instructor.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .licenseNumber(request.licenseNumber())
                .email(request.email())
                .phone(request.phone())
                .specialization(request.specialization())
                .rating(0.0)
                .build();
    }

    public InstructorResponse toResponse(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getLicenseNumber(),
                instructor.getEmail(),
                instructor.getPhone(),
                instructor.getSpecialization(),
                instructor.getRating(),
                instructor.getCreatedAt(),
                instructor.getLastModifiedDate()
        );
    }
}

