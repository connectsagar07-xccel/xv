package com.logicleaf.invplatform.dao;

import com.logicleaf.invplatform.model.ProcessedData;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface ProcessedDataRepository extends ReactiveMongoRepository<ProcessedData, String> {
    // example query
    Mono<ProcessedData> findBySourceName(String sourceName);
}
