package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.IntegrationStatusResponse;
import com.logicleaf.invplatform.service.IntegrationService;
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
@PreAuthorize("hasRole('FOUNDER')")
@RequestMapping("api/founder/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private final IntegrationService integrationService;

    @GetMapping("/status")
    public ResponseEntity<?> getIntegrationStatuses(@AuthenticationPrincipal UserDetails userDetails) {
        String startupId = userDetails.getUsername(); // Replace with startupId lookup if needed
        List<IntegrationStatusResponse> statuses = integrationService.getIntegrationStatuses(startupId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Integration statuses fetched successfully.");
        response.put("data", statuses);

        return ResponseEntity.ok(response);
    }
}
