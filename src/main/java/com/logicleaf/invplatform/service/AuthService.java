package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dao.UserRepository;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.controller.AuthController.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final long OTP_EXPIRATION_MINUTES = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Mono<User> registerUser(SignUpRequest request, String role) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isVerified(false)
                .profileCompleted(false)
                .build();

        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException("Email already taken"));
                    }
                    return userRepository.save(user).flatMap(this::generateAndSaveOtp);
                });
    }

    private Mono<User> generateAndSaveOtp(User user) {
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));
        user.setOtpCode(code);
        user.setOtpExpiry(Instant.now().plusSeconds(OTP_EXPIRATION_MINUTES * 60));
        log.info("Generated OTP {} for user {}", code, user.getEmail());
        // In a real application, you would send the OTP via email/SMS here.
        return userRepository.save(user);
    }

    public Mono<User> verifyOtp(String email, String code) {
        return userRepository.findByEmail(email)
                .flatMap(user -> {
                    if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(Instant.now())) {
                        return Mono.error(new IllegalArgumentException("OTP expired."));
                    }
                    if (!code.equals(user.getOtpCode())) {
                        return Mono.error(new IllegalArgumentException("Invalid OTP."));
                    }
                    user.setVerified(true);
                    user.setOtpCode(null);
                    user.setOtpExpiry(null);
                    return userRepository.save(user);
                })
                .switchIfEmpty(Mono.error(new IllegalArgumentException("User not found.")));
    }
}