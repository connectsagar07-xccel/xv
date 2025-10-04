package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "startup_metrics")
public class StartupMetrics {
    @Id
    private String id;
    private String startupId;
    private String metricName;
    private String metricValue;
    private LocalDate dateCaptured;
    private String source; // e.g., "zoho", "manual"
}