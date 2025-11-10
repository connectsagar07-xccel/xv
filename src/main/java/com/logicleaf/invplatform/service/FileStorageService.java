package com.logicleaf.invplatform.service;

// import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    // @Value("${ATTACHMENT_DIR:uploads/files}")
    private String ATTACHMENT_DIR = "E:/invplatform/uploads/attachments/";

    /**
     * Upload a file with the same name.
     *
     * @param fileName  The file name (e.g. "report.pdf")
     * @param fileBytes File content
     * @return The absolute path of the saved file
     */
    public String uploadFile(String fileName, byte[] fileBytes) {
        try {
            Path dirPath = Paths.get(ATTACHMENT_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            Path filePath = dirPath.resolve(fileName);

            // Save or overwrite
            Files.write(filePath, fileBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            return filePath.toAbsolutePath().toString();

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file by its absolute path.
     *
     * @param filePath The file path
     * @return true if deleted, false if not found
     */
    public boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Download a file as byte array.
     *
     * @param fileName The file name (located inside ATTACHMENT_DIR)
     * @return File bytes
     */
    public byte[] downloadFile(String fileName) {
        try {
            Path filePath = Paths.get(ATTACHMENT_DIR).resolve(fileName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("File not found: " + fileName);
            }
            return Files.readAllBytes(filePath);

        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
        }
    }

    public String getUniqueFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            fileName = "file";
        }

        // Sanitize filename (allow only letters, numbers, underscores, dots, and hyphens)
        String safeFileName = fileName.replaceAll("[^a-zA-Z0-9_.\\-]", "_");

        // Generate short UUID prefix
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return uuid + "_" + safeFileName;
    }
}
