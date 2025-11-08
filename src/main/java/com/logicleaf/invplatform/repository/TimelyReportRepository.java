package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.TimelyReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelyReportRepository extends MongoRepository<TimelyReport, String> {
    List<TimelyReport> findByFounderUserId(String founderUserId);
}
