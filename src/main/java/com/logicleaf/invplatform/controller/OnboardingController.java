package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dao.InvestorRepository;
import com.logicleaf.invplatform.dao.StartupRepository;
import com.logicleaf.invplatform.dao.UserRepository;
import com.logicleaf.invplatform.model.Investor;
import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final InvestorRepository investorRepository;

    @PostMapping("/founder/profile")
    @PreAuthorize("hasRole('FOUNDER')")
    public Mono<ResponseEntity<Map<String, String>>> createFounderProfile(@Valid @RequestBody FounderProfileRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .flatMap(user -> {
                    Startup startup = Startup.builder()
                            .userId(user.getId())
                            .companyName(request.getCompanyName())
                            .startupName(request.getStartupName())
                            .sector(request.getSector())
                            .stage(request.getStage())
                            .fundingRaised(request.getFundingRaised())
                            .hqLocation(request.getHqLocation())
                            .teamSize(request.getTeamSize())
                            .website(request.getWebsite())
                            .status("Profile Completed")
                            .build();

                    return startupRepository.save(startup)
                            .then(updateProfileCompletedStatus(user))
                            .thenReturn(ResponseEntity.ok(Map.of("message", "Founder profile created successfully.")));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"))));
    }

    @PostMapping("/investor/profile")
    @PreAuthorize("hasRole('INVESTOR')")
    public Mono<ResponseEntity<Map<String, String>>> createInvestorProfile(@Valid @RequestBody InvestorProfileRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .flatMap(user -> {
                    Investor investor = Investor.builder()
                            .userId(user.getId())
                            .investorType(request.getInvestorType())
                            .firmName(request.getFirmName())
                            .ticketSize(request.getTicketSize())
                            .sectorFocus(request.getSectorFocus())
                            .aum(request.getAum())
                            .build();

                    return investorRepository.save(investor)
                            .then(updateProfileCompletedStatus(user))
                            .thenReturn(ResponseEntity.ok(Map.of("message", "Investor profile created successfully.")));
                })
                .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"))));
    }

    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Boolean>>> getOnboardingStatus(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> ResponseEntity.ok(Map.of("profileCompleted", user.isProfileCompleted())))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    private Mono<User> updateProfileCompletedStatus(User user) {
        user.setProfileCompleted(true);
        return userRepository.save(user);
    }

    @Data
    public static class FounderProfileRequest {
        @NotEmpty
        private String companyName;
        @NotEmpty
        private String startupName;
        @NotEmpty
        private String sector;
        @NotEmpty
        private String stage;
        @NotEmpty
        private String fundingRaised;
        @NotEmpty
        private String hqLocation;
        private int teamSize;
        @NotEmpty
        private String website;
    }

    @Data
    public static class InvestorProfileRequest {
        @NotEmpty
        private String investorType;
        @NotEmpty
        private String firmName;
        @NotEmpty
        private String ticketSize;
        @NotEmpty
        private String sectorFocus;
        private String aum; // Optional
    }
}