package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.Investor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface InvestorRepository extends MongoRepository<Investor, String> {
    Optional<Investor> findByUserId(String userId);
}