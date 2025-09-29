package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dao.RefreshTokenRepository;
import com.logicleaf.invplatform.dao.UserRepository;
import com.logicleaf.invplatform.model.RefreshToken;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.security.JwtService;
import com.logicleaf.invplatform.service.OtpService;
import com.logicleaf.invplatform.service.RefreshTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OtpService otpService;

    @PostMapping("/signup/founder")
    public Mono<ResponseEntity<Map<String, String>>> signupFounder(@Valid @RequestBody SignUpRequest request) {
        return signup(request, "FOUNDER");
    }

    @PostMapping("/signup/investor")
    public Mono<ResponseEntity<Map<String, String>>> signupInvestor(@Valid @RequestBody SignUpRequest request) {
        return signup(request, "INVESTOR");
    }

    private Mono<ResponseEntity<Map<String, String>>> signup(SignUpRequest request, String role) {
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Email already taken")));
                    }

                    User user = User.builder()
                            .name(request.getName())
                            .email(request.getEmail())
                            .phoneNumber(request.getPhoneNumber())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .role(role)
                            .build(); // isVerified and profileCompleted default to false

                    return userRepository.save(user)
                            .then(otpService.generateAndSendOtp(user.getEmail()))
                            .thenReturn(ResponseEntity.ok(Map.of("message", "OTP has been sent to your email.")));
                });
    }

    @PostMapping("/verify")
    public Mono<ResponseEntity<Map<String, String>>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return otpService.verifyOtp(request.getEmail(), request.getCode())
                .flatMap(isValid -> {
                    if (!isValid) {
                        return Mono.just(ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP.")));
                    }

                    return userRepository.findByEmail(request.getEmail())
                            .flatMap(user -> {
                                user.setVerified(true);
                                return userRepository.save(user);
                            })
                            .flatMap(savedUser -> {
                                String accessToken = jwtService.generateToken(savedUser.getEmail());
                                RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser.getId());
                                return Mono.just(ResponseEntity.ok(Map.of(
                                        "accessToken", accessToken,
                                        "refreshToken", refreshToken.getToken()
                                )));
                            })
                            .switchIfEmpty(Mono.just(ResponseEntity.status(404).body(Map.of("error", "User not found after OTP verification."))));
                });
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).block();
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        if (!user.isVerified()) {
            return ResponseEntity.status(403).body(Map.of("error", "Account not verified. Please complete the OTP verification."));
        }

        String accessToken = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken()).block();
        if (token == null || !refreshTokenService.validateRefreshToken(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
        }

        User user = userRepository.findById(token.getUserId()).block();
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        // create new access token (we could rotate refresh token if desired)
        String newAccess = jwtService.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of("accessToken", newAccess));
    }

    @Data
    public static class SignUpRequest {
        @NotEmpty
        private String name;
        @NotEmpty @Email
        private String email;
        @NotEmpty
        private String phoneNumber;
        @NotEmpty
        private String password;
    }

    @Data
    public static class OtpVerificationRequest {
        @NotEmpty @Email
        private String email;
        @NotEmpty
        private String code;
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }
}
