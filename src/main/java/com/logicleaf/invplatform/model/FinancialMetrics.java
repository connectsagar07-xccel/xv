package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "financial_metrics")
public class FinancialMetrics {
    @Id
    private String id;
    private String startupId;
    private String month; // "2025-01"
    private Double monthlyRevenue;
    private Double monthlyExpenses;
    private Double cogs;
    private Double grossProfit;
    private Double grossMarginPercentage;
    private Double netProfit;
    private Double netMarginPercentage;
    private Double burnRate;
    private Double mrr;
    private Double arr;
    private LocalDateTime calculatedAt;
}
