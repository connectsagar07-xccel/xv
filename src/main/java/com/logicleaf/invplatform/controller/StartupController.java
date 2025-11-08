package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.InvestorFullResponse;
import com.logicleaf.invplatform.service.StartupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/founder/startup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDER')")
public class StartupController {

    private final StartupService startupService;

    /**
     * Get complete investor info (User + Investor + Mapping) for logged-in founder's startup.
     */
    @GetMapping("/investors")
    public ResponseEntity<?> getFullInvestors(@AuthenticationPrincipal UserDetails userDetails) {

        List<InvestorFullResponse> investors =
                startupService.getFullInvestorDataForStartup(userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Complete investor details fetched successfully.");
        response.put("data", investors);

        return ResponseEntity.ok(response);
    }
}
