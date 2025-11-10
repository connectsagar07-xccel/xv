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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

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

    @Autowired
    private ObjectMapper objectMapper;

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
            String apiDomain = response.has("api_domain") ? response.get("api_domain").asText()
                    : "https://www.zohoapis.com";
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
                    // String targetOrgName = "Your Organization Name";
                    // String organizationId = null;
                    // for (JsonNode org : organizations) {
                    // if (org.has("name") && targetOrgName.equals(org.get("name").asText())) {
                    // organizationId = org.get("organization_id").asText();
                    // break;
                    // }
                    // }
                    // if (organizationId != null) {
                    // startup.setZohoOrganizationId(organizationId);
                    // }
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

    public JsonNode fetchSalesOrdersForFounder(String founderEmail, String startDate, String endDate) {
        // Step 1: Find the user
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + founderEmail));

        // Step 2: Find their startup
        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup not found for user: " + founderEmail));

        // Step 3: Ensure Zoho is linked
        if (startup.getZohoAccessToken() == null || startup.getZohoOrganizationId() == null) {
            throw new RuntimeException("Zoho account not linked for this startup.");
        }

        // Step 4: Ensure token validity (refresh if needed)
        ensureValidToken(startup);

        // Step 5: Build URL dynamically
        StringBuilder urlBuilder = new StringBuilder("https://www.zohoapis.in/books/v3/salesorders");
        urlBuilder.append("?organization_id=").append(startup.getZohoOrganizationId());

        if (startDate != null && !startDate.isBlank()) {
            urlBuilder.append("&date_start=").append(startDate);
        }
        if (endDate != null && !endDate.isBlank()) {
            urlBuilder.append("&date_end=").append(endDate);
        }

        // Step 6: Setup headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + startup.getZohoAccessToken());
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Step 7: Execute request
        ResponseEntity<String> resp = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, entity,
                String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Zoho API returned " + resp.getStatusCodeValue() + ": " + resp.getBody());
        }

        // Step 8: Parse and return JSON
        try {
            return objectMapper.readTree(resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Zoho response: " + e.getMessage());
        }
    }

    public JsonNode fetchExpensesForFounder(String founderEmail, String startDate, String endDate) {
        // Step 1: Identify the founder user
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + founderEmail));

        // Step 2: Find their startup record
        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup not found for user: " + founderEmail));

        // Step 3: Check Zoho linkage
        if (startup.getZohoAccessToken() == null || startup.getZohoOrganizationId() == null) {
            throw new RuntimeException("Zoho account not linked for this startup.");
        }

        // Step 4: Ensure valid or refreshed token
        ensureValidToken(startup);

        // Step 5: Build Zoho Expenses API URL dynamically
        StringBuilder urlBuilder = new StringBuilder("https://www.zohoapis.in/books/v3/expenses");
        urlBuilder.append("?organization_id=").append(startup.getZohoOrganizationId());

        // Optional query params (Zoho expects YYYY-MM-DD)
        if (startDate != null && !startDate.isEmpty()) {
            urlBuilder.append("&date_start=").append(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            urlBuilder.append("&date_end=").append(endDate);
        }

        // Step 6: Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + startup.getZohoAccessToken());
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // Step 7: Call Zoho API
        ResponseEntity<String> resp = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, entity,
                String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Zoho API returned " + resp.getStatusCodeValue() + ": " + resp.getBody());
        }

        // Step 8: Parse JSON response
        try {
            return objectMapper.readTree(resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Zoho response: " + e.getMessage());
        }
    }

    public JsonNode fetchMonthlyExpensesForFounder(String founderEmail, int year, int month) {
        // 1) Resolve founder → startup
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + founderEmail));

        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup not found for user: " + founderEmail));

        if (startup.getZohoAccessToken() == null || startup.getZohoOrganizationId() == null) {
            throw new RuntimeException("Zoho account not linked for this startup.");
        }
        if (startup.getZohoTokenExpiryTime() != null &&
                startup.getZohoTokenExpiryTime().isBefore(java.time.LocalDateTime.now())) {
            // (Optional) plug refresh flow here
            throw new RuntimeException("Zoho access token expired. Please reauthorize.");
        }

        ensureValidToken(startup);

        // 2) Compute month window
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 3) Pick correct API domain (prefer dynamic; fallback to IN)
        String apiDomain = (currentToken != null && currentToken.getApiDomain() != null)
                ? currentToken.getApiDomain()
                : "https://www.zohoapis.in";

        // 4) Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + startup.getZohoAccessToken());
        headers.set("Accept", "application/json");
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 5) Paginate until done
        int page = 1;
        boolean hasMore = true;
        ArrayNode allExpenses = objectMapper.createArrayNode();
        ObjectNode lastPageContext = objectMapper.createObjectNode();

        while (hasMore) {
            String url = String.format(
                    "%s/books/v3/expenses?organization_id=%s&date_start=%s&date_end=%s&per_page=200&page=%d",
                    apiDomain, startup.getZohoOrganizationId(), fmt.format(start), fmt.format(end), page);

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (!resp.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Zoho API returned " + resp.getStatusCodeValue() + ": " + resp.getBody());
            }

            JsonNode body = readJson(resp.getBody());
            if (body.path("code").asInt(-1) != 0) {
                throw new RuntimeException("Zoho API error: " + body.path("message").asText());
            }

            // merge expenses
            JsonNode expenses = body.path("expenses");
            if (expenses.isArray()) {
                expenses.forEach(allExpenses::add);
            }

            // handle pagination
            JsonNode pageCtx = body.path("page_context");
            hasMore = pageCtx.path("has_more_page").asBoolean(false);
            lastPageContext = (ObjectNode) pageCtx;
            page++;
        }

        // 6) Uniform return object
        ObjectNode result = objectMapper.createObjectNode();
        result.put("code", 0);
        result.put("message", "success");
        result.set("expenses", allExpenses);
        result.set("page_context", lastPageContext);
        return result;
    }

    private JsonNode readJson(String s) {
        try {
            return objectMapper.readTree(s);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Zoho response: " + e.getMessage());
        }
    }

    private void ensureValidToken() {
        if (currentToken == null || currentToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Token missing or expired. Please authorize again.");
        }
    }

    private void refreshAccessToken(Startup startup) {
        String refreshToken = startup.getZohoRefreshToken();
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token missing. Please reauthorize your Zoho account.");
        }

        String url = "https://accounts.zoho.in/oauth/v2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("refresh_token", refreshToken);
        params.add("client_id", zohoClientId);
        params.add("client_secret", zohoClientSecret);
        params.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<JsonNode> resp = restTemplate.exchange(url, HttpMethod.POST, request, JsonNode.class);
            JsonNode body = resp.getBody();

            if (body == null || !body.has("access_token")) {
                throw new RuntimeException(
                        "Failed to refresh Zoho token: " + (body != null ? body.toString() : "null response"));
            }

            String newAccessToken = body.get("access_token").asText();
            long expiresIn = body.has("expires_in") ? body.get("expires_in").asLong() : 3600L;

            startup.setZohoAccessToken(newAccessToken);
            startup.setZohoTokenExpiryTime(LocalDateTime.now().plusSeconds(expiresIn));

            startupRepository.save(startup);

            // Also update in-memory currentToken if present
            currentToken = OAuthToken.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .apiDomain("https://www.zohoapis.in") // or use startup field if stored
                    .expiresAt(Instant.now().plusSeconds(expiresIn))
                    .build();

            System.out.println("✅ Zoho access token refreshed successfully at " + LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Error refreshing Zoho token: " + e.getMessage());
        }
    }

    private void ensureValidToken(Startup startup) {
        // If token not found, fail immediately
        if (startup.getZohoAccessToken() == null) {
            throw new IllegalStateException("Missing Zoho access token. Please authorize again.");
        }

        // If expired, refresh it
        if (startup.getZohoTokenExpiryTime() == null ||
                startup.getZohoTokenExpiryTime().isBefore(LocalDateTime.now())) {

            System.out.println("🔁 Zoho access token expired. Attempting refresh...");
            refreshAccessToken(startup);
        }
    }

    public JsonNode fetchAllUsersFromZoho(String founderEmail) {
        // 1️⃣ Find founder user
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + founderEmail));

        // 2️⃣ Get startup info
        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup not found for user: " + founderEmail));

        // 3️⃣ Ensure Zoho linkage
        if (startup.getZohoAccessToken() == null || startup.getZohoOrganizationId() == null) {
            throw new RuntimeException("Zoho account not linked for this startup.");
        }

        // 4️⃣ Ensure valid token
        ensureValidToken(startup);

        // 5️⃣ Determine API domain (fallback to .in)
        String apiDomain = (currentToken != null && currentToken.getApiDomain() != null)
                ? currentToken.getApiDomain()
                : "https://www.zohoapis.in";

        // 6️⃣ Build URL
        String url = String.format("%s/books/v3/users?organization_id=%s",
                apiDomain, startup.getZohoOrganizationId());

        // 7️⃣ Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + startup.getZohoAccessToken());
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 8️⃣ Call Zoho API (GET)
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // 9️⃣ Return raw JSON
        try {
            return objectMapper.readTree(resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Zoho response: " + e.getMessage(), e);
        }
    }

    public JsonNode fetchEmployeesFromZoho(String founderEmail, Integer page, Integer perPage) {
        // 1️⃣ Find founder user
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + founderEmail));

        // 2️⃣ Get startup info
        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup not found for user: " + founderEmail));

        // 3️⃣ Ensure Zoho linkage
        if (startup.getZohoAccessToken() == null || startup.getZohoOrganizationId() == null) {
            throw new RuntimeException("Zoho account not linked for this startup.");
        }

        // 4️⃣ Ensure token validity
        ensureValidToken(startup);

        // 5️⃣ Determine API domain
        String apiDomain = (currentToken != null && currentToken.getApiDomain() != null)
                ? currentToken.getApiDomain()
                : "https://www.zohoapis.in";

        // 6️⃣ Build URL dynamically with pagination
        StringBuilder urlBuilder = new StringBuilder(
                String.format("%s/books/v3/employees?organization_id=%s",
                        apiDomain, startup.getZohoOrganizationId()));

        // Add pagination params if provided
        if (page != null && page > 0) {
            urlBuilder.append("&page=").append(page);
        } else {
            urlBuilder.append("&page=1");
        }

        if (perPage != null && perPage > 0) {
            urlBuilder.append("&per_page=").append(perPage);
        } else {
            urlBuilder.append("&per_page=200");
        }

        // 7️⃣ Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Zoho-oauthtoken " + startup.getZohoAccessToken());
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // 8️⃣ Make API call
        ResponseEntity<String> resp = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, entity,
                String.class);

        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Zoho API returned " + resp.getStatusCodeValue() + ": " + resp.getBody());
        }

        // 9️⃣ Return parsed JSON (raw)
        try {
            return objectMapper.readTree(resp.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Zoho response: " + e.getMessage(), e);
        }
    }

}