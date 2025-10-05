package com.logicleaf.invplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class PortfolioDashboardResponse {
    private Map<String, String> aggregateMetrics; // e.g., { "Total Invested": "500000", "Blended Revenue": "150000" }
    private List<PortfolioStartupSummary> portfolio;
}