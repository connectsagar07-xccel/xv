package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.model.FounderProfileRequest;
import com.logicleaf.invplatform.model.InvestorProfileRequest;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.repository.UserRepository;
import com.logicleaf.invplatform.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final UserRepository userRepository;

    @PostMapping("/founder")
    public ResponseEntity<String> createFounderProfile(@RequestBody FounderProfileRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        onboardingService.createFounderProfile(user.getId(), request);
        return ResponseEntity.ok("Founder profile created successfully.");
    }

    @PostMapping("/investor")
    public ResponseEntity<String> createInvestorProfile(@RequestBody InvestorProfileRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        onboardingService.createInvestorProfile(user.getId(), request);
        return ResponseEntity.ok("Investor profile created successfully.");
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}