package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.RefreshToken;
import com.logicleaf.invplatform.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(String userId);
}