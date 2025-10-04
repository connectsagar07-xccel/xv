package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.service.ZohoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/zoho")
@RequiredArgsConstructor
public class ZohoController {

    private final ZohoService zohoService;

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

    @GetMapping("/callback")
    public String callback(@RequestParam String code,
                           @RequestParam(value = "accounts-server", required = false) String accountsServer) {
        return zohoService.exchangeCodeForToken(code, accountsServer);
    }

    @GetMapping("/salesorders")
    public String getSalesOrders() {
        return zohoService.fetchSalesOrders();
    }
}

