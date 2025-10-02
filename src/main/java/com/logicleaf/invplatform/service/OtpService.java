package com.logicleaf.invplatform.service;

import org.springframework.stereotype.Service;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendOtp(String recipient, String otp) {
        // Mock implementation: Log the OTP to the console
        logger.info("Sending OTP {} to {}", otp, recipient);
    }
}