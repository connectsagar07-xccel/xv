package com.logicleaf.invplatform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ZohoConfig {

    @Bean
    public com.logicleaf.invplatform.generated.expenses.invoker.ApiClient expensesApiClient() {
        com.logicleaf.invplatform.generated.expenses.invoker.ApiClient client = new com.logicleaf.invplatform.generated.expenses.invoker.ApiClient(new RestTemplate());
        client.setBasePath("https://www.zohoapis.com/books/v3");
        return client;
    }

    @Bean
    public com.logicleaf.invplatform.generated.salesorder.invoker.ApiClient salesOrderApiClient() {
        com.logicleaf.invplatform.generated.salesorder.invoker.ApiClient client = new com.logicleaf.invplatform.generated.salesorder.invoker.ApiClient(new RestTemplate());
        client.setBasePath("https://www.zohoapis.com/books/v3");
        return client;
    }
}