package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.DuplicateEmailException;
import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthServiceImpl authService;

    private SignUpRequest signUpRequest;
    private User user;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest("Test User", "test@example.com", "1234567890", "password");
        user = User.builder()
                .id("1")
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.FOUNDER)
                .otpCode("123456")
                .otpExpiry(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();
    }

    @Test
    void signup_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(otpService.generateOtp()).thenReturn("123456");

        User result = authService.signup(signUpRequest, Role.FOUNDER);

        assertNotNull(result);
        assertEquals(signUpRequest.getEmail(), result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(otpService).sendOtp(anyString(), anyString());
    }

    @Test
    void signup_DuplicateEmail_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        assertThrows(DuplicateEmailException.class, () -> authService.signup(signUpRequest, Role.FOUNDER));
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        user.setVerified(true);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any(User.class))).thenReturn("test-token");
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(new RefreshToken("rt", "1", "test-refresh-token", null));

        JwtAuthenticationResponse response = authService.login(loginRequest);

        assertEquals("test-token", response.getToken());
        assertEquals("test-refresh-token", response.getRefreshToken());
    }

    @Test
    void login_UserNotVerified_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        user.setVerified(false);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(IllegalStateException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest));
    }

    @Test
    void verifyOtp_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        boolean result = authService.verifyOtp(new VerifyOtpRequest("test@example.com", "123456"));
        assertTrue(result);
        assertTrue(user.isVerified());
        assertNull(user.getOtpCode());
        verify(userRepository).save(user);
    }

    @Test
    void verifyOtp_InvalidOtp() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        boolean result = authService.verifyOtp(new VerifyOtpRequest("test@example.com", "wrong-otp"));
        assertFalse(result);
        assertFalse(user.isVerified());
    }

    @Test
    void verifyOtp_ExpiredOtp() {
        user.setOtpExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        boolean result = authService.verifyOtp(new VerifyOtpRequest("test@example.com", "123456"));
        assertFalse(result);
        assertFalse(user.isVerified());
    }
}