package com.logicleaf.invplatform.dao;

import com.logicleaf.invplatform.model.Otp;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface OtpRepository extends ReactiveMongoRepository<Otp, String> {
    Mono<Otp> findByEmail(String email);
    Mono<Void> deleteByEmail(String email);
}