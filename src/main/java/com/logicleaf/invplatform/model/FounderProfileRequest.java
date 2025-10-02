package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FounderProfileRequest {
    private String companyName;
    private String startupName;
    private String sector;
    private String stage;
    private Double fundingRaised;
    private String hqLocation;
    private Integer teamSize;
    private String website;
}