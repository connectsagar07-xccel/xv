package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dao.RefreshTokenRepository;
import com.logicleaf.invplatform.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms:2592000000}") // default 30 days
    private long refreshExpirationMs;

    /**
     * Create refresh token (delete existing for userId first).
     * Blocks briefly to keep imperative flow.
     */
    public RefreshToken createRefreshToken(String userId) {
        // delete existing tokens for this user
        refreshTokenRepository.deleteByUserId(userId).block();

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                .build();

        return refreshTokenRepository.save(token).block();
    }

    public boolean validateRefreshToken(RefreshToken token) {
        return token != null && token.getExpiryDate().isAfter(Instant.now());
    }

    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId).block();
    }
}