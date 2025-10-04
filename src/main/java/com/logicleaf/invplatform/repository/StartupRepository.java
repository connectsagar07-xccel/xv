package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.Startup;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface StartupRepository extends MongoRepository<Startup, String> {
    Optional<Startup> findByFounderUserId(String founderUserId);
}