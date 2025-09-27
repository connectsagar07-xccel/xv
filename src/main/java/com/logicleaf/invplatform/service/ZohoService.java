package com.logicleaf.invplatform.service;

import com.invplatform.generated.salesorder.api.SalesOrderApi;
import com.invplatform.generated.salesorder.invoker.ApiClient;
import com.logicleaf.invplatform.config.ZohoApiClientFactory;
import com.logicleaf.invplatform.dao.ProcessedDataRepository;
import com.logicleaf.invplatform.model.OAuthToken;
import com.logicleaf.invplatform.model.ProcessedData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZohoService {

    private final ZohoTokenManager tokenManager;
    private final ZohoApiClientFactory apiClientFactory;
    private final ProcessedDataRepository repository;

    /**
     * Fetch sales orders for the given year (example). We create SalesOrderApi with a fresh token for each call.
     */
    public Mono<ProcessedData> fetchZohoSalesOrders(int year) {
        return tokenManager.getToken()
                .flatMap(token -> {
                    ApiClient client = apiClientFactory.createApiClient(token);
                    SalesOrderApi api = new SalesOrderApi(client);

                    // same parameters you used previously (keep them identical)
                    return api.listSalesOrders(
                                    null, null, null, null, null, null, null, null, null,
                                    null, null, null, null, null, null, null, null, null,
                                    "json", null, null, null, 1, 200
                            )
                            .map(zohoResponse -> ProcessedData.builder()
                                    .sourceName("Zoho")
                                    .type("sales_order")
                                    .receivedAt(Instant.now())
                                    .processedAt(Instant.now())
                                    .content(Map.of("rawResponse", zohoResponse))
                                    .metadata(Map.of("year", String.valueOf(year)))
                                    .build()
                            )
                            .flatMap(repository::save);
                });
    }
}
