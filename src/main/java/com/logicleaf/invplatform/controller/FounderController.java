package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.InviteInvestorRequest;
import com.logicleaf.invplatform.model.StartupInvestorMapping;
import com.logicleaf.invplatform.service.ConnectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/founder")
@PreAuthorize("hasRole('FOUNDER')")
public class FounderController {

    @Autowired
    private ConnectionService connectionService;

    @PostMapping("/connections/invite")
    public ResponseEntity<?> inviteInvestor(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody InviteInvestorRequest inviteRequest) {
        try {
            StartupInvestorMapping mapping = connectionService.inviteInvestor(
                    userDetails.getUsername(),
                    inviteRequest.getInvestorEmail()
            );
            return ResponseEntity.ok(mapping);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/connections/{mappingId}/approve")
    public ResponseEntity<?> approveConnection(@AuthenticationPrincipal UserDetails userDetails,
                                               @PathVariable String mappingId) {
        try {
            StartupInvestorMapping mapping = connectionService.approveConnection(
                    userDetails.getUsername(),
                    mappingId
            );
            return ResponseEntity.ok(mapping);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}