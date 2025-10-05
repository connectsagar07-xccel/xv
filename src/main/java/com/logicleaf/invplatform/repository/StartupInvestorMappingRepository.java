package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.StartupInvestorMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface StartupInvestorMappingRepository extends MongoRepository<StartupInvestorMapping, String> {
    List<StartupInvestorMapping> findByStartupId(String startupId);
    List<StartupInvestorMapping> findByInvestorId(String investorId);
}