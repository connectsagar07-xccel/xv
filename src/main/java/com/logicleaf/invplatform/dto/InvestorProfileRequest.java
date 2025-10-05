package com.logicleaf.invplatform.dto;

import com.logicleaf.invplatform.model.InvestorType;
import com.logicleaf.invplatform.model.Sector;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InvestorProfileRequest {
    @NotNull
    private InvestorType investorType;

    @NotBlank
    private String firmName;

    @NotBlank
    private String ticketSize;

    @NotNull
    private List<Sector> sectorFocus;

    private Double aum;
}