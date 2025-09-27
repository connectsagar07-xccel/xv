package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.OAuthToken;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Responsible for talking to Zoho accounts endpoint to exchange code or refresh tokens.
 */
@Service
@RequiredArgsConstructor
public class ZohoTokenService {

    private static final Logger log = LoggerFactory.getLogger(ZohoTokenService.class);

    private final WebClient.Builder webClientBuilder;

    @Value("${zoho.client-id}")
    private String clientId;

    @Value("${zoho.client-secret}")
    private String clientSecret;

    /**
     * Default token URL from properties (may be accounts.zoho.com). We will override
     * for .in region based on callback 'accounts-server' or saved api_domain.
     */
    @Value("${zoho.token-url}")
    private String tokenUrl;

    @Value("${zoho.redirect-uri}")
    private String redirectUri;

    /**
     * Exchange authorization code for tokens. If Zoho redirected with accounts-server param (like https://accounts.zoho.in),
     * pass that value in accountsServer (exact origin) and we will use accountsServer + /oauth/v2/token as token endpoint.
     *
     * @param code           authorization code from callback
     * @param accountsServer optional (eg "https://accounts.zoho.in")
     * @return Mono<OAuthToken>
     */
    @SuppressWarnings("unchecked")
    public Mono<OAuthToken> exchangeCodeForToken(String code, String accountsServer) {
        String tokenEndpoint = deriveTokenEndpoint(accountsServer, null);

        return webClientBuilder.build()
                .post()
                .uri(tokenEndpoint)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("code", code))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    log.info("Zoho token response: {}", resp);

                    String accessToken = (String) resp.get("access_token");
                    String refreshToken = (String) resp.get("refresh_token");
                    String apiDomain = (String) resp.get("api_domain");
                    Number expiresNum = (Number) resp.get("expires_in");
                    long expiresIn = expiresNum != null ? expiresNum.longValue() : 3600L;

                    // If apiDomain missing, fallback to default books base (without /books/v3)
                    if (apiDomain == null || apiDomain.isBlank()) {
                        // tokenEndpoint might be accounts.* - but as fallback keep .zohoapis.com
                        apiDomain = tokenUrl.contains(".zoho.in") ? "https://www.zohoapis.in" : "https://www.zohoapis.com";
                    }

                    return OAuthToken.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .apiDomain(apiDomain)
                            .expiresAt(Instant.now().plusSeconds(expiresIn))
                            .build();
                });
    }

    /**
     * Refresh access token using the given refresh token. We will select accounts token endpoint
     * based on apiDomain (if apiDomain contains ".zohoapis.in" we use accounts.zoho.in).
     *
     * @param refreshToken existing refresh token
     * @param apiDomain    previously saved apiDomain (helps choose region)
     * @return Mono<OAuthToken>
     */
    @SuppressWarnings("unchecked")
    public Mono<OAuthToken> refreshAccessToken(String refreshToken, String apiDomain) {
        String tokenEndpoint = deriveTokenEndpoint(null, apiDomain);

        return webClientBuilder.build()
                .post()
                .uri(tokenEndpoint)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(Map.class)
                .map(resp -> {
                    log.info("Zoho refresh response: {}", resp);

                    String accessToken = (String) resp.get("access_token");
                    // Zoho sometimes returns a new refresh_token on refresh, sometimes not
                    String newRefresh = (String) resp.get("refresh_token");
                    String newApiDomain = (String) resp.get("api_domain");
                    Number expiresNum = (Number) resp.get("expires_in");
                    long expiresIn = expiresNum != null ? expiresNum.longValue() : 3600L;

                    String finalRefreshToken = (newRefresh != null) ? newRefresh : refreshToken;
                    String finalApiDomain = (newApiDomain != null && !newApiDomain.isBlank()) ? newApiDomain : apiDomain;

                    return OAuthToken.builder()
                            .accessToken(accessToken)
                            .refreshToken(finalRefreshToken)
                            .apiDomain(finalApiDomain)
                            .expiresAt(Instant.now().plusSeconds(expiresIn))
                            .build();
                });
    }

    /**
     * Helper: if accountsServer is provided (from callback) we use it, otherwise derive from apiDomain
     * or fallback to tokenUrl property.
     */
    private String deriveTokenEndpoint(String accountsServer, String apiDomain) {
        if (accountsServer != null && !accountsServer.isBlank()) {
            return accountsServer.endsWith("/") ?
                    accountsServer + "oauth/v2/token" : accountsServer + "/oauth/v2/token";
        }

        if (apiDomain != null && apiDomain.contains(".zohoapis.in")) {
            return "https://accounts.zoho.in/oauth/v2/token";
        }

        // fallback to configured tokenUrl (e.g., https://accounts.zoho.com/oauth/v2/token)
        return tokenUrl;
    }
}
