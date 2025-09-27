package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.model.OAuthToken;
import com.logicleaf.invplatform.service.ZohoService;
import com.logicleaf.invplatform.service.ZohoTokenManager;
import com.logicleaf.invplatform.service.ZohoTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/zoho")
@RequiredArgsConstructor
public class ZohoController {

    private final ZohoService zohoService;
    private final ZohoTokenService tokenService;
    private final ZohoTokenManager tokenManager;

    @Value("${zoho.client-id}")
    private String clientId;

    @Value("${zoho.redirect-uri}")
    private String redirectUri;

    @Value("${zoho.scope}")
    private String scope;

    @Value("${zoho.auth-url}")
    private String authUrl;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        String url = authUrl + "?scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8)
                + "&client_id=" + clientId
                + "&response_type=code"
                + "&access_type=offline"
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        response.sendRedirect(url);
    }

    /**
     * Callback handler receives 'code'. Zoho may also append 'accounts-server' (like https://accounts.zoho.in).
     * We pass that to the token service so it can select the correct token endpoint.
     */
    @GetMapping("/callback")
    public Mono<String> callback(@RequestParam String code,
                                 @RequestParam(value = "accounts-server", required = false) String accountsServer) {
        return tokenService.exchangeCodeForToken(code, accountsServer)
                .flatMap(tokenManager::saveInitialToken)
                .map(saved -> "OAuth successful! Tokens saved.")
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just("Error during token exchange: " + e.getMessage());
                });
    }

    @GetMapping("/salesorders")
    public Mono<?> getSalesOrders(@RequestParam int year) {
        return zohoService.fetchZohoSalesOrders(year);
    }
}
