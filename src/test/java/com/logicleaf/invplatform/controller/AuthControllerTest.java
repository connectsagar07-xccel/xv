package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.model.RefreshToken;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.security.JwtService;
import com.logicleaf.invplatform.config.SecurityConfig;
import com.logicleaf.invplatform.config.UserDetailsServiceImpl;
import com.logicleaf.invplatform.model.RefreshToken;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.security.JwtService;
import com.logicleaf.invplatform.service.AuthService;
import com.logicleaf.invplatform.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
@Import({SecurityConfig.class, JwtService.class})
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthService authService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private com.logicleaf.invplatform.dao.UserRepository userRepository;

    @MockBean
    private com.logicleaf.invplatform.dao.RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void signupFounder_Success() {
        User user = new User();
        user.setEmail("founder@example.com");

        when(authService.registerUser(any(), anyString())).thenReturn(Mono.just(user));

        webTestClient.post().uri("/api/auth/signup/founder")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"test\",\"email\":\"founder@example.com\",\"password\":\"pass\",\"phoneNumber\":\"123\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("OTP has been sent to your email.");
    }

    @Test
    void verifyOtp_Success() {
        User user = User.builder().id("1").email("test@example.com").build();
        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).build();

        when(authService.verifyOtp(anyString(), anyString())).thenReturn(Mono.just(user));
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(Mono.just(refreshToken));

        webTestClient.post().uri("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"email\":\"test@example.com\",\"code\":\"123456\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty();
    }

    @Test
    void login_Success() {
        User user = User.builder()
                .id("1")
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .isVerified(true)
                .role("FOUNDER")
                .build();
        RefreshToken refreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).build();

        when(userDetailsService.findByUsername("test@example.com")).thenReturn(Mono.just(user));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Mono.just(user));
        when(refreshTokenService.createRefreshToken("1")).thenReturn(Mono.just(refreshToken));

        webTestClient.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"email\":\"test@example.com\",\"password\":\"password\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty()
                .jsonPath("$.refreshToken").isNotEmpty();
    }

    @Test
    void refresh_Success() {
        User user = User.builder().id("1").email("test@example.com").build();
        RefreshToken refreshToken = RefreshToken.builder()
                .userId("1")
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(100000))
                .build();

        when(refreshTokenService.findByToken(anyString())).thenReturn(Mono.just(refreshToken));
        when(refreshTokenService.isTokenValid(any())).thenReturn(true);
        when(userRepository.findById("1")).thenReturn(Mono.just(user));

        webTestClient.post().uri("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"refreshToken\":\"" + refreshToken.getToken() + "\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").isNotEmpty();
    }
}