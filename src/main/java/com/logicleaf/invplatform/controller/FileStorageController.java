package com.logicleaf.invplatform.controller;

import com.logicleaf.invplatform.service.FileStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document")
public class FileStorageController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * API: GET /api/document/download?fileName=example.pdf
     * 
     * Downloads the specified file.
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {
        try {
            byte[] data = fileStorageService.downloadFile(fileName);

            ByteArrayResource resource = new ByteArrayResource(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(404)
                    .body(new ByteArrayResource(("Error: " + e.getMessage()).getBytes()));
        }
    }
}
