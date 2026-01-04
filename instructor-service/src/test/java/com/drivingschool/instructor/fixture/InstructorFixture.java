package com.drivingschool.instructor.fixture;

import com.drivingschool.instructor.dto.InstructorRequest;
import com.drivingschool.instructor.dto.InstructorResponse;
import com.drivingschool.instructor.entity.Instructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InstructorFixture {

    public static final InstructorFixture INSTANCE = InstructorFixture.builder()
            .defaultInstructorId(1L)
            .defaultFirstName("John")
            .defaultLastName("Smith")
            .defaultLicenseNumber("LIC-12345")
            .defaultEmail("john.smith@example.com")
            .defaultPhone("0712345678")
            .defaultSpecialization(Instructor.Specialization.BOTH)
            .defaultRating(4.5)
            .build();

    private final Long defaultInstructorId;
    private final String defaultFirstName;
    private final String defaultLastName;
    private final String defaultLicenseNumber;
    private final String defaultEmail;
    private final String defaultPhone;
    private final Instructor.Specialization defaultSpecialization;
    private final Double defaultRating;

    public static Long defaultInstructorId() {
        return INSTANCE.getDefaultInstructorId();
    }

    public static String defaultFirstName() {
        return INSTANCE.getDefaultFirstName();
    }

    public static String defaultLastName() {
        return INSTANCE.getDefaultLastName();
    }

    public static String defaultLicenseNumber() {
        return INSTANCE.getDefaultLicenseNumber();
    }

    public static String defaultEmail() {
        return INSTANCE.getDefaultEmail();
    }

    public static String defaultPhone() {
        return INSTANCE.getDefaultPhone();
    }

    public static Instructor.Specialization defaultSpecialization() {
        return INSTANCE.getDefaultSpecialization();
    }

    public static Double defaultRating() {
        return INSTANCE.getDefaultRating();
    }

    public static InstructorRequest instructorRequest() {
        return new InstructorRequest(
                defaultFirstName(),
                defaultLastName(),
                defaultLicenseNumber(),
                defaultEmail(),
                defaultPhone(),
                defaultSpecialization()
        );
    }

    public static InstructorRequest instructorRequest(String licenseNumber, String email) {
        return new InstructorRequest(
                defaultFirstName(),
                defaultLastName(),
                licenseNumber,
                email,
                defaultPhone(),
                defaultSpecialization()
        );
    }

    public static Instructor instructor() {
        return Instructor.builder()
                .id(defaultInstructorId())
                .firstName(defaultFirstName())
                .lastName(defaultLastName())
                .licenseNumber(defaultLicenseNumber())
                .email(defaultEmail())
                .phone(defaultPhone())
                .specialization(defaultSpecialization())
                .rating(defaultRating())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Instructor instructor(Long id) {
        return Instructor.builder()
                .id(id)
                .firstName(defaultFirstName())
                .lastName(defaultLastName())
                .licenseNumber(defaultLicenseNumber())
                .email(defaultEmail())
                .phone(defaultPhone())
                .specialization(defaultSpecialization())
                .rating(defaultRating())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static InstructorResponse instructorResponse() {
        return new InstructorResponse(
                defaultInstructorId(),
                defaultFirstName(),
                defaultLastName(),
                defaultLicenseNumber(),
                defaultEmail(),
                defaultPhone(),
                defaultSpecialization(),
                defaultRating(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    public static InstructorResponse instructorResponse(Long id) {
        return new InstructorResponse(
                id,
                defaultFirstName(),
                defaultLastName(),
                defaultLicenseNumber(),
                defaultEmail(),
                defaultPhone(),
                defaultSpecialization(),
                defaultRating(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
