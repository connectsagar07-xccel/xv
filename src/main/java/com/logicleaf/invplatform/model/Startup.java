package com.logicleaf.invplatform.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "startups")
public class Startup {
    @Id
    private String id;

    private String founderId;     // user.id of the founder
    private String startupName;
    private String sector;        // one of predefined sectors
    private String stage;         // e.g., Seed, Series A
    private BigDecimal fundingRaised;
    private String hqLocation;
    private Integer teamSize;
    private String website;

    private String status;        // e.g., PROFILE_COMPLETED or INCOMPLETE
}
