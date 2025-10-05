package com.logicleaf.invplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateReportRequest {
    @NotBlank
    private String period; // e.g., "Monthly", "Quarterly"

    @NotBlank
    private String dataJSON; // JSON string with manual inputs and metrics

    private boolean visibility = false; // Default to not visible
}