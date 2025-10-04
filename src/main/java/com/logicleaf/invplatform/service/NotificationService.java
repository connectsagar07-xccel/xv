package com.logicleaf.invplatform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    public void sendOtp(String email, String otp) {
        // In a real application, this would use an email or SMS service.
        // For this MVP, we'll just log it to the console.
        logger.info("Sending OTP to {}: {}", email, otp);
    }
}