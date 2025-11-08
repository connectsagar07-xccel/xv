package com.logicleaf.invplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicleaf.invplatform.model.TimelyReport;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.service.TimelyReportService;
import com.logicleaf.invplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/timely-reports")
@RequiredArgsConstructor
public class TimelyReportController {

    private final TimelyReportService timelyReportService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @PreAuthorize("hasRole('FOUNDER')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createTimelyReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart("report") String reportJson,
            @RequestPart(value = "attachments", required = false) MultipartFile[] attachments
    ) throws Exception {

        TimelyReport reportRequest = objectMapper.readValue(reportJson, TimelyReport.class);

        String founderEmail = userDetails.getUsername();
        User founder = userService.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated founder not found"));

        reportRequest.setFounderUserId(founder.getId());

        TimelyReport created = timelyReportService.createTimelyReport(reportRequest, attachments);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Timely report created successfully with attachments and emailed to investors");
        response.put("data", created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



    /**
     * Get all Timely Reports for the authenticated founder
     */
    @PreAuthorize("hasRole('FOUNDER')")
    @GetMapping
    public ResponseEntity<?> getReportsByAuthenticatedFounder(
            @AuthenticationPrincipal UserDetails userDetails) {

        String founderEmail = userDetails.getUsername();
        User founder = userService.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated founder not found"));

        List<TimelyReport> reports = timelyReportService.getReportsByFounder(founder.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", reports.size());
        response.put("data", reports);

        return ResponseEntity.ok(response);
    }
}
