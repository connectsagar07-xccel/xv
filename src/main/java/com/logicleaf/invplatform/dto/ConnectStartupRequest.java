package com.logicleaf.invplatform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectStartupRequest {
    @NotBlank
    private String startupId;
}