package com.logicleaf.invplatform.config;

import com.logicleaf.invplatform.service.ZohoService;
import com.invplatform.generated.salesorder.api.SalesOrderApi;
import com.invplatform.generated.expenses.api.ExpensesApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class ZohoApiConfig {

    @Value("${zoho.api.base-path}")
    private String zohoApiBasePath;

    private final ZohoService zohoService;

    public ZohoApiConfig(ZohoService zohoService) {
        this.zohoService = zohoService;
    }

    @Bean
    @RequestScope
    public SalesOrderApi salesordersApi() {
        com.invplatform.generated.salesorder.invoker.ApiClient apiClient = new com.invplatform.generated.salesorder.invoker.ApiClient();
        apiClient.setBasePath(zohoApiBasePath);
        apiClient.addDefaultHeader("Authorization", "Zoho-oauthtoken " + getAccessToken());
        return new SalesOrderApi(apiClient);
    }

    @Bean
    @RequestScope
    public ExpensesApi expensesApi() {
        com.invplatform.generated.expenses.invoker.ApiClient apiClient = new com.invplatform.generated.expenses.invoker.ApiClient();
        apiClient.setBasePath(zohoApiBasePath);
        apiClient.addDefaultHeader("Authorization", "Zoho-oauthtoken " + getAccessToken());
        return new ExpensesApi(apiClient);
    }

    private String getAccessToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // The startupId will be extracted from the request, e.g., from a path variable or request parameter.
        // For this to work, we'll need a mechanism to get the startupId into the request context.
        // We will assume a request attribute 'startupId' is set by a filter or interceptor.
        String startupId = (String) request.getAttribute("startupId");
        if (startupId == null) {
            // Attempt to get it from request parameters as a fallback for initial setup/testing.
            startupId = request.getParameter("startupId");
            if (startupId == null) {
                throw new IllegalStateException("Startup ID not found in request attributes or parameters.");
            }
        }
        return zohoService.getValidZohoAccessToken(startupId);
    }
}
