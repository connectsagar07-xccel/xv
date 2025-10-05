package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.CreateReportRequest;
import com.logicleaf.invplatform.model.Report;
import com.logicleaf.invplatform.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasRole('FOUNDER')")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<?> createReport(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody CreateReportRequest request) {
        try {
            Report report = reportService.createReport(userDetails.getUsername(), request);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getReports(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Report> reports = reportService.getReportsForStartup(userDetails.getUsername());
            return ResponseEntity.ok(reports);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{reportId}/visibility")
    public ResponseEntity<?> updateReportVisibility(@AuthenticationPrincipal UserDetails userDetails,
                                                    @PathVariable String reportId,
                                                    @RequestBody Map<String, Boolean> visibilityMap) {
        try {
            Boolean isVisible = visibilityMap.get("isVisible");
            if (isVisible == null) {
                return ResponseEntity.badRequest().body("'isVisible' field is required.");
            }
            Report report = reportService.updateReportVisibility(userDetails.getUsername(), reportId, isVisible);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}