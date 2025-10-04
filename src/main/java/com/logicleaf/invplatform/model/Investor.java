package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "investors")
public class Investor {
    @Id
    private String id;
    private String userId;
    private InvestorType investorType;
    private String firmName;
    private String ticketSize;
    private List<Sector> sectorFocus;
    private Double aum; // Assets Under Management
}