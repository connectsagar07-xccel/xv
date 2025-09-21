package com.logicleaf.invplatform.config;

import com.invplatform.generated.salesorder.api.SalesOrderApi;
import com.invplatform.generated.salesorder.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZohoApiConfig {

    @Value("${zoho.api.base-url}")
    private String baseUrl;

    @Value("${zoho.api.oauth-token}")
    private String oauthToken;

    @Bean
    public ApiClient zohoApiClient() {
        ApiClient client = new ApiClient();
        client.setBasePath(baseUrl);

        // OAuth in header
        client.addDefaultHeader("Authorization", "Zoho-oauthtoken " + oauthToken);

        return client;
    }

    @Bean
    public SalesOrderApi salesOrderApi(ApiClient zohoApiClient) {
        return new SalesOrderApi(zohoApiClient);
    }
}
