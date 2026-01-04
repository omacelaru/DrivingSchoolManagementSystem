package com.drivingschool.scheduling.fixture;

import com.drivingschool.scheduling.dto.InstructorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class InstructorResponseFixture {

    public static final InstructorResponseFixture INSTANCE = InstructorResponseFixture.builder()
            .defaultInstructorId(1L)
            .defaultFirstName("John")
            .defaultLastName("Smith")
            .defaultLicenseNumber("LIC-12345")
            .defaultEmail("john.smith@example.com")
            .defaultPhone("0712345678")
            .defaultSpecialization("BOTH")
            .defaultRating(4.5)
            .build();

    private final Long defaultInstructorId;
    private final String defaultFirstName;
    private final String defaultLastName;
    private final String defaultLicenseNumber;
    private final String defaultEmail;
    private final String defaultPhone;
    private final String defaultSpecialization;
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

    public static String defaultSpecialization() {
        return INSTANCE.getDefaultSpecialization();
    }

    public static Double defaultRating() {
        return INSTANCE.getDefaultRating();
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
