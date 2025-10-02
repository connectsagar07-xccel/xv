package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.DuplicateEmailException;
import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;

    @Override
    public User signup(SignUpRequest signUpRequest, Role role) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Email address " + signUpRequest.getEmail() + " is already in use.");
        }

        String otp = otpService.generateOtp();
        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .phone(signUpRequest.getPhone())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(role)
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(10)) // OTP valid for 10 minutes
                .verified(false)
                .build();

        userRepository.save(user);
        otpService.sendOtp(user.getEmail(), otp);
        return user;
    }

    @Override
    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isVerified()) {
            throw new IllegalStateException("User account is not verified. Please verify your OTP.");
        }

        String jwt = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new JwtAuthenticationResponse(jwt, refreshToken.getToken());
    }

    @Override
    public Optional<JwtAuthenticationResponse> refreshToken(TokenRefreshRequest tokenRefreshRequest) {
        return refreshTokenService.findByToken(tokenRefreshRequest.getRefreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserId)
                .map(userId -> {
                    User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
                    String jwt = jwtService.generateToken(user);
                    return new JwtAuthenticationResponse(jwt, tokenRefreshRequest.getRefreshToken());
                });
    }

    @Override
    public boolean verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        User user = userRepository.findByEmail(verifyOtpRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getOtpCode() != null && user.getOtpCode().equals(verifyOtpRequest.getOtpCode()) &&
                user.getOtpExpiry().isAfter(LocalDateTime.now())) {
            user.setVerified(true);
            user.setOtpCode(null);
            user.setOtpExpiry(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}