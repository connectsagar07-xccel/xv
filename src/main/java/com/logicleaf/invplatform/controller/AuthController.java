package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dao.RefreshTokenRepository;
import com.logicleaf.invplatform.dao.UserRepository;
import com.logicleaf.invplatform.model.RefreshToken;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.service.JwtService;
import com.logicleaf.invplatform.service.RefreshTokenService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername()).block()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }
        if (userRepository.existsByEmail(request.getEmail()).block()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already taken"));
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of("USER"))
                .build();

        User saved = userRepository.save(user).block();

        String accessToken = jwtService.generateToken(saved.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(saved.getId());

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken.getToken()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).block();
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        String accessToken = jwtService.generateToken(user.getUsername());
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
        String newAccess = jwtService.generateToken(user.getUsername());
        return ResponseEntity.ok(Map.of("accessToken", newAccess));
    }

    @Data
    public static class SignupRequest {
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class RefreshRequest {
        private String refreshToken;
    }
}
