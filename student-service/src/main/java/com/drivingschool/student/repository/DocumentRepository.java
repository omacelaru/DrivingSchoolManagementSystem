package com.drivingschool.student.repository;

import com.drivingschool.student.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByIdAndStudentId(Long documentId, Long studentId);

    List<Document> findByStudentId(Long studentId);
    List<Document> findByStudentIdAndStatus(Long studentId, Document.DocumentStatus status);
}

