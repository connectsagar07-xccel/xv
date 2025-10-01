package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dao.UserRepository;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.controller.AuthController.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private SignUpRequest signUpRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
        signUpRequest.setName("Test User");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password");
        signUpRequest.setPhoneNumber("1234567890");

        user = User.builder()
                .id("1")
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role("FOUNDER")
                .isVerified(false)
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.existsByEmail(any(String.class))).thenReturn(Mono.just(false));
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user));

        StepVerifier.create(authService.registerUser(signUpRequest, "FOUNDER"))
                .expectNextMatches(savedUser ->
                        savedUser.getEmail().equals(signUpRequest.getEmail()) &&
                        savedUser.getOtpCode() != null &&
                        savedUser.getOtpExpiry() != null
                )
                .verifyComplete();
    }

    @Test
    void registerUser_EmailAlreadyTaken() {
        when(userRepository.existsByEmail(any(String.class))).thenReturn(Mono.just(true));

        StepVerifier.create(authService.registerUser(signUpRequest, "FOUNDER"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void verifyOtp_Success() {
        user.setOtpCode("123456");
        user.setOtpExpiry(Instant.now().plusSeconds(600));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(authService.verifyOtp("test@example.com", "123456"))
                .expectNextMatches(verifiedUser ->
                        verifiedUser.isVerified() &&
                        verifiedUser.getOtpCode() == null &&
                        verifiedUser.getOtpExpiry() == null
                )
                .verifyComplete();
    }

    @Test
    void verifyOtp_InvalidCode() {
        user.setOtpCode("123456");
        user.setOtpExpiry(Instant.now().plusSeconds(600));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(user));

        StepVerifier.create(authService.verifyOtp("test@example.com", "654321"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void verifyOtp_Expired() {
        user.setOtpCode("123456");
        user.setOtpExpiry(Instant.now().minusSeconds(600));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(user));

        StepVerifier.create(authService.verifyOtp("test@example.com", "123456"))
                .expectError(IllegalArgumentException.class)
                .verify();
    }
}