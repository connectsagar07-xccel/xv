package com.logicleaf.invplatform.repository;

import com.logicleaf.invplatform.model.DocumentType;
import com.logicleaf.invplatform.model.StartupDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StartupDocumentRepository extends MongoRepository<StartupDocument, String> {
    List<StartupDocument> findByStartupId(String startupId);
    List<StartupDocument> findByStartupIdAndDocumentType(String startupId, DocumentType documentType);
}
