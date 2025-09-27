package com.logicleaf.invplatform.config;

import com.invplatform.generated.salesorder.api.SalesOrderApi;
import com.invplatform.generated.salesorder.invoker.ApiClient;
import com.logicleaf.invplatform.service.ZohoTokenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZohoApiConfig {

//    private final ZohoTokenService tokenService;
//
//    public ZohoApiConfig(ZohoTokenService tokenService) {
//        this.tokenService = tokenService;
//    }
//
//    @Bean
//    public ApiClient zohoApiClient() {
//        ApiClient client = new ApiClient();
//        client.setBasePath("https://www.zohoapis.com/books/v3");
//
//        // dynamically inject token before each request
//        client.addDefaultHeader("Authorization", "Zoho-oauthtoken " + tokenService.getValidAccessToken().block());
//
//        return client;
//    }
//
//    @Bean
//    public SalesOrderApi salesOrderApi(ApiClient zohoApiClient) {
//        return new SalesOrderApi(zohoApiClient);
//    }
}
