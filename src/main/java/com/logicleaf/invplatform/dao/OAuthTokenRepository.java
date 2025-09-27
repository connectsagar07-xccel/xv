package com.logicleaf.invplatform.dao;

import com.logicleaf.invplatform.model.OAuthToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OAuthTokenRepository extends ReactiveMongoRepository<OAuthToken, String> {
}
