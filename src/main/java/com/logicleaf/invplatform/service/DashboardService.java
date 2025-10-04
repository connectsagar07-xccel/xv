package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.dto.PortfolioDashboardResponse;
import com.logicleaf.invplatform.dto.PortfolioStartupSummary;
import com.logicleaf.invplatform.model.*;
import com.logicleaf.invplatform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private InvestorRepository investorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StartupInvestorMappingRepository mappingRepository;

    @Autowired
    private StartupRepository startupRepository;

    @Autowired
    private StartupMetricsRepository metricsRepository;

    @Autowired
    private ReportRepository reportRepository;

    public PortfolioDashboardResponse getInvestorDashboard(String investorEmail) {
        User investorUser = userRepository.findByEmail(investorEmail)
                .orElseThrow(() -> new RuntimeException("Investor not found."));
        Investor investor = investorRepository.findByUserId(investorUser.getId())
                .orElseThrow(() -> new RuntimeException("Investor profile not found."));

        List<StartupInvestorMapping> connections = mappingRepository.findByInvestorId(investor.getId())
                .stream()
                .filter(mapping -> mapping.getStatus() == MappingStatus.ACTIVE)
                .collect(Collectors.toList());

        List<PortfolioStartupSummary> portfolioSummaries = new ArrayList<>();
        for (StartupInvestorMapping connection : connections) {
            Startup startup = startupRepository.findById(connection.getStartupId()).orElse(null);
            if (startup == null) continue;

            // Fetch latest metrics for this startup
            List<StartupMetrics> metrics = metricsRepository.findByStartupId(startup.getId());
            Map<String, String> latestMetrics = new HashMap<>();
            // This is a simplified approach; a real app might get the most recent for each metric name
            metrics.forEach(metric -> latestMetrics.put(metric.getMetricName(), metric.getMetricValue()));

            // Fetch recent, visible reports
            List<Report> recentReports = reportRepository.findByStartupId(startup.getId())
                    .stream()
                    .filter(Report::isVisibility)
                    .limit(5) // Get the 5 most recent visible reports
                    .collect(Collectors.toList());

            PortfolioStartupSummary summary = PortfolioStartupSummary.builder()
                    .startupId(startup.getId())
                    .startupName(startup.getStartupName())
                    .companyName(startup.getCompanyName())
                    .latestMetrics(latestMetrics)
                    .recentReports(recentReports)
                    .build();

            portfolioSummaries.add(summary);
        }

        // Calculate aggregate metrics (placeholders for now)
        Map<String, String> aggregateMetrics = new HashMap<>();
        aggregateMetrics.put("Total Invested", "0"); // This would require more data
        aggregateMetrics.put("Blended Revenue", "0");
        aggregateMetrics.put("Portfolio Companies", String.valueOf(portfolioSummaries.size()));

        return PortfolioDashboardResponse.builder()
                .aggregateMetrics(aggregateMetrics)
                .portfolio(portfolioSummaries)
                .build();
    }
}