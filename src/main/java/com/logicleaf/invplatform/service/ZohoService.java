package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.OAuthToken;
import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.repository.StartupRepository;
import com.logicleaf.invplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import com.fasterxml.jackson.databind.JsonNode;


import java.time.Instant;
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

    @Autowired
    private RestTemplate restTemplate;

    private OAuthToken currentToken;

    private static final String ZOHO_AUTH_URL = "https://accounts.zoho.in/oauth/v2/auth";
    private static final String ZOHO_TOKEN_URL = "https://accounts.zoho.in/oauth/v2/token";

    public String getZohoAuthUrl(String userEmail) {
        // We pass the user's email as state to identify them on callback
        return ZOHO_AUTH_URL + "?response_type=code&client_id=" + zohoClientId +
               "&scope=ZohoExpense.fullaccess.all,ZohoBooks.fullaccess.all&redirect_uri=" + zohoRedirectUri +
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

            String accessToken = response.get("access_token").asText();
            String refreshToken = response.has("refresh_token") ? response.get("refresh_token").asText() : null;
            String apiDomain = response.has("api_domain") ? response.get("api_domain").asText() : "https://www.zohoapis.com";
            long expiresIn = response.get("expires_in").asLong();

            currentToken = OAuthToken.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .apiDomain(apiDomain)
                    .expiresAt(Instant.now().plusSeconds(expiresIn))
                    .build();

            startup.setZohoAccessToken(accessToken);
            if (refreshToken != null) {
                startup.setZohoRefreshToken(refreshToken);
            }
            startup.setZohoTokenExpiryTime(LocalDateTime.now().plusSeconds(expiresIn));

            // Fetch organization ID from Zoho Books
            String orgUrl = "https://books.zoho.in/api/v3/organizations";
            HttpHeaders orgHeaders = new HttpHeaders();
            orgHeaders.set("Authorization", "Zoho-oauthtoken " + accessToken);
            HttpEntity<Void> orgEntity = new HttpEntity<>(orgHeaders);

            ResponseEntity<JsonNode> orgResp = restTemplate.exchange(orgUrl, HttpMethod.GET, orgEntity, JsonNode.class);
            if (orgResp.getStatusCode().is2xxSuccessful() && orgResp.getBody() != null) {
                JsonNode organizations = orgResp.getBody().get("organizations");
                if (organizations != null && organizations.isArray() && organizations.size() > 0) {
//                    String targetOrgName = "Your Organization Name";
//                    String organizationId = null;
//                    for (JsonNode org : organizations) {
//                        if (org.has("name") && targetOrgName.equals(org.get("name").asText())) {
//                            organizationId = org.get("organization_id").asText();
//                            break;
//                        }
//                    }
//                    if (organizationId != null) {
//                        startup.setZohoOrganizationId(organizationId);
//                    }
                    String organizationId = organizations.get(0).get("organization_id").asText();
                    startup.setZohoOrganizationId(organizationId);
                }
            }

            startupRepository.save(startup);
        } else {
            throw new RuntimeException("Failed to get Zoho access token.");
        }
    }

    // Placeholder for fetching metrics
    public void fetchAndSaveMetrics(String startupId) {
        // Logic to use the access token to call Zoho APIs will go here.
        // This will involve checking if the token is expired and refreshing if needed.
        // Then, use the OpenAPI generated clients to fetch data.
    }

    public String fetchSalesOrders(String organizationId) {
        ensureValidToken();

        String url = currentToken.getApiDomain() + "/books/v3/salesorders?organization_id=" + organizationId;

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
}