package com.drivingschool.student.integration;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.repository.DocumentRepository;
import com.drivingschool.student.repository.StudentRepository;
import com.drivingschool.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("local-h2")
@Transactional
class StudentFlowIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Test
    void createStudent_persistsEntityAndReturnsCreated() {
        studentService.createStudent(studentRequest(
                "Ana", "Popescu", "1234567890123", "ana.popescu@example.com", "0712345678", "Str. Lalelelor 10"));

        assertThat(studentRepository.existsByCnp("1234567890123")).isTrue();
    }

    @Test
    void createStudent_duplicateCnpReturnsBusinessError() {
        studentService.createStudent(studentRequest(
                "Ana", "Popescu", "1234567890123", "ana.popescu@example.com", "0712345678", "Str. Lalelelor 10"));

        BusinessException ex = assertThrows(BusinessException.class, () -> studentService.createStudent(studentRequest(
                "Maria", "Ionescu", "1234567890123", "maria.ionescu@example.com", "0799999999", "Bd. Unirii 1")));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATE_CNP.getCode());
    }

    @Test
    void uploadAllRequiredDocuments_activatesStudentAndApprovesDocuments() {
        studentService.createStudent(studentRequest(
                "George", "Marin", "1980525123456", "george.marin@example.com", "0722222222", "Str. Independentei 20"));

        Student savedStudent = studentRepository.findByCnp("1980525123456").orElseThrow();

        uploadDocument(savedStudent.getId(), Document.DocumentType.ID_COPY);
        uploadDocument(savedStudent.getId(), Document.DocumentType.PHOTO);
        uploadDocument(savedStudent.getId(), Document.DocumentType.MEDICAL_CERTIFICATE);

        Student reloaded = studentRepository.findById(savedStudent.getId()).orElseThrow();
        List<Document> documents = documentRepository.findByStudentId(savedStudent.getId());

        assertThat(reloaded.getStatus()).isEqualTo(Student.StudentStatus.ACTIVE);
        assertThat(documents).hasSize(3);
        assertThat(documents).allMatch(doc -> doc.getStatus() == Document.DocumentStatus.APPROVED);
    }

    private StudentRequest studentRequest(
            String firstName,
            String lastName,
            String cnp,
            String email,
            String phone,
            String address
    ) {
        return new StudentRequest(
                firstName,
                lastName,
                cnp,
                email,
                phone,
                address,
                Optional.empty(),
                List.of("B")
        );
    }

    private void uploadDocument(Long studentId, Document.DocumentType type) {
        studentService.uploadDocument(studentId, type, "/tmp/" + type.name().toLowerCase() + ".pdf");
    }
}

