package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dto.JwtAuthenticationResponse;
import com.logicleaf.invplatform.dto.LoginRequest;
import com.logicleaf.invplatform.dto.SignUpRequest;
import com.logicleaf.invplatform.dto.VerifyOtpRequest;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.repository.UserRepository;
import com.logicleaf.invplatform.security.CustomUserDetails;
import com.logicleaf.invplatform.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private NotificationService notificationService;

    public User registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email address already in use.");
        }

        // Create new user's account
        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .phone(signUpRequest.getPhone())
                .passwordHash(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(signUpRequest.getRole())
                .isVerified(false)
                .build();

        // Generate and send OTP
        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10)); // OTP valid for 10 minutes

        user = userRepository.save(user);
        notificationService.sendOtp(user.getEmail(), otp);

        return user;
    }

    public boolean verifyOtp(VerifyOtpRequest verifyOtpRequest) {
        User user = userRepository.findByEmail(verifyOtpRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.isVerified()) {
            return true; // Already verified
        }

        if (user.getOtp().equals(verifyOtpRequest.getOtp()) && user.getOtpExpiryTime().isAfter(LocalDateTime.now())) {
            user.setVerified(true);
            user.setOtp(null); // Clear OTP after successful verification
            user.setOtpExpiryTime(null);
            userRepository.save(user);
            return true;
        }

        return false;
    }

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        return new JwtAuthenticationResponse(jwt, customUserDetails.getUser());
    }

    private String generateOtp() {
        // Generate a 6-digit OTP
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(999999);
        return String.format("%06d", num);
    }
}