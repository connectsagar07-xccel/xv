package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.exception.BadRequestException;
import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelyReportService {

    private final TimelyReportRepository timelyReportRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final MailService mailService;

    // 🌍 Global storage folder for attachments
    private static final String ATTACHMENT_DIR = "E:/invplatform/uploads/attachments/";


    public TimelyReport createTimelyReport(TimelyReport report, MultipartFile[] attachments) {
        if (report.getTitle() == null || report.getTitle().isBlank()) {
            throw new BadRequestException("Report title cannot be empty");
        }

        // Validate founder & startup
        User founder = userRepository.findById(report.getFounderUserId())
                .orElseThrow(() -> new BadRequestException("Founder not found"));
        Startup startup = startupRepository.findByFounderUserId(founder.getId())
                .orElseThrow(() -> new BadRequestException("Startup not found for founder"));

        // 💾 Save all uploaded files
        if (attachments != null && attachments.length > 0) {
            List<Attachment> savedFiles = saveAttachments(attachments);
            report.setAttachments(savedFiles);
        }

        report.setStartupId(startup.getId());
        report.setCreatedAt(System.currentTimeMillis());
        report.setUpdatedAt(System.currentTimeMillis());

        TimelyReport savedReport = timelyReportRepository.save(report);

        // ✉️ Send emails to all investors
        if (report.getInvestorUserIds() != null && !report.getInvestorUserIds().isEmpty()) {
            report.getInvestorUserIds().forEach(investorUserId ->
                    userRepository.findById(investorUserId).ifPresent(investorUser -> {
                        try {
                            mailService.sendTimelyReportEmail(savedReport,
                                    founder.getEmail(),
                                    investorUser.getEmail(),
                                    startup.getStartupName());
                        } catch (MessagingException e) {
                            System.err.println("Failed to send email to " + investorUser.getEmail() + ": " + e.getMessage());
                        }
                    })
            );
        }

        return savedReport;
    }

    /**
     * Save all files to the global folder and return a list of Attachment objects.
     */
    private List<Attachment> saveAttachments(MultipartFile[] attachments) {
        List<Attachment> savedFiles = new ArrayList<>();

        try {
            Path dirPath = Paths.get(ATTACHMENT_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            for (MultipartFile file : attachments) {
                // Generate unique file name
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path filePath = dirPath.resolve(fileName);

                // Save file to disk
                file.transferTo(filePath.toFile());

                // Build attachment object
                Attachment attachment = Attachment.builder()
                        .fileName(fileName)
                        .filePath(filePath.toAbsolutePath().toString())
                        .build();

                savedFiles.add(attachment);
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to save attachments: " + e.getMessage());
        }

        return savedFiles;
    }

    public List<TimelyReport> getReportsByFounder(String founderUserId) {
        return timelyReportRepository.findByFounderUserId(founderUserId);
    }
}
