package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;
    private Document.DocumentType documentType;
    private String filePath;
    private Document.DocumentStatus status;
    private LocalDateTime uploadDate;
}

