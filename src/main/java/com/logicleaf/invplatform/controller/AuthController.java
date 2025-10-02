package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/founder")
    public ResponseEntity<String> signupFounder(@RequestBody SignUpRequest signUpRequest) {
        authService.signup(signUpRequest, Role.FOUNDER);
        return ResponseEntity.ok("Founder registered successfully. Please check your email for OTP.");
    }

    @PostMapping("/signup/investor")
    public ResponseEntity<String> signupInvestor(@RequestBody SignUpRequest signUpRequest) {
        authService.signup(signUpRequest, Role.INVESTOR);
        return ResponseEntity.ok("Investor registered successfully. Please check your email for OTP.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest verifyOtpRequest) {
        boolean isVerified = authService.verifyOtp(verifyOtpRequest);
        if (isVerified) {
            return ResponseEntity.ok("OTP verified successfully. You can now log in.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<JwtAuthenticationResponse> refreshToken(@RequestBody TokenRefreshRequest tokenRefreshRequest) {
        return authService.refreshToken(tokenRefreshRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }
}