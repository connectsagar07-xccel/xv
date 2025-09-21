package com.logicleaf.invplatform.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processed_data")
public class ProcessedData {

    @Id
    private String id;

    private String sourceName; // e.g., "Zoho", "other vendor", etc.
    private String type;       // e.g., "investment data", "trades", etc.

    private Instant receivedAt;
    private Instant processedAt;

    private Map<String, Object> content; // Dynamic response payload

    private Map<String, String> metadata;
}
