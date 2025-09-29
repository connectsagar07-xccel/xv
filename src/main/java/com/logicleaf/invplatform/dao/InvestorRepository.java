package com.logicleaf.invplatform.dao;

import com.logicleaf.invplatform.model.Investor;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface InvestorRepository extends ReactiveMongoRepository<Investor, String> {
}