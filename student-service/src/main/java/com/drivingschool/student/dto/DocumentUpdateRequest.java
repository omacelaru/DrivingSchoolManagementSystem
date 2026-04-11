package com.drivingschool.student.dto;

import com.drivingschool.student.entity.Document;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

@Schema(description = "Partial update for a student document. Omit a property or send null to leave it unchanged; at least one property must be present.")
public record DocumentUpdateRequest(
        @Schema(description = "Document type")
        Optional<Document.DocumentType> documentType,

        @Schema(description = "Storage path or URI")
        Optional<String> filePath,

        @Schema(description = "Workflow status")
        Optional<Document.DocumentStatus> status
) {
}
