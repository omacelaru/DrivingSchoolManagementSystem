package com.drivingschool.student.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotBlank(message = "Document type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @NotBlank(message = "File path is required")
    @Column(nullable = false, length = 500)
    private String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadDate;

    public enum DocumentType {
        ID_COPY, MEDICAL_CERTIFICATE, PHOTO, DRIVING_LICENSE_COPY
    }

    public enum DocumentStatus {
        PENDING, APPROVED, REJECTED
    }
}

