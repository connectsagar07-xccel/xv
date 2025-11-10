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
@PreAuthorize("hasRole('FOUNDER')")
@RequestMapping("/api/timely-reports")
@RequiredArgsConstructor
public class TimelyReportController {

        private final TimelyReportService timelyReportService;
        private final UserService userService;
        private final ObjectMapper objectMapper;

        @PostMapping(consumes = { "multipart/form-data" })
        public ResponseEntity<?> createTimelyReport(
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestPart("report") String reportJson,
                        @RequestPart(value = "attachments", required = false) MultipartFile[] attachments)
                        throws Exception {

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

        @PutMapping(value = "/{reportId}", consumes = { "multipart/form-data" })
        public ResponseEntity<?> updateTimelyReport(
                        @PathVariable String reportId,
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestPart("report") String reportJson,
                        @RequestPart(value = "attachments", required = false) MultipartFile[] attachments)
                        throws Exception {

                TimelyReport updatedReportRequest = objectMapper.readValue(reportJson, TimelyReport.class);

                String founderEmail = userDetails.getUsername();
                User founder = userService.findByEmail(founderEmail)
                                .orElseThrow(() -> new RuntimeException("Authenticated founder not found"));

                updatedReportRequest.setFounderUserId(founder.getId());

                TimelyReport updatedReport = timelyReportService.updateTimelyReport(reportId, updatedReportRequest,
                                attachments);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Timely report updated successfully");
                response.put("data", updatedReport);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/draft")
        public ResponseEntity<?> getDraftTimelyReport(@AuthenticationPrincipal UserDetails userDetails) {
                // 1️⃣ Extract email from authenticated user
                String founderEmail = userDetails.getUsername();

                // 2️⃣ Find founder by email
                User founder = userService.findByEmail(founderEmail)
                                .orElseThrow(() -> new RuntimeException("Authenticated founder not found"));

                // 3️⃣ Fetch the draft report for this founder
                TimelyReport draftReport = timelyReportService.getDraftTimelyReport(founder.getId());

                // 4️⃣ Build response
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", draftReport != null
                                ? "Draft report fetched successfully"
                                : "No draft report found");
                response.put("data", draftReport);

                return ResponseEntity.status(HttpStatus.OK).body(response);
        }

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
