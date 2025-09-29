package com.logicleaf.invplatform.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "investors")
public class Investor {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId; // Link to the User

    private String investorType; // VC, Angel, Family Office
    private String firmName;
    private String ticketSize;
    private String sectorFocus;
    private String aum; // Assets Under Management (optional)
}