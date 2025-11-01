package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.repository.StartupRepository;
import com.logicleaf.invplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;


import java.time.LocalDateTime;

@Service
public class ZohoService {

    @Value("${ZOHO_CLIENT_ID}")
    private String zohoClientId;

    @Value("${ZOHO_CLIENT_SECRET}")
    private String zohoClientSecret;

    @Value("${ZOHO_REDIRECT_URI}")
    private String zohoRedirectUri;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String ZOHO_AUTH_URL = "https://accounts.zoho.com/oauth/v2/auth";
    private static final String ZOHO_TOKEN_URL = "https://accounts.zoho.com/oauth/v2/token";

    public String getZohoAuthUrl(String userEmail) {
        // We pass the user's email as state to identify them on callback
        return ZOHO_AUTH_URL + "?response_type=code&client_id=" + zohoClientId +
               "&scope=ZohoExpenses.fullaccess.all,ZohoBooks.fullaccess.all&redirect_uri=" + zohoRedirectUri +
               "&access_type=offline&prompt=consent&state=" + userEmail;
    }

    public void handleZohoCallback(String code, String state) {
        String userEmail = state; // The user's email we passed in the auth URL

        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found for state: " + userEmail));

        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup profile not found for user: " + userEmail));

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", code);
        map.add("client_id", zohoClientId);
        map.add("client_secret", zohoClientSecret);
        map.add("redirect_uri", zohoRedirectUri);
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        JsonNode response = restTemplate.postForObject(ZOHO_TOKEN_URL, request, JsonNode.class);

        if (response != null && response.has("access_token")) {
            startup.setZohoAccessToken(response.get("access_token").asText());
            if (response.has("refresh_token")) {
                startup.setZohoRefreshToken(response.get("refresh_token").asText());
            }
            // Zoho token expiry is in seconds
            long expiresIn = response.get("expires_in").asLong();
            startup.setZohoTokenExpiryTime(LocalDateTime.now().plusSeconds(expiresIn));

            startupRepository.save(startup);
        } else {
            throw new RuntimeException("Failed to get Zoho access token.");
        }
    }

    public String getValidZohoAccessToken(String startupId) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new RuntimeException("Startup not found with id: " + startupId));

        if (startup.getZohoRefreshToken() == null) {
            throw new IllegalStateException("Zoho account not connected for startup: " + startupId);
        }

        // Refresh the token if it's expired or about to expire (e.g., within the next minute)
        if (startup.getZohoTokenExpiryTime() == null || startup.getZohoTokenExpiryTime().isBefore(LocalDateTime.now().plusMinutes(1))) {
            refreshZohoToken(startup);
        }

        return startup.getZohoAccessToken();
    }

    private void refreshZohoToken(Startup startup) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("refresh_token", startup.getZohoRefreshToken());
        map.add("client_id", zohoClientId);
        map.add("client_secret", zohoClientSecret);
        map.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            JsonNode response = restTemplate.postForObject(ZOHO_TOKEN_URL, request, JsonNode.class);

            if (response != null && response.has("access_token")) {
                startup.setZohoAccessToken(response.get("access_token").asText());
                long expiresIn = response.get("expires_in").asLong();
                startup.setZohoTokenExpiryTime(LocalDateTime.now().plusSeconds(expiresIn));
                startupRepository.save(startup);
            } else {
                // Log the error response from Zoho for debugging
                String errorResponse = response != null ? response.toString() : "No response body";
                throw new RuntimeException("Failed to refresh Zoho access token. Response: " + errorResponse);
            }
        } catch (Exception e) {
            // Log the exception
            throw new RuntimeException("Error while refreshing Zoho token: " + e.getMessage(), e);
        }
    }
}