package com.drivingschool.student.fixture;

import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.Student;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@Builder
@AllArgsConstructor
public class StudentFixture {

    public static final StudentFixture INSTANCE = StudentFixture.builder()
            .defaultStudentId(1L)
            .defaultFirstName("John")
            .defaultLastName("Doe")
            .defaultCnp("1234567890123")
            .defaultEmail("john.doe@example.com")
            .defaultPhone("0123456789")
            .defaultAddress("123 Main St")
            .build();

    private final Long defaultStudentId;
    private final String defaultFirstName;
    private final String defaultLastName;
    private final String defaultCnp;
    private final String defaultEmail;
    private final String defaultPhone;
    private final String defaultAddress;

    public static Long defaultStudentId() {
        return INSTANCE.getDefaultStudentId();
    }

    public static String defaultFirstName() {
        return INSTANCE.getDefaultFirstName();
    }

    public static String defaultLastName() {
        return INSTANCE.getDefaultLastName();
    }

    public static String defaultCnp() {
        return INSTANCE.getDefaultCnp();
    }

    public static String defaultEmail() {
        return INSTANCE.getDefaultEmail();
    }

    public static String defaultPhone() {
        return INSTANCE.getDefaultPhone();
    }

    public static String defaultAddress() {
        return INSTANCE.getDefaultAddress();
    }

    public static StudentRequest studentRequest() {
        return new StudentRequest(
                defaultFirstName(),
                defaultLastName(),
                defaultCnp(),
                defaultEmail(),
                defaultPhone(),
                defaultAddress(),
                Optional.empty(),
                List.of("B")
        );
    }

    public static StudentRequest studentRequest(String firstName, String lastName, String cnp, String email) {
        return new StudentRequest(
                firstName,
                lastName,
                cnp,
                email,
                defaultPhone(),
                defaultAddress(),
                Optional.empty(),
                List.of("B")
        );
    }

    public static Student student() {
        return Student.builder()
                .id(defaultStudentId())
                .firstName(defaultFirstName())
                .lastName(defaultLastName())
                .cnp(defaultCnp())
                .email(defaultEmail())
                .phone(defaultPhone())
                .address(defaultAddress())
                .status(Student.StudentStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .build();
    }

    public static Student student(Long id, Student.StudentStatus status) {
        return Student.builder()
                .id(id)
                .firstName(defaultFirstName())
                .lastName(defaultLastName())
                .cnp(defaultCnp())
                .email(defaultEmail())
                .phone(defaultPhone())
                .address(defaultAddress())
                .status(status)
                .registrationDate(LocalDateTime.now())
                .build();
    }

    public static Student studentPending() {
        return student(defaultStudentId(), Student.StudentStatus.PENDING);
    }

    public static StudentResponse studentResponse(Long id, Student.StudentStatus status) {
        return new StudentResponse(
                id,
                defaultFirstName(),
                defaultLastName(),
                defaultCnp(),
                defaultEmail(),
                defaultPhone(),
                defaultAddress(),
                status,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null,
                List.of()
        );
    }

    public static Document document(Document.DocumentType documentType, Document.DocumentStatus status) {
        return document(1L, documentType, status);
    }

    public static Document document(Long documentId, Document.DocumentType documentType, Document.DocumentStatus status) {
        return Document.builder()
                .id(documentId)
                .student(student())
                .documentType(documentType)
                .filePath("/path/to/file.pdf")
                .status(status)
                .uploadDate(LocalDateTime.now())
                .build();
    }
}
