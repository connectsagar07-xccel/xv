package com.logicleaf.invplatform.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "investors")
public class Investor {
    @Id
    private String id;
    private String userId;
    private String investorType;
    private String firmName;
    private String ticketSize;
    private List<String> sectorFocus;
    private Double aum; // Assets Under Management
}