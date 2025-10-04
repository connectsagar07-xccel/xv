package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.Report;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ReportRepository extends MongoRepository<Report, String> {
    List<Report> findByStartupId(String startupId);
}