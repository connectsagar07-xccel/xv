package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.StartupMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StartupMetricsRepository extends MongoRepository<StartupMetrics, String> {
    List<StartupMetrics> findByStartupId(String startupId);
}