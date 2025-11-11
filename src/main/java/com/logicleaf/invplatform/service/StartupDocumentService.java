package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.BadRequestException;
import com.logicleaf.invplatform.exception.ResourceNotFoundException;
import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StartupDocumentService {

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final StartupDocumentRepository documentRepository;
    private final FileStorageService fileStorageService;

    /**
     * Upload a document for the founder's startup.
     */
    public StartupDocument uploadDocument(String founderEmail, MultipartFile file, DocumentType documentType) {

        User founder = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Founder not found."));

        Startup startup = startupRepository.findByFounderUserId(founder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Startup not found for this founder."));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty.");
        }

        try {
            String safeFileName = fileStorageService.getUniqueFileName(file.getOriginalFilename());

            String savedPath = fileStorageService.uploadFile(safeFileName, file.getBytes());

            StartupDocument document = StartupDocument.builder()
                    .startupId(startup.getId())
                    .documentName(safeFileName)
                    .documentPath(savedPath)
                    .documentType(documentType)
                    .fileSize(file.getSize())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return documentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
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

    public void deleteDocument(String documentId) {
        try {
            // Find document by ID
            StartupDocument document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new ResourceNotFoundException("No document found with ID: " + documentId));


            fileStorageService.deleteFile(document.getDocumentName());

            documentRepository.deleteById(documentId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete document: " + e.getMessage(), e);
        }
    }

}
