package com.logicleaf.invplatform.dao;

import com.logicleaf.invplatform.model.RefreshToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveMongoRepository<RefreshToken, String> {
    Mono<RefreshToken> findByToken(String token);
    Mono<Void> deleteByUserId(String userId);
}
