package com.drivingschool.scheduling.fixture;

import com.drivingschool.scheduling.dto.StudentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StudentResponseFixture {

    public static final StudentResponseFixture INSTANCE = StudentResponseFixture.builder()
            .defaultStudentId(1L)
            .defaultFirstName("John")
            .defaultLastName("Doe")
            .defaultEmail("john.doe@example.com")
            .defaultStatus("ACTIVE")
            .build();

    private final Long defaultStudentId;
    private final String defaultFirstName;
    private final String defaultLastName;
    private final String defaultEmail;
    private final String defaultStatus;

    public static Long defaultStudentId() {
        return INSTANCE.getDefaultStudentId();
    }

    public static String defaultFirstName() {
        return INSTANCE.getDefaultFirstName();
    }

    public static String defaultLastName() {
        return INSTANCE.getDefaultLastName();
    }

    public static String defaultEmail() {
        return INSTANCE.getDefaultEmail();
    }

    public static String defaultStatus() {
        return INSTANCE.getDefaultStatus();
    }

    public static StudentResponse studentResponse(String status) {
        return new StudentResponse(
                defaultStudentId(),
                defaultFirstName(),
                defaultLastName(),
                defaultEmail(),
                status
        );
    }

    public static StudentResponse studentResponseActive() {
        return studentResponse("ACTIVE");
    }

    public static StudentResponse studentResponsePending() {
        return studentResponse("PENDING");
    }

    public static StudentResponse studentResponseSuspended() {
        return studentResponse("SUSPENDED");
    }

    public static StudentResponse studentResponseGraduated() {
        return studentResponse("GRADUATED");
    }
}
