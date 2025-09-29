package com.logicleaf.invplatform.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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

    @Indexed(unique = true)
    private String userId; // Link to the User

    private String startupName;
    private String sector;
    private String stage;
    private String fundingRaised;
    private String hqLocation;
    private int teamSize;
    private String website;
}