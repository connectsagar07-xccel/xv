package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.model.ProcessedData;
import com.logicleaf.invplatform.service.ZohoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/zoho")
@RequiredArgsConstructor
public class ZohoController {

    private final ZohoService zohoService;

    @GetMapping("/salesorders")
    public Mono<ProcessedData> getSalesOrders(@RequestParam int year) {
        return zohoService.fetchZohoSalesOrders(year);
    }
}
