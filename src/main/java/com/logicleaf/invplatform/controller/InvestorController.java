package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.ConnectStartupRequest;
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
@RequestMapping("/api/investor")
@PreAuthorize("hasRole('INVESTOR')")
public class InvestorController {

    @Autowired
    private ConnectionService connectionService;

    @PostMapping("/connections/request")
    public ResponseEntity<?> requestConnection(@AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody ConnectStartupRequest request) {
        try {
            StartupInvestorMapping mapping = connectionService.requestConnection(
                    userDetails.getUsername(),
                    request.getStartupId()
            );
            return ResponseEntity.ok(mapping);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/connections/{mappingId}/accept")
    public ResponseEntity<?> acceptInvitation(@AuthenticationPrincipal UserDetails userDetails,
                                              @PathVariable String mappingId) {
        try {
            StartupInvestorMapping mapping = connectionService.acceptInvitation(
                    userDetails.getUsername(),
                    mappingId
            );
            return ResponseEntity.ok(mapping);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}