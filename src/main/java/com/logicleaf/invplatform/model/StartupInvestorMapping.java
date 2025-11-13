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
@Document(collection = "startup_investor_mappings")
public class StartupInvestorMapping {

    @Id
    private String id;

    private String startupId;
    private String investorId;

    private MappingStatus status;

    private InvestorRole investorRole;

    private String investorEmail;

    
}
