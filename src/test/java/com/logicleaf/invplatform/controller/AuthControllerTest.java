package com.logicleaf.invplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logicleaf.invplatform.exception.DuplicateEmailException;
import com.logicleaf.invplatform.model.JwtAuthenticationResponse;
import com.logicleaf.invplatform.model.LoginRequest;
import com.logicleaf.invplatform.model.Role;
import com.logicleaf.invplatform.model.SignUpRequest;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.model.VerifyOtpRequest;
import com.logicleaf.invplatform.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private SignUpRequest signUpRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest("Test User", "test@example.com", "1234567890", "password");
        loginRequest = new LoginRequest("test@example.com", "password");
    }

    @Test
    void testSignupFounder_Success() throws Exception {
        User user = User.builder().name("Test User").email("test@example.com").verified(false).build();
        when(authService.signup(any(SignUpRequest.class), any(Role.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/signup/founder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Founder registered successfully. Please check your email for OTP."));
    }

    @Test
    void testSignupFounder_DuplicateEmail() throws Exception {
        when(authService.signup(any(SignUpRequest.class), any(Role.class)))
                .thenThrow(new DuplicateEmailException("Email address test@example.com is already in use."));

        mockMvc.perform(post("/api/auth/signup/founder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email address test@example.com is already in use."));
    }


    @Test
    void testLogin_Success() throws Exception {
        JwtAuthenticationResponse response = new JwtAuthenticationResponse("test-token", "test-refresh-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-token"))
                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"));
    }

    @Test
    void testLogin_Unverified() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new IllegalStateException("User account is not verified. Please verify your OTP."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User account is not verified. Please verify your OTP."));
    }


    @Test
    void testVerifyOtp_Success() throws Exception {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest("test@example.com", "123456");
        when(authService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("OTP verified successfully. You can now log in."));
    }

    @Test
    void testVerifyOtp_Failure() throws Exception {
        VerifyOtpRequest verifyOtpRequest = new VerifyOtpRequest("test@example.com", "wrong-otp");
        when(authService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyOtpRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid or expired OTP."));
    }
}