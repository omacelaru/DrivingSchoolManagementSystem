package com.drivingschool.student.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.DocumentUpdateRequest;
import com.drivingschool.student.dto.StudentProfileRequest;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.fixture.StudentFixture;
import com.drivingschool.student.mapper.StudentMapper;
import com.drivingschool.student.repository.DocumentRepository;
import com.drivingschool.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DocumentRepository documentRepository;

    private final StudentMapper studentMapper = Mappers.getMapper(StudentMapper.class);

    private StudentService studentService;

    private StudentRequest studentRequest;
    private Student student;

    @BeforeEach
    void setUp() {
        studentRequest = StudentFixture.studentRequest();
        student = StudentFixture.studentPending();

        studentService = new StudentService(
                studentRepository,
                documentRepository,
                studentMapper
        );
    }

    @Test
    void whenCreateStudent_thenReturnsStudentResponse() {
        // Given
        String cnp = StudentFixture.defaultCnp();
        String email = StudentFixture.defaultEmail();
        String firstName = StudentFixture.defaultFirstName();
        Long studentId = StudentFixture.defaultStudentId();

        when(studentRepository.existsByCnp(cnp)).thenReturn(false);
        when(studentRepository.existsByEmail(email)).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(studentId);
            return saved;
        });

        // When
        StudentResponse result = studentService.createStudent(studentRequest);

        // Then
        assertNotNull(result);
        assertEquals(firstName, result.firstName());
        assertEquals(studentId, result.id());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void whenCreateStudentWithDuplicateCNP_thenThrowsBusinessException() {
        // Given
        String cnp = StudentFixture.defaultCnp();

        when(studentRepository.existsByCnp(cnp)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> studentService.createStudent(studentRequest));

        assertEquals(ErrorCode.DUPLICATE_CNP.getCode(), exception.getErrorCode());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void whenCreateStudentWithDuplicateEmail_thenThrowsBusinessException() {
        // Given
        String cnp = StudentFixture.defaultCnp();
        String email = StudentFixture.defaultEmail();

        when(studentRepository.existsByCnp(cnp)).thenReturn(false);
        when(studentRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> studentService.createStudent(studentRequest));

        assertEquals(ErrorCode.DUPLICATE_EMAIL.getCode(), exception.getErrorCode());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void whenCreateStudentWithEmptyTargetCategories_thenThrowsBusinessException() {
        StudentRequest request = new StudentRequest(
                StudentFixture.defaultFirstName(),
                StudentFixture.defaultLastName(),
                StudentFixture.defaultCnp(),
                StudentFixture.defaultEmail(),
                StudentFixture.defaultPhone(),
                StudentFixture.defaultAddress(),
                Optional.empty(),
                List.of()
        );

        when(studentRepository.existsByCnp(StudentFixture.defaultCnp())).thenReturn(false);
        when(studentRepository.existsByEmail(StudentFixture.defaultEmail())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> studentService.createStudent(request));
        assertEquals(ErrorCode.TARGET_LICENSE_CATEGORIES_REQUIRED.getCode(), ex.getErrorCode());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void whenCreateStudentWithInvalidDrivingCategory_thenThrowsBusinessException() {
        StudentRequest request = new StudentRequest(
                StudentFixture.defaultFirstName(),
                StudentFixture.defaultLastName(),
                StudentFixture.defaultCnp(),
                StudentFixture.defaultEmail(),
                StudentFixture.defaultPhone(),
                StudentFixture.defaultAddress(),
                Optional.empty(),
                List.of("NOT_A_CODE")
        );

        when(studentRepository.existsByCnp(StudentFixture.defaultCnp())).thenReturn(false);
        when(studentRepository.existsByEmail(StudentFixture.defaultEmail())).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> studentService.createStudent(request));
        assertEquals(ErrorCode.INVALID_DRIVING_CATEGORY.getCode(), ex.getErrorCode());
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void whenCreateStudentWithProfileAndCategories_thenPersistsAssociations() {
        StudentProfileRequest profileRequest = new StudentProfileRequest("Jane Doe", "0721234567", "Prefers mornings");
        StudentRequest request = new StudentRequest(
                StudentFixture.defaultFirstName(),
                StudentFixture.defaultLastName(),
                StudentFixture.defaultCnp(),
                StudentFixture.defaultEmail(),
                StudentFixture.defaultPhone(),
                StudentFixture.defaultAddress(),
                Optional.of(profileRequest),
                List.of("b")
        );

        when(studentRepository.existsByCnp(StudentFixture.defaultCnp())).thenReturn(false);
        when(studentRepository.existsByEmail(StudentFixture.defaultEmail())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> {
            Student saved = invocation.getArgument(0);
            saved.setId(StudentFixture.defaultStudentId());
            return saved;
        });

        StudentResponse result = studentService.createStudent(request);

        assertNotNull(result.profile());
        assertEquals("Jane Doe", result.profile().emergencyContactName());
        assertEquals(List.of("B"), result.targetDrivingCategoryCodes());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void whenGetStudentById_thenReturnsStudentResponse() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));

        // When
        StudentResponse result = studentService.getStudentById(studentId);

        // Then
        assertNotNull(result);
        assertEquals(studentId, result.id());
    }

    @Test
    void whenGetStudentByIdWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById(studentId));
    }

    @Test
    void whenUpdateStudent_thenReturnsUpdatedStudentResponse() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        String updatedFirstName = "Jane";
        String updatedLastName = "Doe";
        String cnp = StudentFixture.defaultCnp();
        String updatedEmail = "jane.doe@example.com";

        StudentRequest updateRequest = StudentFixture.studentRequest(updatedFirstName, updatedLastName, cnp, updatedEmail);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail(updatedEmail)).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        StudentResponse result = studentService.updateStudent(studentId, updateRequest);

        // Then
        assertNotNull(result);
        assertEquals(updatedFirstName, result.firstName());
        assertEquals(updatedLastName, result.lastName());
        assertEquals(updatedEmail, result.email());
        verify(studentRepository, times(1)).save(any(Student.class));
    }

    @Test
    void whenUpdateStudentWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentService.updateStudent(studentId, studentRequest));
    }

    @Test
    void whenUpdateStudentWithDuplicateCNP_thenThrowsBusinessException() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        String duplicateCnp = "9999999999999";

        StudentRequest updateRequest = StudentFixture.studentRequest("Jane", "Doe", duplicateCnp, StudentFixture.defaultEmail());

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.existsByCnp(duplicateCnp)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> studentService.updateStudent(studentId, updateRequest));

        assertEquals(ErrorCode.DUPLICATE_CNP.getCode(), exception.getErrorCode());
    }

    @Test
    void whenUpdateStudentWithDuplicateEmail_thenThrowsBusinessException() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        String duplicateEmail = "other@example.com";

        StudentRequest updateRequest = StudentFixture.studentRequest("Jane", "Doe", StudentFixture.defaultCnp(), duplicateEmail);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail(duplicateEmail)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> studentService.updateStudent(studentId, updateRequest));

        assertEquals(ErrorCode.DUPLICATE_EMAIL.getCode(), exception.getErrorCode());
    }

    @Test
    void whenDeleteStudent_thenDeletesStudent() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        when(studentRepository.existsById(studentId)).thenReturn(true);
        doNothing().when(studentRepository).deleteById(studentId);

        // When
        assertDoesNotThrow(() -> studentService.deleteStudent(studentId));

        // Then
        verify(studentRepository, times(1)).deleteById(studentId);
    }

    @Test
    void whenDeleteStudentWithNonExistentId_thenThrowsResourceNotFoundException() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        when(studentRepository.existsById(studentId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudent(studentId));
    }

    @Test
    void whenGetAllStudentsWithStatus_thenReturnsStudentsWithStatus() {
        // Given
        Student.StudentStatus status = Student.StudentStatus.PENDING;
        int expectedStudentsCount = 1;

        List<Student> students = Collections.singletonList(student);
        when(studentRepository.findByStatus(status)).thenReturn(students);

        // When
        List<StudentResponse> result = studentService.getAllStudents(status);

        // Then
        assertNotNull(result);
        assertEquals(expectedStudentsCount, result.size());
    }

    @Test
    void whenGetAllStudentsWithoutStatus_thenReturnsAllStudents() {
        // Given
        Student.StudentStatus status = null;
        int expectedStudentsCount = 1;

        List<Student> students = Collections.singletonList(student);
        when(studentRepository.findAll()).thenReturn(students);

        // When
        List<StudentResponse> result = studentService.getAllStudents(status);

        // Then
        assertNotNull(result);
        assertEquals(expectedStudentsCount, result.size());
    }

    @Test
    void whenSearchStudentsByName_thenReturnsMatchingStudents() {
        // Given
        String searchName = StudentFixture.defaultFirstName();
        int expectedStudentsCount = 1;

        List<Student> students = Collections.singletonList(student);
        when(studentRepository.findByNameContaining(searchName)).thenReturn(students);

        // When
        List<StudentResponse> result = studentService.searchStudentsByName(searchName);

        // Then
        assertNotNull(result);
        assertEquals(expectedStudentsCount, result.size());
    }

    @Test
    void whenUploadDocument_thenReturnsDocumentResponse() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        Document.DocumentType documentType = Document.DocumentType.ID_COPY;
        String filePath = "/path/to/file.pdf";
        Long documentId = 1L;

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document saved = invocation.getArgument(0);
            saved.setId(documentId);
            return saved;
        });
        when(documentRepository.findByStudentId(studentId)).thenReturn(Collections.emptyList());

        // When
        DocumentResponse result = studentService.uploadDocument(studentId, documentType, filePath);

        // Then
        assertNotNull(result);
        assertEquals(documentId, result.id());
        assertEquals(documentType, result.documentType());
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void whenUploadDocumentWithNonExistentStudentId_thenThrowsResourceNotFoundException() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        Document.DocumentType documentType = Document.DocumentType.ID_COPY;
        String filePath = "/path/to/file.pdf";

        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentService.uploadDocument(studentId, documentType, filePath));
    }

    @Test
    void whenUploadDocumentAndAllRequiredDocumentsUploaded_thenActivatesStudent() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        Document.DocumentType documentType = Document.DocumentType.MEDICAL_CERTIFICATE;
        String filePath = "/path/to/medical.pdf";
        Long idCopyId = 1L;
        Long photoId = 2L;
        Long medicalId = 3L;
        Student.StudentStatus expectedStatus = Student.StudentStatus.ACTIVE;

        Document idCopy = StudentFixture.document(Document.DocumentType.ID_COPY, Document.DocumentStatus.PENDING);
        idCopy.setId(idCopyId);
        Document photo = StudentFixture.document(Document.DocumentType.PHOTO, Document.DocumentStatus.PENDING);
        photo.setId(photoId);
        Document medical = StudentFixture.document(documentType, Document.DocumentStatus.PENDING);
        medical.setId(medicalId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(medicalId);
            }
            return saved;
        });
        when(documentRepository.findByStudentId(studentId)).thenReturn(List.of(idCopy, photo, medical));
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        studentService.uploadDocument(studentId, documentType, filePath);

        // Then
        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);
        verify(studentRepository, times(1)).save(studentCaptor.capture());
        assertEquals(expectedStatus, studentCaptor.getValue().getStatus());
    }

    @Test
    void whenGetStudentDocuments_thenReturnsDocumentsList() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        Document.DocumentType documentType = Document.DocumentType.ID_COPY;
        Document.DocumentStatus documentStatus = Document.DocumentStatus.APPROVED;
        int expectedDocumentsCount = 1;

        Document document = StudentFixture.document(documentType, documentStatus);
        when(studentRepository.existsById(studentId)).thenReturn(true);
        when(documentRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(document));

        // When
        List<DocumentResponse> result = studentService.getStudentDocuments(studentId);

        // Then
        assertNotNull(result);
        assertEquals(expectedDocumentsCount, result.size());
        assertEquals(documentType, result.getFirst().documentType());
    }

    @Test
    void whenGetStudentDocumentsWithNonExistentStudentId_thenThrowsResourceNotFoundException() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        when(studentRepository.existsById(studentId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentDocuments(studentId));
    }

    @Test
    void whenUploadDocumentAndMissingRequiredDocuments_thenDoesNotActivateStudent() {
        // Given
        Long studentId = StudentFixture.defaultStudentId();
        Document.DocumentType documentType = Document.DocumentType.ID_COPY;
        String filePath = "/path/to/id.pdf";
        Long documentId = 1L;

        Document idCopy = StudentFixture.document(documentType, Document.DocumentStatus.PENDING);
        idCopy.setId(documentId);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(documentId);
            }
            return saved;
        });
        when(documentRepository.findByStudentId(studentId)).thenReturn(Collections.singletonList(idCopy));

        // When
        studentService.uploadDocument(studentId, documentType, filePath);

        // Then
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    void whenUpdateStudentDocument_thenReturnsUpdatedResponse() {
        Long studentId = StudentFixture.defaultStudentId();
        Long documentId = 5L;
        Document doc = StudentFixture.document(documentId, Document.DocumentType.ID_COPY, Document.DocumentStatus.PENDING);
        DocumentUpdateRequest request = new DocumentUpdateRequest(empty(), empty(), of(Document.DocumentStatus.APPROVED));

        when(documentRepository.findByIdAndStudentId(documentId, studentId)).thenReturn(Optional.of(doc));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(StudentFixture.student()));
        when(documentRepository.findByStudentId(studentId)).thenReturn(List.of(doc));

        DocumentResponse result = studentService.updateStudentDocument(studentId, documentId, request);

        assertEquals(Document.DocumentStatus.APPROVED, result.status());
        verify(documentRepository).save(doc);
    }

    @Test
    void whenUpdateStudentDocumentWithEmptyBody_thenThrowsBusinessException() {
        Long studentId = StudentFixture.defaultStudentId();
        Long documentId = 5L;

        BusinessException ex = assertThrows(BusinessException.class,
                () -> studentService.updateStudentDocument(studentId, documentId,
                        new DocumentUpdateRequest(empty(), empty(), empty())));
        assertEquals(ErrorCode.DOCUMENT_UPDATE_EMPTY.getCode(), ex.getErrorCode());
        verify(documentRepository, never()).findByIdAndStudentId(any(), any());
    }

    @Test
    void whenUpdateStudentDocumentWithBlankFilePath_thenThrowsBusinessException() {
        Long studentId = StudentFixture.defaultStudentId();
        Long documentId = 5L;

        BusinessException ex = assertThrows(BusinessException.class,
                () -> studentService.updateStudentDocument(studentId, documentId,
                        new DocumentUpdateRequest(empty(), of("   "), empty())));
        assertEquals(ErrorCode.DOCUMENT_FILE_PATH_INVALID.getCode(), ex.getErrorCode());
    }

    @Test
    void whenUpdateStudentDocumentNotFound_thenThrowsResourceNotFoundException() {
        Long studentId = StudentFixture.defaultStudentId();
        Long documentId = 5L;
        when(documentRepository.findByIdAndStudentId(documentId, studentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> studentService.updateStudentDocument(studentId, documentId,
                        new DocumentUpdateRequest(empty(), of("/new/path.pdf"), empty())));
    }

    @Test
    void whenDeleteStudentDocument_thenDeletes() {
        Long studentId = StudentFixture.defaultStudentId();
        Long documentId = 5L;
        Document doc = StudentFixture.document(documentId, Document.DocumentType.ID_COPY, Document.DocumentStatus.PENDING);

        when(documentRepository.findByIdAndStudentId(documentId, studentId)).thenReturn(Optional.of(doc));
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(StudentFixture.student()));
        when(documentRepository.findByStudentId(studentId)).thenReturn(Collections.emptyList());

        studentService.deleteStudentDocument(studentId, documentId);

        verify(documentRepository).delete(doc);
    }

    @Test
    void whenDeleteStudentDocumentNotFound_thenThrowsResourceNotFoundException() {
        Long studentId = StudentFixture.defaultStudentId();
        Long documentId = 5L;
        when(documentRepository.findByIdAndStudentId(documentId, studentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> studentService.deleteStudentDocument(studentId, documentId));
        verify(documentRepository, never()).delete(any());
    }
}
