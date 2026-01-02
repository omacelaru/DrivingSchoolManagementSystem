package com.drivingschool.instructor.mapper;

import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {
    public Instructor toEntity(InstructorRequest request) {
        return Instructor.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .licenseNumber(request.getLicenseNumber())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialization(request.getSpecialization())
                .rating(0.0)
                .build();
    }

    public InstructorResponse toResponse(Instructor instructor) {
        return InstructorResponse.builder()
                .id(instructor.getId())
                .firstName(instructor.getFirstName())
                .lastName(instructor.getLastName())
                .licenseNumber(instructor.getLicenseNumber())
                .email(instructor.getEmail())
                .phone(instructor.getPhone())
                .specialization(instructor.getSpecialization())
                .rating(instructor.getRating())
                .createdAt(instructor.getCreatedAt())
                .lastModifiedDate(instructor.getLastModifiedDate())
                .build();
    }
}

