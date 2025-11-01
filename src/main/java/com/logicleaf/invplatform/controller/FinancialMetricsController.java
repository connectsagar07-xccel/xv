package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.model.FinancialMetrics;
import com.logicleaf.invplatform.service.FinancialMetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/financials")
public class FinancialMetricsController {

    private final FinancialMetricsService financialMetricsService;

    public FinancialMetricsController(FinancialMetricsService financialMetricsService) {
        this.financialMetricsService = financialMetricsService;
    }

    @PostMapping("/{startupId}/calculate")
    public ResponseEntity<FinancialMetrics> calculateFinancialMetrics(
            @PathVariable String startupId,
            @RequestParam String month,
            @RequestParam String organizationId) {
        FinancialMetrics metrics = financialMetricsService.calculateAndSaveFinancialMetrics(startupId, month, organizationId);
        return ResponseEntity.ok(metrics);
    }
}
