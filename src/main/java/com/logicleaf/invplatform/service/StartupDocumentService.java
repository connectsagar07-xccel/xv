package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.ResourceNotFoundException;
import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StartupDocumentService {

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final StartupDocumentRepository documentRepository;

    // Base directory to store uploaded documents
    private static final String UPLOAD_DIR = "E:/invplatform/uploads/attachments/";

    /**
     * Upload a document for the founder's startup.
     */
    public StartupDocument uploadDocument(String founderEmail, MultipartFile file, DocumentType documentType) {

        // 1️⃣ Find founder
        User founder = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Founder not found."));

        // 2️⃣ Find startup
        Startup startup = startupRepository.findByFounderUserId(founder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Startup not found for this founder."));

        // 3️⃣ Ensure directory exists
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 4️⃣ Create unique file name
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String fullPath = UPLOAD_DIR + fileName;

        // 5️⃣ Save file to server
        try {
            file.transferTo(new File(fullPath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document: " + e.getMessage());
        }

        // 6️⃣ Save metadata in DB
        StartupDocument document = StartupDocument.builder()
                .startupId(startup.getId())
                .documentName(file.getOriginalFilename())
                .documentPath(fullPath)
                .documentType(documentType)
                .fileSize(file.getSize())
                .uploadedAt(LocalDateTime.now())
                .build();

        return documentRepository.save(document);
    }

    /**
     * Get all documents uploaded for the logged-in founder's startup.
     */
    public List<StartupDocument> getDocuments(String founderEmail, DocumentType documentType) {
        User founder = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Founder not found."));

        Startup startup = startupRepository.findByFounderUserId(founder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Startup not found for this founder."));

        if (documentType != null) {
            return documentRepository.findByStartupIdAndDocumentType(startup.getId(), documentType);
        } else {
            return documentRepository.findByStartupId(startup.getId());
        }
    }
}
