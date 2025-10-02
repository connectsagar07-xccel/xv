package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.generated.expenses.api.ExpensesApi;
import com.logicleaf.invplatform.generated.salesorder.api.SalesOrderApi;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZohoServiceImpl implements ZohoService {

    private static final String ZOHO_ACCESS_TOKEN = "zoho_access_token";

    @Value("${zoho.client.id}")
    private String zohoClientId;

    @Value("${zoho.client.secret}")
    private String zohoClientSecret;

    @Value("${zoho.redirect.uri}")
    private String zohoRedirectUri;

    @Value("${zoho.organization.id}")
    private String zohoOrganizationId;

    private final com.logicleaf.invplatform.generated.expenses.invoker.ApiClient expensesApiClient;
    private final com.logicleaf.invplatform.generated.salesorder.invoker.ApiClient salesOrderApiClient;

    @Override
    public String getZohoAuthUrl() {
        String scope = "ZohoBooks.expenses.READ,ZohoBooks.salesorders.READ";
        return UriComponentsBuilder.fromHttpUrl("https://accounts.zoho.com/oauth/v2/auth")
                .queryParam("scope", scope)
                .queryParam("client_id", zohoClientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", zohoRedirectUri)
                .queryParam("access_type", "offline")
                .build().toUriString();
    }

    @Override
    public void handleCallback(String code, HttpSession session) {
        String url = "https://accounts.zoho.com/oauth/v2/token";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", zohoClientId);
        params.put("client_secret", zohoClientSecret);
        params.put("redirect_uri", zohoRedirectUri);
        params.put("grant_type", "authorization_code");

        Map<String, String> response = restTemplate.postForObject(url, params, Map.class);
        String accessToken = response.get("access_token");

        session.setAttribute(ZOHO_ACCESS_TOKEN, accessToken);
    }

    @Override
    public Object getExpenses(HttpSession session) throws Exception {
        String accessToken = (String) session.getAttribute(ZOHO_ACCESS_TOKEN);
        if (accessToken == null) {
            throw new IllegalStateException("Not authenticated with Zoho. Please initiate the OAuth2 flow.");
        }
        expensesApiClient.setAccessToken(accessToken);
        ExpensesApi expensesApi = new ExpensesApi(expensesApiClient);
        // Passing null for all optional parameters as per the generated client
        return expensesApi.listExpenses(zohoOrganizationId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    @Override
    public Object getSalesOrders(HttpSession session) throws Exception {
        String accessToken = (String) session.getAttribute(ZOHO_ACCESS_TOKEN);
        if (accessToken == null) {
            throw new IllegalStateException("Not authenticated with Zoho. Please initiate the OAuth2 flow.");
        }
        salesOrderApiClient.setAccessToken(accessToken);
        SalesOrderApi salesOrderApi = new SalesOrderApi(salesOrderApiClient);
        // Passing null for all optional parameters as per the generated client
        return salesOrderApi.listSalesOrders(zohoOrganizationId, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}