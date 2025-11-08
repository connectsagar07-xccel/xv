package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.ConnectStartupRequest;
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
@RequestMapping("/api/investor")

public class InvestorController {

    @Autowired
    private ConnectionService connectionService;

    @PreAuthorize("hasRole('INVESTOR')")
    @PostMapping("/connections/request")
    public ResponseEntity<?> requestConnection(@AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ConnectStartupRequest request) {
        StartupInvestorMapping mapping = connectionService.requestConnection(
                userDetails.getUsername(),
                request.getStartupId(),
                request.getInvestorRole());
        return ResponseEntity.ok(mapping);
    }

    @GetMapping("/connections/{mappingId}/accept")
    public ResponseEntity<?> acceptInvitation(@PathVariable String mappingId) {
        StartupInvestorMapping mapping = connectionService.acceptInvitation(mappingId);
        return ResponseEntity.ok(mapping);
    }

    // PUBLIC: investor rejects founder invite (delete + email counterparty)
    @GetMapping("/connections/{mappingId}/reject")
    public ResponseEntity<?> rejectInvitation(@PathVariable String mappingId) {
        connectionService.rejectByInvestor(mappingId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Invitation rejected and removed successfully."
        ));
    }
}