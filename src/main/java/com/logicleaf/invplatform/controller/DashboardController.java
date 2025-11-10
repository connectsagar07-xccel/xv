package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.FounderDashboardResponse;
import com.logicleaf.invplatform.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @PreAuthorize("hasRole('FOUNDER')")
    @GetMapping("/founder/dashboard")
    public ResponseEntity<?> getFounderDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        FounderDashboardResponse dashboard = dashboardService.getFounderDashboardData(userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Founder dashboard fetched successfully");
        response.put("data", dashboard);

        return ResponseEntity.ok(response);
    }
}
