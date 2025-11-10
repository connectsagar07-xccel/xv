package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.BadRequestException;
import com.logicleaf.invplatform.exception.ResourceNotFoundException;

import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelyReportService {

    private final TimelyReportRepository timelyReportRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final MailService mailService;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private FileStorageService fileStorageService;


    public TimelyReport createTimelyReport(TimelyReport report, MultipartFile[] attachments) {
        if (report.getTitle() == null || report.getTitle().isBlank()) {
            throw new BadRequestException("Report title cannot be empty");
        }

        // Validate founder & startup
        User founder = userRepository.findById(report.getFounderUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Founder not found"));
        Startup startup = startupRepository.findByFounderUserId(founder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Startup not found for founder"));

        if (report.isDraftReport()) {
            boolean draftExists = timelyReportRepository.existsByStartupIdAndDraftReportTrue(startup.getId());
            if (draftExists) {
                throw new BadRequestException(
                        "A draft report already exists. Please update or delete it before creating a new one.");
            }
        }

        // 💾 Save all uploaded files
        if (attachments != null && attachments.length > 0) {
            List<TimelyReportAttachment> savedFiles = saveAttachments(attachments);
            report.setAttachments(savedFiles);
        }

        report.setStartupId(startup.getId());
        report.setCreatedAt(System.currentTimeMillis());
        report.setUpdatedAt(System.currentTimeMillis());

        TimelyReport savedReport = timelyReportRepository.save(report);

        // ✅ Generate PDF report
        byte[] pdfBytes = pdfGeneratorService.generateTimelyReportPdf(savedReport, startup.getStartupName());

        TimelyReportAttachment pdfAttachment = savePdfAttachment(pdfBytes, report.getTitle(), startup.getStartupName());
        report.setReportPdf(pdfAttachment);

        // ✅ Send to investors
        if(!report.isDraftReport()){
            if (report.getInvestorUserIds() != null && !report.getInvestorUserIds().isEmpty()) {
                for (String investorUserId : report.getInvestorUserIds()) {
                    userRepository.findById(investorUserId).ifPresent(investor -> {
                        try {
                            mailService.sendTimelyReportWithPdf(
                                    founder.getEmail(),
                                    investor.getEmail(),
                                    startup.getStartupName(),
                                    savedReport,
                                    pdfBytes,
                                    pdfAttachment.getFileName());
                        } catch (Exception e) {
                            System.err.println("Failed to send PDF email: " + e.getMessage());
                        }
                    });
                }
            }
        }
        

        return savedReport;
    }

    // 💾 Save PDF file to disk and return reference
    private TimelyReportAttachment savePdfAttachment(byte[] pdfBytes, String title, String startupName) {
        try {

            String fileName = startupName + "_" + title + ".pdf";
            String safeFileName = fileStorageService.getUniqueFileName(fileName);

            // Use global file storage service to save file
            String savedPath = fileStorageService.uploadFile(safeFileName, pdfBytes);

            return TimelyReportAttachment.builder()
                    .fileName(safeFileName)
                    .filePath(savedPath)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to save generated PDF report: " + e.getMessage(), e);
        }
    }

    /**
     * Save all files to the global folder and return a list of Attachment objects.
     */
    private List<TimelyReportAttachment> saveAttachments(MultipartFile[] attachments) {
        List<TimelyReportAttachment> savedFiles = new ArrayList<>();

        for (MultipartFile file : attachments) {
            try {
                String fileName = (file.getOriginalFilename() != null)
                        ? file.getOriginalFilename().replaceAll("[^a-zA-Z0-9_.\\-]", "_")
                        : "attachment";
                        
                String safeFileName = fileStorageService.getUniqueFileName(fileName);
                // Save using FileStorageService
                String savedPath = fileStorageService.uploadFile(safeFileName, file.getBytes());

                savedFiles.add(TimelyReportAttachment.builder()
                        .fileName(safeFileName)
                        .filePath(savedPath)
                        .build());

            } catch (IOException e) {
                throw new RuntimeException("Failed to save attachment: " + e.getMessage(), e);
            }
        }

        return savedFiles;
    }

    public List<TimelyReport> getReportsByFounder(String founderUserId) {
        return timelyReportRepository.findByFounderUserId(founderUserId);
    }
}
