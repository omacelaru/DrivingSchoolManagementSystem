package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Document;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO containing document information")
public class DocumentResponse {
    @Schema(description = "Unique document identifier", example = "1")
    private Long id;
    
    @Schema(description = "Type of document", example = "ID_CARD")
    private Document.DocumentType documentType;
    
    @Schema(description = "Path to the document file", example = "/documents/student_1/id_card.pdf")
    private String filePath;
    
    @Schema(description = "Document status", example = "APPROVED")
    private Document.DocumentStatus status;
    
    @Schema(description = "Date and time when document was uploaded", example = "2025-01-01T10:30:00")
    private LocalDateTime uploadDate;
}

