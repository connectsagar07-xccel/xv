package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.OAuthToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZohoService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${zoho.client-id}")
    private String clientId;

    @Value("${zoho.client-secret}")
    private String clientSecret;

    @Value("${zoho.token-url}")
    private String tokenUrl;

    @Value("${zoho.redirect-uri}")
    private String redirectUri;

    // keep the token in-memory for MVP (in real world: store in DB)
    private OAuthToken currentToken;

    public String exchangeCodeForToken(String code, String accountsServer) {
        String endpoint = deriveTokenEndpoint(accountsServer, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&code=" + code;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> resp = restTemplate.exchange(endpoint, HttpMethod.POST, entity, Map.class);

        Map<String, Object> map = resp.getBody();
        String accessToken = (String) map.get("access_token");
        String refreshToken = (String) map.get("refresh_token");
        String apiDomain = (String) map.get("api_domain");
        Number expiresNum = (Number) map.get("expires_in");

        currentToken = OAuthToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .apiDomain(apiDomain)
                .expiresAt(Instant.now().plusSeconds(expiresNum != null ? expiresNum.longValue() : 3600L))
                .build();

        return "OAuth successful! Token acquired.";
    }

    public String fetchSalesOrders() {
        ensureValidToken();

        String url = currentToken.getApiDomain() + "/books/v3/salesorders?organization_id=60047867322";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentToken.getAccessToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return resp.getBody();
    }

    private void ensureValidToken() {
        if (currentToken == null || currentToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Token missing or expired. Please authorize again.");
        }
    }

    private String deriveTokenEndpoint(String accountsServer, String apiDomain) {
        if (accountsServer != null && !accountsServer.isBlank()) {
            return accountsServer + (accountsServer.endsWith("/") ? "oauth/v2/token" : "/oauth/v2/token");
        }
        if (apiDomain != null && apiDomain.contains(".zohoapis.in")) {
            return "https://accounts.zoho.in/oauth/v2/token";
        }
        return tokenUrl; // fallback from application.properties
    }
}