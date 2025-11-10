package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "integrations")
public class Integration {

    @Id
    private String id;

    private String startupId;

    private IntegrationType integrationType;   
    private IntegrationStatus status;               

    private LocalDateTime lastSyncTime;

    private String accessToken;

    private String refreshToken;

    private LocalDateTime expiresAt;

    private String connectionConfig; // Store JSON string (orgId, sheetId, etc.)

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();
}
