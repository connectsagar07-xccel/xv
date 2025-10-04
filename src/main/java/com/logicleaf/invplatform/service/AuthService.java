package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dto.SignupRequest;
import com.logicleaf.invplatform.model.Role;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {


    @Autowired
    private UserRepository userRepository;

    // Sign Up
    public String signup(SignupRequest request) {
        // Generate OTP
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(request.getPassword())
                .role(Role.FOUNDER)
                .otp(otp)
                .otpVerified(false)
                .build();

        userRepository.save(user);

        //TODO: Send OTP via Email/SMS API
        return "User registered. OTP sent: " + otp;
    }

    // Verify OTP
    public String verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp().equals(otp)) {
            user.setOtpVerified(true);
            user.setOtp(null);
            userRepository.save(user);
            return "OTP verified. Account activated!";
        } else {
            return "Invalid OTP";
        }
    }
}
