package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.FounderProfileRequest;
import com.logicleaf.invplatform.dto.InvestorProfileRequest;
import com.logicleaf.invplatform.model.Investor;
import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    @Autowired
    private OnboardingService onboardingService;

    @PostMapping("/founder")
    @PreAuthorize("hasRole('FOUNDER')")
    public ResponseEntity<?> createFounderProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                  @Valid @RequestBody FounderProfileRequest request) {
        try {
            Startup startup = onboardingService.createFounderProfile(userDetails.getUsername(), request);
            return ResponseEntity.ok(startup);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/investor")
    @PreAuthorize("hasRole('INVESTOR')")
    public ResponseEntity<?> createInvestorProfile(@AuthenticationPrincipal UserDetails userDetails,
                                                   @Valid @RequestBody InvestorProfileRequest request) {
        try {
            Investor investor = onboardingService.createInvestorProfile(userDetails.getUsername(), request);
            return ResponseEntity.ok(investor);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}