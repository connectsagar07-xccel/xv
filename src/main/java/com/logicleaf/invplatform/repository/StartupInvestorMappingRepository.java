package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.MappingStatus;
import com.logicleaf.invplatform.model.StartupInvestorMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface StartupInvestorMappingRepository extends MongoRepository<StartupInvestorMapping, String> {
    List<StartupInvestorMapping> findByStartupId(String startupId);
    List<StartupInvestorMapping> findByInvestorId(String investorId);
    Optional<StartupInvestorMapping> findByStartupIdAndInvestorId(String startupId, String investorId);
    List<StartupInvestorMapping> findByStartupIdAndStatus(String startupId, MappingStatus status);
}