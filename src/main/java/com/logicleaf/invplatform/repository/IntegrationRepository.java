package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.Integration;
import com.logicleaf.invplatform.model.IntegrationType;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface IntegrationRepository extends MongoRepository<Integration, String> {
    List<Integration> findByStartupId(String startupId);
    Integration findByStartupIdAndIntegrationType(String startupId, IntegrationType type);
}
