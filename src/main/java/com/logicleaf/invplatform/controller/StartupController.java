package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.dto.InvestorFullResponse;
import com.logicleaf.invplatform.model.DocumentType;
import com.logicleaf.invplatform.model.StartupDocument;
import com.logicleaf.invplatform.service.StartupDocumentService;
import com.logicleaf.invplatform.service.StartupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/startup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('FOUNDER')")
public class StartupController {

    private final StartupService startupService;

    /**
     * Get complete investor info (User + Investor + Mapping) for logged-in
     * founder's startup.
     */
    @GetMapping("/investors")
    public ResponseEntity<?> getFullInvestors(@AuthenticationPrincipal UserDetails userDetails) {

        List<InvestorFullResponse> investors = startupService.getFullInvestorDataForStartup(userDetails.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Complete investor details fetched successfully.");
        response.put("data", investors);

        return ResponseEntity.ok(response);
    }

    private final StartupDocumentService documentService;

    /**
     * Upload a startup document (Financial / Legal / Pitch)
     */
    @PostMapping("/documents")
    public ResponseEntity<?> uploadDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {

        StartupDocument uploaded = documentService.uploadDocument(userDetails.getUsername(), file, documentType);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Document uploaded successfully.");
        response.put("data", uploaded);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<?> deleteStartupDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String documentId) {

        documentService.deleteDocument(documentId);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Document deleted successfully.");
        return ResponseEntity.ok(response);
    }

    /**
     * Get all documents uploaded for this startup
     */
    @GetMapping("/documents")
    public ResponseEntity<?> getStartupDocuments(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "documentType", required = false) DocumentType documentType) {

        List<StartupDocument> documents = documentService.getDocuments(userDetails.getUsername(), documentType);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", documentType == null
                ? "All startup documents fetched successfully."
                : "Startup documents of type " + documentType + " fetched successfully.");
        response.put("data", documents);

        return ResponseEntity.ok(response);
    }
}
