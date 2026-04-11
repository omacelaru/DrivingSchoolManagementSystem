package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Document;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response DTO containing document information")
public record DocumentResponse(
    @Schema(description = "Unique document identifier", example = "1")
    Long id,
    
    @Schema(description = "Type of document", example = "ID_COPY")
    Document.DocumentType documentType,
    
    @Schema(description = "Path to the document file", example = "/documents/student_1/id_card.pdf")
    String filePath,
    
    @Schema(description = "Document status", example = "APPROVED")
    Document.DocumentStatus status,
    
    @Schema(description = "Date and time when document was uploaded", example = "2027-01-01T10:30:00")
    LocalDateTime uploadDate
) {
}

