package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dto.CreateReportRequest;
import com.logicleaf.invplatform.model.Report;
import com.logicleaf.invplatform.model.Startup;
import com.logicleaf.invplatform.model.User;
import com.logicleaf.invplatform.repository.ReportRepository;
import com.logicleaf.invplatform.repository.StartupRepository;
import com.logicleaf.invplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private UserRepository userRepository;

    public Report createReport(String founderEmail, CreateReportRequest request) {
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found."));
        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup profile not found."));

        Report report = Report.builder()
                .startupId(startup.getId())
                .period(request.getPeriod())
                .dataJSON(request.getDataJSON())
                .visibility(request.isVisibility())
                .pdfUrl(null) // Placeholder for PDF generation
                .build();

        return reportRepository.save(report);
    }

    public Report updateReportVisibility(String founderEmail, String reportId, boolean isVisible) {
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found."));
        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup profile not found."));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found."));

        // Ensure the report belongs to the founder's startup
        if (!report.getStartupId().equals(startup.getId())) {
            throw new RuntimeException("You are not authorized to modify this report.");
        }

        report.setVisibility(isVisible);

        // Here you would trigger notifications to connected investors if the report is made visible
        // notificationService.notifyInvestorsOfNewReport(startup.getId(), report);

        return reportRepository.save(report);
    }

    public List<Report> getReportsForStartup(String founderEmail) {
        User user = userRepository.findByEmail(founderEmail)
                .orElseThrow(() -> new RuntimeException("User not found."));
        Startup startup = startupRepository.findByFounderUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Startup profile not found."));

        return reportRepository.findByStartupId(startup.getId());
    }
}