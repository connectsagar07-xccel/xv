package com.logicleaf.invplatform.dto;

import com.logicleaf.invplatform.model.InvestorRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConnectStartupRequest {
    @NotBlank
    private String startupId;

    @NotNull
    private InvestorRole investorRole;
}