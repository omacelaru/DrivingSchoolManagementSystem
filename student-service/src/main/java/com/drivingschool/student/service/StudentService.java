package com.drivingschool.student.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ErrorCode;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.common.dto.PageResponse;
import com.drivingschool.common.mapper.PageResponseMapper;
import com.drivingschool.common.pagination.PageableFactory;
import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.DocumentUpdateRequest;
import com.drivingschool.student.dto.StudentProfileRequest;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.dto.StudentSelfUpdateRequest;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.DrivingLicenseCategory;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.entity.StudentProfile;
import com.drivingschool.student.mapper.StudentMapper;
import com.drivingschool.student.pagination.StudentSortField;
import com.drivingschool.student.repository.DocumentRepository;
import com.drivingschool.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;
    private final DocumentRepository documentRepository;
    private final StudentMapper studentMapper;
    @Value("${app.pagination.default-page-size:20}")
    private int defaultPageSize;

    public StudentResponse createStudent(StudentRequest request) {
        log.info("Creating student with CNP: {}", request.cnp());

        validateStudentUniqueness(request.cnp(), request.email());
        Student student = studentMapper.toEntity(request);
        request.profile().ifPresent(profileRequest -> applyProfileOnCreate(student, profileRequest));
        applyTargetLicenseCategories(student, request.targetDrivingCategoryCodes());
        Student saved = studentRepository.save(student);
        log.info("Student created with ID: {}", saved.getId());

        return studentMapper.toResponse(saved);
    }

    private void applyProfileOnCreate(Student student, StudentProfileRequest profileRequest) {
        StudentProfile profile = StudentProfile.builder()
                .student(student)
                .emergencyContactName(profileRequest.emergencyContactName())
                .emergencyContactPhone(profileRequest.emergencyContactPhone())
                .notes(profileRequest.notes())
                .build();
        student.setProfile(profile);
    }

    private void applyTargetLicenseCategories(Student student, List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            throw new BusinessException(
                    "At least one target driving licence category is required",
                    ErrorCode.TARGET_LICENSE_CATEGORIES_REQUIRED);
        }
        Set<DrivingLicenseCategory> resolved = new HashSet<>();
        for (String raw : codes) {
            try {
                resolved.add(DrivingLicenseCategory.fromApiCode(raw));
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(
                        "Invalid driving license category code: " + raw,
                        ErrorCode.INVALID_DRIVING_CATEGORY);
            }
        }
        if (resolved.size() != codes.size()) {
            throw new BusinessException("Duplicate driving license category codes in request", ErrorCode.INVALID_DRIVING_CATEGORY);
        }
        student.getTargetLicenseCategories().clear();
        student.getTargetLicenseCategories().addAll(resolved);
    }

    private void validateStudentUniqueness(String cnp, String email) {
        if (studentRepository.existsByCnp(cnp)) {
            throw new BusinessException("Student with CNP " + cnp + " already exists", ErrorCode.DUPLICATE_CNP);
        }

        if (studentRepository.existsByEmail(email)) {
            throw new BusinessException("Student with email " + email + " already exists", ErrorCode.DUPLICATE_EMAIL);
        }
    }

    @Cacheable(value = "students", key = "#id")
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        log.info("Fetching student with ID: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
        return studentMapper.toResponse(student);
    }

    @CacheEvict(value = "students", key = "#id")
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        log.info("Updating student with ID: {}", id);
        Student student = findStudentById(id);
        validateStudentUniquenessForUpdate(student, request);
        studentMapper.updateEntity(student, request);
        request.profile().ifPresent(profileRequest -> mergeProfile(student, profileRequest));
        applyTargetLicenseCategories(student, request.targetDrivingCategoryCodes());
        Student updated = studentRepository.save(student);
        log.info("Student updated with ID: {}", updated.getId());

        return studentMapper.toResponse(updated);
    }

    @CacheEvict(value = "students", key = "#id")
    public StudentResponse updateOwnStudent(Long id, StudentSelfUpdateRequest request) {
        log.info("Updating student self-profile with ID: {}", id);
        Student student = findStudentById(id);

        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setPhone(request.phone());
        student.setAddress(request.address());
        if (request.profile() != null) {
            mergeProfile(student, request.profile());
        }

        Student updated = studentRepository.save(student);
        log.info("Student self-profile updated with ID: {}", updated.getId());
        return studentMapper.toResponse(updated);
    }

    private void mergeProfile(Student student, StudentProfileRequest profileRequest) {
        StudentProfile profile = student.getProfile();
        if (profile == null) {
            profile = StudentProfile.builder().student(student).build();
            student.setProfile(profile);
        }
        profile.setEmergencyContactName(profileRequest.emergencyContactName());
        profile.setEmergencyContactPhone(profileRequest.emergencyContactPhone());
        profile.setNotes(profileRequest.notes());
    }

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    private void validateStudentUniquenessForUpdate(Student existingStudent, StudentRequest request) {
        if (!existingStudent.getCnp().equals(request.cnp()) && studentRepository.existsByCnp(request.cnp())) {
            throw new BusinessException("Student with CNP " + request.cnp() + " already exists", ErrorCode.DUPLICATE_CNP);
        }

        if (!existingStudent.getEmail().equals(request.email()) && studentRepository.existsByEmail(request.email())) {
            throw new BusinessException("Student with email " + request.email() + " already exists", ErrorCode.DUPLICATE_EMAIL);
        }
    }

    @CacheEvict(value = "students", key = "#id")
    public void deleteStudent(Long id) {
        log.info("Deleting student with ID: {}", id);
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student", id);
        }
        studentRepository.deleteById(id);
        log.info("Student deleted with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> getStudentsPage(
            Student.StudentStatus status,
            Integer page,
            Integer size,
            String sortBy,
            String sortDir
    ) {
        Pageable pageable = PageableFactory.build(
                page, size, sortBy, sortDir, defaultPageSize, StudentSortField.class
        );
        Page<Student> studentPage = status != null
                ? studentRepository.findByStatus(status, pageable)
                : studentRepository.findAll(pageable);
        return PageResponseMapper.from(studentPage.map(studentMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> searchStudentsByName(String name) {
        log.info("Searching students by name: {}", name);
        List<Student> students = studentRepository.findByNameContaining(name);
        return students.stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "students", key = "#studentId")
    public DocumentResponse uploadDocument(Long studentId, Document.DocumentType documentType, String filePath) {
        log.info("Uploading document {} for student ID: {}", documentType, studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        Document document = Document.builder()
                .student(student)
                .documentType(documentType)
                .filePath(filePath)
                .status(Document.DocumentStatus.PENDING)
                .build();

        document = documentRepository.save(document);
        log.info("Document uploaded with ID: {}", document.getId());

        checkAndUpdateStudentStatus(studentId);

        return studentMapper.toDocumentResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getStudentDocuments(Long studentId) {
        log.info("Fetching documents for student ID: {}", studentId);
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student", studentId);
        }

        List<Document> documents = documentRepository.findByStudentId(studentId);
        return documents.stream()
                .map(studentMapper::toDocumentResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "students", key = "#studentId")
    public DocumentResponse updateStudentDocument(Long studentId, Long documentId, DocumentUpdateRequest request) {
        log.info("Updating document ID {} for student ID: {}", documentId, studentId);
        if (!hasDocumentUpdate(request)) {
            throw new BusinessException(
                    "At least one of documentType, filePath, or status must be provided",
                    ErrorCode.DOCUMENT_UPDATE_EMPTY);
        }
        request.filePath().ifPresent(path -> {
            if (path.isBlank()) {
                throw new BusinessException("filePath cannot be blank when provided", ErrorCode.DOCUMENT_FILE_PATH_INVALID);
            }
        });
        final Document document = documentRepository.findByIdAndStudentId(documentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        request.documentType().ifPresent(document::setDocumentType);
        request.filePath().ifPresent(document::setFilePath);
        request.status().ifPresent(document::setStatus);
        Document saved = documentRepository.save(document);
        checkAndUpdateStudentStatus(studentId);
        return studentMapper.toDocumentResponse(saved);
    }

    @CacheEvict(value = "students", key = "#studentId")
    public void deleteStudentDocument(Long studentId, Long documentId) {
        log.info("Deleting document ID {} for student ID: {}", documentId, studentId);
        Document document = documentRepository.findByIdAndStudentId(documentId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", documentId));
        documentRepository.delete(document);
        log.info("Document deleted with ID: {}", documentId);
        checkAndUpdateStudentStatus(studentId);
    }

    private static boolean hasDocumentUpdate(DocumentUpdateRequest request) {
        return request.documentType().isPresent()
                || request.filePath().isPresent()
                || request.status().isPresent();
    }

    /**
     * Checks if all required documents are uploaded for a student.
     * Required documents: ID_COPY, PHOTO, MEDICAL_CERTIFICATE
     * If all required documents are present, updates student status to ACTIVE.
     */
    private void checkAndUpdateStudentStatus(Long studentId) {
        log.debug("Checking required documents for student ID: {}", studentId);
        
        Student student = findStudentById(studentId);
        if (!isStudentPending(student)) {
            return;
        }

        Set<Document.DocumentType> requiredTypes = getRequiredDocumentTypes();
        List<Document> allDocuments = documentRepository.findByStudentId(studentId);
        Set<Document.DocumentType> uploadedTypes = extractDocumentTypes(allDocuments);

        if (hasAllRequiredDocuments(uploadedTypes, requiredTypes)) {
            activateStudent(student);
            approveDocuments(allDocuments, studentId);
        } else {
            logMissingDocuments(studentId, requiredTypes, uploadedTypes);
        }
    }

    private boolean isStudentPending(Student student) {
        if (student.getStatus() != Student.StudentStatus.PENDING) {
            log.debug("Student {} is already {}, skipping status check", student.getId(), student.getStatus());
            return false;
        }
        return true;
    }

    private Set<Document.DocumentType> getRequiredDocumentTypes() {
        return Set.of(
                Document.DocumentType.ID_COPY,
                Document.DocumentType.PHOTO,
                Document.DocumentType.MEDICAL_CERTIFICATE
        );
    }

    private Set<Document.DocumentType> extractDocumentTypes(List<Document> documents) {
        return documents.stream()
                .map(Document::getDocumentType)
                .collect(Collectors.toSet());
    }

    private boolean hasAllRequiredDocuments(Set<Document.DocumentType> uploadedTypes, Set<Document.DocumentType> requiredTypes) {
        return uploadedTypes.containsAll(requiredTypes);
    }

    private void activateStudent(Student student) {
        log.info("All required documents are uploaded for student ID: {}. Updating status to ACTIVE", student.getId());
        student.setStatus(Student.StudentStatus.ACTIVE);
        studentRepository.save(student);
        log.info("Student {} status updated to ACTIVE", student.getId());
    }

    private void approveDocuments(List<Document> documents, Long studentId) {
        documents.forEach(doc -> doc.setStatus(Document.DocumentStatus.APPROVED));
        documentRepository.saveAll(documents);
        log.info("All documents for student {} set to APPROVED", studentId);
    }

    private void logMissingDocuments(Long studentId, Set<Document.DocumentType> requiredTypes, Set<Document.DocumentType> uploadedTypes) {
        Set<Document.DocumentType> missingTypes = requiredTypes.stream()
                .filter(type -> !uploadedTypes.contains(type))
                .collect(Collectors.toSet());
        log.debug("Student {} is missing required documents: {}", studentId, missingTypes);
    }
}

