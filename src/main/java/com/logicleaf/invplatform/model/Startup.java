package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "startups")
public class Startup {
    @Id
    private String id;
    private String founderUserId;
    private String companyName;
    private String startupName;
    private Sector sector;
    private String stage;
    private Double fundingRaised;
    private String hqLocation;
    private Integer teamSize;
    private String website;

    // Zoho Integration Fields
    // private String zohoAccessToken;
    // private String zohoRefreshToken;
    // private String zohoOrganizationId;
    // private java.time.LocalDateTime zohoTokenExpiryTime;
}