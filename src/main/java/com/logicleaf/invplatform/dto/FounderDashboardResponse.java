package com.logicleaf.invplatform.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FounderDashboardResponse {

    private int teamSize;
    private int cashRunwayMonths;


    // Chart data
    private List<MonthlyMetric> revenueGrowth;
    private List<MonthlyMetric> burnRateAnalysis;

    // KPI donut chart
    private Map<String, Double> keyPerformanceIndicators;

    // Progress bars
    private Map<String, Integer> monthlyGoalsProgress;
}
