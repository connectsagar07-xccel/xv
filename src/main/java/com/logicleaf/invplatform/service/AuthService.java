package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dto.*;
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
import java.util.Optional;

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
        Optional<User> existingUserOpt = userRepository.findByEmail(signUpRequest.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (existingUser.isVerified()) {
                throw new RuntimeException("Email address already in use.");
            } else {
                // Overwrite unverified user's data and resend OTP
                existingUser.setName(signUpRequest.getName());
                existingUser.setPhone(signUpRequest.getPhone());
                existingUser.setPasswordHash(passwordEncoder.encode(signUpRequest.getPassword()));
                existingUser.setRole(signUpRequest.getRole());

                String otp = generateOtp();
                existingUser.setOtp(otp);
                existingUser.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
                existingUser = userRepository.save(existingUser);

                notificationService.sendOtp(existingUser.getEmail(), otp);
                throw new RuntimeException("Verification pending. New OTP sent to your email.");
            }
        }

        // New user registration
        User newUser = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .phone(signUpRequest.getPhone())
                .passwordHash(passwordEncoder.encode(signUpRequest.getPassword()))
                .role(signUpRequest.getRole())
                .isVerified(false)
                .build();

        String otp = generateOtp();
        newUser.setOtp(otp);
        newUser.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        newUser = userRepository.save(newUser);

        notificationService.sendOtp(newUser.getEmail(), otp);
        return newUser;
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

    public void resendOtp(ResendOtpRequest resendOtpRequest) {
        User user = userRepository.findByEmail(resendOtpRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found."));

        if (user.isVerified()) {
            throw new RuntimeException("User is already verified.");
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);
        notificationService.sendOtp(user.getEmail(), otp);
    }

    public LoginResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        TokenResponse tokenResponse = new TokenResponse(jwt);
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.isVerified());

        return new LoginResponse("success", "Login successful.", tokenResponse, userResponse);
    }

    private String generateOtp() {
        // Generate a 6-digit OTP
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(999999);
        return String.format("%06d", num);
    }
}
