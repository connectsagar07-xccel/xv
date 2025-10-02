package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestorProfileRequest {
    private String investorType;
    private String firmName;
    private String ticketSize;
    private List<String> sectorFocus;
    private Double aum;
}