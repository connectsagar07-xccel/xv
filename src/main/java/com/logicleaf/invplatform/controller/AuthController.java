package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dao.RefreshTokenRepository;
import com.logicleaf.invplatform.dao.StartupRepository;
import com.logicleaf.invplatform.dao.UserRepository;
import com.logicleaf.invplatform.dto.FounderOnboardRequest;
import com.logicleaf.invplatform.dto.LoginRequest;
import com.logicleaf.invplatform.dto.SignupRequest;
import com.logicleaf.invplatform.dto.VerifyOtpRequest;
import com.logicleaf.invplatform.model.RefreshToken;
import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.security.JwtService;
import com.logicleaf.invplatform.service.RefreshTokenService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StartupRepository startupRepository;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        // basic validations
        if (req.getEmail() == null || req.getPassword() == null || req.getName() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, email and password are required"));
        }

        if (userRepository.existsByEmail(req.getEmail()).block()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already taken"));
        }

        String usernameCandidate = deriveUsernameFromEmail(req.getEmail());
        // ensure username uniqueness; if exists append random suffix
        if (userRepository.existsByUsername(usernameCandidate).block()) {
            usernameCandidate = usernameCandidate + new Random().nextInt(999);
        }

        // Generate OTP (mock) - 6 digit
        String otp = String.format("%06d", new Random().nextInt(1_000_000));
        Instant expiry = Instant.now().plus(10, ChronoUnit.MINUTES);

        User user = User.builder()
                .username(usernameCandidate)
                .name(req.getName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of(req.getRole() == null ? "FOUNDER" : req.getRole().name()))
                .otp(otp)
                .otpExpiry(expiry)
                .otpVerified(false)
                .build();

        User saved = userRepository.save(user).block();

        // MOCK: send OTP (in real app call email/SMS provider)
        System.out.println("Signup OTP for " + saved.getEmail() + " = " + otp + " (expires at " + expiry + ")");

        return ResponseEntity.ok(Map.of(
                "message", "Signup successful. OTP sent to email/phone. Verify using /api/auth/verify-otp",
                "email", saved.getEmail()
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest req) {
        if (req.getEmail() == null || req.getOtp() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and otp required"));
        }

        User user = userRepository.findByEmail(req.getEmail()).block();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        if (user.isOtpVerified()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already verified"));
        }

        if (user.getOtp() == null || user.getOtpExpiry() == null || Instant.now().isAfter(user.getOtpExpiry())) {
            return ResponseEntity.badRequest().body(Map.of("error", "OTP expired or missing; request a new OTP"));
        }

        if (!user.getOtp().equals(req.getOtp())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid OTP"));
        }

        user.setOtpVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user).block();

        return ResponseEntity.ok(Map.of("message", "OTP verified successfully. You can now login."));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (req.getEmail() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password required"));
        }

        User user = userRepository.findByEmail(req.getEmail()).block();
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        if (!user.isOtpVerified()) {
            return ResponseEntity.status(403).body(Map.of("error", "OTP not verified"));
        }

        String accessToken = jwtService.generateToken(user.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken(),
                "role", user.getRoles()
        ));
    }

    @PostMapping("/onboard/founder")
    public ResponseEntity<?> onboardFounder(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                            @RequestBody FounderOnboardRequest req) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing Authorization header"));
        }
        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception ex) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
        }

        User user = userRepository.findByUsername(username).block();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        // Only founders can call this
        if (!user.getRoles().contains("FOUNDER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Only founders can create startup profile"));
        }

        Startup startup = Startup.builder()
                .founderId(user.getId())
                .startupName(req.getStartupName())
                .sector(req.getSector())
                .stage(req.getStage())
                .fundingRaised(req.getFundingRaised() == null ? null : req.getFundingRaised())
                .hqLocation(req.getHqLocation())
                .teamSize(req.getTeamSize())
                .website(req.getWebsite())
                .status("PROFILE_COMPLETED")
                .build();

        Startup saved = startupRepository.save(startup).block();

        return ResponseEntity.ok(Map.of("message", "Startup profile created", "startupId", saved.getId()));
    }

    private String deriveUsernameFromEmail(String email) {
        if (email == null) return "user" + new Random().nextInt(99999);
        String[] parts = email.split("@");
        return parts.length > 0 ? parts[0] : email;
    }

}
