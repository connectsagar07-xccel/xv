package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dao.RefreshTokenRepository;
import com.logicleaf.invplatform.model.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration-ms:2592000000}") // default 30 days
    private long refreshExpirationMs;

    public Mono<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public Mono<RefreshToken> createRefreshToken(String userId) {
        return refreshTokenRepository.deleteByUserId(userId)
                .then(Mono.defer(() -> {
                    RefreshToken token = RefreshToken.builder()
                            .userId(userId)
                            .token(UUID.randomUUID().toString())
                            .expiryDate(Instant.now().plusMillis(refreshExpirationMs))
                            .build();
                    return refreshTokenRepository.save(token);
                }));
    }

    public boolean isTokenValid(RefreshToken token) {
        return token != null && token.getExpiryDate().isAfter(Instant.now());
    }

    public Mono<Void> deleteByUserId(String userId) {
        return refreshTokenRepository.deleteByUserId(userId);
    }
}