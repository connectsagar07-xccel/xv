package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.FinancialMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FinancialMetricsRepository extends MongoRepository<FinancialMetrics, String> {
}
