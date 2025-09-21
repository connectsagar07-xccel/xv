package com.logicleaf.invplatform.service;

import com.invplatform.generated.salesorder.api.SalesOrderApi;
import com.invplatform.generated.salesorder.invoker.ApiClient;
import com.logicleaf.invplatform.dao.ProcessedDataRepository;
import com.logicleaf.invplatform.model.ProcessedData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ZohoService {

    private final SalesOrderApi salesOrderApi; // injected with OAuth + URL
    private final ProcessedDataRepository repository;

    public Mono<ProcessedData> fetchZohoSalesOrders(int year) {
        return salesOrderApi.listSalesOrders(
                        null, null, null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, null, null,
                        "json", null, null, null, 1, 200
                )
                .log()
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
    }
}
