package com.drivingschool.student.service;

import com.drivingschool.common.exception.BusinessException;
import com.drivingschool.common.exception.ResourceNotFoundException;
import com.drivingschool.student.dto.DocumentResponse;
import com.drivingschool.student.dto.StudentRequest;
import com.drivingschool.student.dto.StudentResponse;
import com.drivingschool.student.entity.Document;
import com.drivingschool.student.entity.Student;
import com.drivingschool.student.mapper.StudentMapper;
import com.drivingschool.student.repository.DocumentRepository;
import com.drivingschool.student.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentService {
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);
    private final StudentRepository studentRepository;
    private final DocumentRepository documentRepository;
    private final StudentMapper studentMapper;

    public StudentService(StudentRepository studentRepository, DocumentRepository documentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.documentRepository = documentRepository;
        this.studentMapper = studentMapper;
    }

    public StudentResponse createStudent(StudentRequest request) {
        log.info("Creating student with CNP: {}", request.getCnp());
        
        if (studentRepository.existsByCnp(request.getCnp())) {
            throw new BusinessException("Student with CNP " + request.getCnp() + " already exists", "DUPLICATE_CNP");
        }
        
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Student with email " + request.getEmail() + " already exists", "DUPLICATE_EMAIL");
        }

        Student student = studentMapper.toEntity(request);
        student = studentRepository.save(student);
        log.info("Student created with ID: {}", student.getId());
        
        return studentMapper.toResponse(student);
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
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));

        // Check for duplicate CNP if changed
        if (!student.getCnp().equals(request.getCnp()) && studentRepository.existsByCnp(request.getCnp())) {
            throw new BusinessException("Student with CNP " + request.getCnp() + " already exists", "DUPLICATE_CNP");
        }

        // Check for duplicate email if changed
        if (!student.getEmail().equals(request.getEmail()) && studentRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Student with email " + request.getEmail() + " already exists", "DUPLICATE_EMAIL");
        }

        studentMapper.updateEntity(student, request);
        student = studentRepository.save(student);
        log.info("Student updated with ID: {}", student.getId());
        
        return studentMapper.toResponse(student);
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
    public List<StudentResponse> getAllStudents(Student.StudentStatus status) {
        log.info("Fetching all students with status: {}", status);
        List<Student> students = status != null 
                ? studentRepository.findByStatus(status)
                : studentRepository.findAll();
        return students.stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> searchStudentsByName(String name) {
        log.info("Searching students by name: {}", name);
        List<Student> students = studentRepository.findByNameContaining(name);
        return students.stream()
                .map(studentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public DocumentResponse uploadDocument(Long studentId, Document.DocumentType documentType, String filePath) {
        log.info("Uploading document for student ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        Document document = new Document();
        document.setStudent(student);
        document.setDocumentType(documentType);
        document.setFilePath(filePath);
        document.setStatus(Document.DocumentStatus.PENDING);

        document = documentRepository.save(document);
        log.info("Document uploaded with ID: {}", document.getId());
        
        return studentMapper.toDocumentResponse(document);
    }

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
}

