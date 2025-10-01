package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dao.RefreshTokenRepository;
import com.logicleaf.invplatform.dao.UserRepository;
import com.logicleaf.invplatform.model.RefreshToken;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.security.JwtService;
import com.logicleaf.invplatform.service.AuthService;
import com.logicleaf.invplatform.service.RefreshTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final AuthService authService;

    @PostMapping("/signup/founder")
    public Mono<ResponseEntity<Map<String, String>>> signupFounder(@Valid @RequestBody SignUpRequest request) {
        return signup(request, "FOUNDER");
    }

    @PostMapping("/signup/investor")
    public Mono<ResponseEntity<Map<String, String>>> signupInvestor(@Valid @RequestBody SignUpRequest request) {
        return signup(request, "INVESTOR");
    }

    private Mono<ResponseEntity<Map<String, String>>> signup(SignUpRequest request, String role) {
        return authService.registerUser(request, role)
                .map(user -> ResponseEntity.ok(Map.of("message", "OTP has been sent to your email.")))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))));
    }

    @PostMapping("/verify-otp")
    public Mono<ResponseEntity<Map<String, String>>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        return authService.verifyOtp(request.getEmail(), request.getCode())
                .flatMap(verifiedUser -> {
                    String accessToken = jwtService.generateToken(verifiedUser.getEmail());
                    return refreshTokenService.createRefreshToken(verifiedUser.getId())
                            .map(refreshToken -> ResponseEntity.ok(Map.of(
                                    "accessToken", accessToken,
                                    "refreshToken", refreshToken.getToken()
                            )));
                })
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .flatMap(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials")));
                    }
                    if (!user.isVerified()) {
                        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Account not verified. Please complete the OTP verification.")));
                    }

                    String accessToken = jwtService.generateToken(user.getEmail());
                    return refreshTokenService.createRefreshToken(user.getId())
                            .map(refreshToken -> ResponseEntity.ok(Map.of(
                                    "accessToken", accessToken,
                                    "refreshToken", refreshToken.getToken()
                            )));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials")));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, String>>> refresh(@RequestBody RefreshRequest request) {
        return refreshTokenService.findByToken(request.getRefreshToken())
                .flatMap(refreshToken -> {
                    if (!refreshTokenService.isTokenValid(refreshToken)) {
                        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token")));
                    }
                    return userRepository.findById(refreshToken.getUserId())
                            .map(user -> {
                                String newAccessToken = jwtService.generateToken(user.getEmail());
                                return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
                            })
                            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found")));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token")));
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
