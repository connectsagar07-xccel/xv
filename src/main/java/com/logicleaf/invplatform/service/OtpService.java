package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dao.OtpRepository;
import com.logicleaf.invplatform.model.Otp;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final long OTP_EXPIRATION_MINUTES = 10;

    private final OtpRepository otpRepository;

    /**
     * Generates a new OTP, saves it, and simulates sending it.
     *
     * @param email The email to associate the OTP with.
     * @return A Mono that completes when the operation is done.
     */
    public Mono<Void> generateAndSendOtp(String email) {
        return otpRepository.deleteByEmail(email)
                .then(Mono.fromCallable(() -> {
                    String code = String.format("%06d", ThreadLocalRandom.current().nextInt(100000, 1000000));
                    return Otp.builder()
                            .email(email)
                            .code(code)
                            .expiryDate(Instant.now().plusSeconds(OTP_EXPIRATION_MINUTES * 60))
                            .build();
                }))
                .flatMap(otpRepository::save)
                .doOnSuccess(otp -> log.info("OTP for {}: {}", otp.getEmail(), otp.getCode()))
                .then();
    }

    /**
     * Verifies the provided OTP for the given email.
     *
     * @param email The user's email.
     * @param code  The OTP code to verify.
     * @return A Mono emitting true if valid, false otherwise.
     */
    public Mono<Boolean> verifyOtp(String email, String code) {
        return otpRepository.findByEmail(email)
                .flatMap(otp -> {
                    if (otp.getExpiryDate().isBefore(Instant.now())) {
                        // OTP expired, delete it
                        return otpRepository.deleteById(otp.getId()).thenReturn(false);
                    }
                    if (otp.getCode().equals(code)) {
                        // Valid, delete it to prevent reuse
                        return otpRepository.deleteById(otp.getId()).thenReturn(true);
                    }
                    // Invalid code
                    return Mono.just(false);
                })
                .defaultIfEmpty(false); // No OTP found
    }
}