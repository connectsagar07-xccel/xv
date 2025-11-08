package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.InviteInvestorRequest;
import com.logicleaf.invplatform.model.StartupInvestorMapping;
import com.logicleaf.invplatform.service.ConnectionService;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/founder")

public class FounderController {

    @Autowired
    private ConnectionService connectionService;

    @PreAuthorize("hasRole('FOUNDER')")
    @PostMapping("/connections/invite")
    public ResponseEntity<?> inviteInvestor(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody InviteInvestorRequest inviteRequest) {
        try {
            StartupInvestorMapping mapping = connectionService.inviteInvestor(
                    userDetails.getUsername(),
                    inviteRequest.getInvestorEmail(),
                    inviteRequest.getInvestorRole());
            return ResponseEntity.ok(mapping);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PUBLIC: founder approves an investor request (status must be PENDING)
    @GetMapping("/connections/{mappingId}/approve")
    public ResponseEntity<?> approveConnection(@PathVariable String mappingId) {
        StartupInvestorMapping mapping = connectionService.approveConnectionPublic(mappingId);
        return ResponseEntity.ok(mapping);
    }

    // PUBLIC: founder rejects an investor request (delete + email counterparty)
    @GetMapping("/connections/{mappingId}/reject")
    public ResponseEntity<?> rejectConnection(@PathVariable String mappingId) {
        connectionService.rejectByFounder(mappingId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Connection rejected and removed successfully."
        ));
    }
}