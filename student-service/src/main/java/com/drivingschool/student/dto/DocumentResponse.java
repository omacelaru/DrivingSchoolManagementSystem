package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Document;

import java.time.LocalDateTime;

public class DocumentResponse {
    private Long id;
    private Document.DocumentType documentType;
    private String filePath;
    private Document.DocumentStatus status;
    private LocalDateTime uploadDate;

    public DocumentResponse() {
    }

    public DocumentResponse(Long id, Document.DocumentType documentType, String filePath, Document.DocumentStatus status, LocalDateTime uploadDate) {
        this.id = id;
        this.documentType = documentType;
        this.filePath = filePath;
        this.status = status;
        this.uploadDate = uploadDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Document.DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(Document.DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Document.DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(Document.DocumentStatus status) {
        this.status = status;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }
}
