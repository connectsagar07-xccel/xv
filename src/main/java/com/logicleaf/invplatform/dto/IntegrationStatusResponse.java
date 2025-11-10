package com.logicleaf.invplatform.dto;

import com.logicleaf.invplatform.model.IntegrationStatus;
import com.logicleaf.invplatform.model.IntegrationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationStatusResponse {
    private IntegrationType integrationType;      // e.g. ZOHO, SALESFORCE, STRIPE
    private IntegrationStatus status;         // CONNECTED / DISCONNECTED / ERROR
    private LocalDateTime lastSyncTime;           // Last successful sync timestamp
}
