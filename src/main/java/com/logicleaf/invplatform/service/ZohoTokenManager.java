package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dao.OAuthTokenRepository;
import com.logicleaf.invplatform.model.OAuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * Central source of truth for tokens. Ensures refresh when expired and persists tokens.
 */
@Service
@RequiredArgsConstructor
public class ZohoTokenManager {

    private final OAuthTokenRepository tokenRepository;
    private final ZohoTokenService tokenService;

    /**
     * Return a valid OAuthToken (refreshing if needed). If no token exists -> returns error (Mono.error).
     */
    public Mono<OAuthToken> getToken() {
        return tokenRepository.findAll().next()
                .switchIfEmpty(Mono.error(new IllegalStateException("No OAuth token found. Call /api/zoho/authorize and complete the flow.")))
                .flatMap(token -> {
                    // if still valid for at least 60 seconds
                    if (token.getExpiresAt() != null && token.getExpiresAt().isAfter(Instant.now().plusSeconds(60))) {
                        return Mono.just(token);
                    }
                    // otherwise refresh
                    if (token.getRefreshToken() == null) {
                        return Mono.error(new IllegalStateException("No refresh token available; re-authorize."));
                    }
                    return tokenService.refreshAccessToken(token.getRefreshToken(), token.getApiDomain())
                            .flatMap(newToken -> tokenRepository.save(newToken));
                });
    }

    /**
     * Save initial token received from code exchange. We delete any old tokens to keep single record.
     */
    public Mono<OAuthToken> saveInitialToken(OAuthToken token) {
        // keep one document only
        return tokenRepository.deleteAll()
                .then(tokenRepository.save(token));
    }
}
