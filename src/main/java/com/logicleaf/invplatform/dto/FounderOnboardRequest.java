package com.logicleaf.invplatform.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FounderOnboardRequest {
    private String startupName;
    private String sector;
    private String stage;
    private BigDecimal fundingRaised;
    private String hqLocation;
    private Integer teamSize;
    private String website;
}
