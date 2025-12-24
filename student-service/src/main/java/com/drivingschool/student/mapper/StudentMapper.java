package com.drivingschool.student.mapper;

import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.Student;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class StudentMapper {
    public Student toEntity(StudentRequest request) {
        return Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .cnp(request.getCnp())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .status(Student.StudentStatus.PENDING)
                .build();
    }

    public StudentResponse toResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .cnp(student.getCnp())
                .email(student.getEmail())
                .phone(student.getPhone())
                .address(student.getAddress())
                .status(student.getStatus())
                .registrationDate(student.getRegistrationDate())
                .lastModifiedDate(student.getLastModifiedDate())
                .documents(student.getDocuments() != null 
                        ? student.getDocuments().stream()
                                .map(this::toDocumentResponse)
                                .collect(Collectors.toList())
                        : null)
                .build();
    }

    public void updateEntity(Student student, StudentRequest request) {
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setCnp(request.getCnp());
        student.setEmail(request.getEmail());
        student.setPhone(request.getPhone());
        student.setAddress(request.getAddress());
    }

    public DocumentResponse toDocumentResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .filePath(document.getFilePath())
                .status(document.getStatus())
                .uploadDate(document.getUploadDate())
                .build();
    }
}

