package com.drivingschool.student.mapper;

import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.StudentProfileResponse;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.DrivingLicenseCategory;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.entity.StudentProfile;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class StudentMapper {
    public Student toEntity(StudentRequest request) {
        return Student.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .cnp(request.cnp())
                .email(request.email())
                .phone(request.phone())
                .address(request.address())
                .status(Student.StudentStatus.PENDING)
                .build();
    }

    public StudentResponse toResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getFirstName(),
                student.getLastName(),
                student.getCnp(),
                student.getEmail(),
                student.getPhone(),
                student.getAddress(),
                student.getStatus(),
                student.getRegistrationDate(),
                student.getLastModifiedDate(),
                student.getDocuments() != null
                        ? student.getDocuments().stream()
                                .map(this::toDocumentResponse)
                                .collect(Collectors.toList())
                        : null,
                toProfileResponse(student.getProfile()),
                toLicenseCategoryCodes(student.getTargetLicenseCategories())
        );
    }

    private StudentProfileResponse toProfileResponse(StudentProfile profile) {
        if (profile == null) {
            return null;
        }
        return new StudentProfileResponse(
                profile.getEmergencyContactName(),
                profile.getEmergencyContactPhone(),
                profile.getNotes()
        );
    }

    private List<String> toLicenseCategoryCodes(Set<DrivingLicenseCategory> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return categories.stream()
                .map(Enum::name)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public void updateEntity(Student student, StudentRequest request) {
        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setCnp(request.cnp());
        student.setEmail(request.email());
        student.setPhone(request.phone());
        student.setAddress(request.address());
    }

    public DocumentResponse toDocumentResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getFilePath(),
                document.getStatus(),
                document.getUploadDate()
        );
    }
}

