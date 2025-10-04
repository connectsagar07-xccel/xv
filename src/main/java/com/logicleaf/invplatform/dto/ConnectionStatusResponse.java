package com.logicleaf.invplatform.dto;

import com.logicleaf.invplatform.model.MappingStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConnectionStatusResponse {
    private String mappingId;
    private String startupId;
    private String startupName;
    private String investorId;
    private String investorName;
    private MappingStatus status;
}