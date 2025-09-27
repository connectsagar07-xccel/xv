package com.logicleaf.invplatform.dao;

import com.logicleaf.invplatform.model.ZohoToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ZohoTokenRepository extends MongoRepository<ZohoToken, String> {
}

