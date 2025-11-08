package com.logicleaf.invplatform.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "timely_reports")
public class TimelyReport {

    @Id
    private String id;

    private String founderUserId;
    private String startupId;

    private String title;
    private String reportingPeriod;

    private String keyMetrics;
    private Double monthlyRevenue;
    private Double monthlyBurn;
    private Integer cashRunway;
    private Integer teamSize;

    private String keyAchievements;
    private String challengesAndLearnings;
    private String otherKeyMetrics;
    private String asksFromInvestors;

    // ✅ List of structured attachment objects
    private List<TimelyReportAttachment> attachments;

    private List<String> investorUserIds;

    private Long createdAt;
    private Long updatedAt;
}
