package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Represents a document uploaded by a startup (e.g., Financial Report, Legal File, Pitch Deck)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "startup_documents")
public class StartupDocument {

    @Id
    private String id;

    private String startupId;       // reference to startup
    private String documentName;    // original file name
    private String documentPath;    // full stored path
    private DocumentType documentType; // Financial / Legal / Pitch
    private Long fileSize;          // in bytes
    private LocalDateTime uploadedAt; // timestamp
}
