package com.logicleaf.invplatform.dao;

import com.logicleaf.invplatform.model.Startup;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface StartupRepository extends ReactiveMongoRepository<Startup, String> {
    Mono<Startup> findByFounderId(String founderId);
}