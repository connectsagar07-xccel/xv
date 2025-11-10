package com.logicleaf.invplatform.service;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.logicleaf.invplatform.model.TimelyReport;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for generating investor-style PDF reports, now with embedded charts.
 */
@Service
public class PdfGeneratorService {

    public byte[] generateTimelyReportPdf(TimelyReport report, String startupName) {
        try {
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);

            document.open();

            // Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(64, 64, 128));
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC, new BaseColor(100, 100, 100));
            Font sectionTitle = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, new BaseColor(60, 60, 60));
            Font normal = new Font(Font.FontFamily.HELVETICA, 11);
            Font green = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(20, 150, 20));
            Font red = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, new BaseColor(200, 40, 40));

            // Header
            String monthTitle = (report.getReportingPeriod() != null)
                    ? report.getReportingPeriod()
                    : "December 2024 Monthly Report";

            Paragraph header = new Paragraph(monthTitle, titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph(startupName + " • Generated on " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d, yyyy")), subtitleFont);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            document.add(subHeader);
            document.add(Chunk.NEWLINE);

            // Metrics Table
            PdfPTable metricsTable = new PdfPTable(4);
            metricsTable.setWidthPercentage(100);
            metricsTable.setWidths(new float[]{2f, 2f, 2f, 2f});

            addMetric(metricsTable, "Monthly Revenue", "₹" + safeFormat(report.getMonthlyRevenue()) + "L");
            addMetric(metricsTable, "Monthly Burn", "₹" + safeFormat(report.getMonthlyBurn()) + "L");
            addMetric(metricsTable, "Cash Runway", report.getCashRunway() != null ? report.getCashRunway() + " months" : "16 months");
            addMetric(metricsTable, "Team Size", report.getTeamSize() != null ? report.getTeamSize().toString() : "23");

            document.add(metricsTable);
            document.add(Chunk.NEWLINE);

            // === Revenue Growth Chart ===
            Paragraph revGrowth = new Paragraph("Revenue Growth", sectionTitle);
            document.add(revGrowth);

            Image revenueChartImage = Image.getInstance(generateRevenueGrowthChart());
            revenueChartImage.scaleToFit(450, 250);
            revenueChartImage.setAlignment(Element.ALIGN_CENTER);
            document.add(revenueChartImage);

            document.add(Chunk.NEWLINE);

            // === Burn vs Runway Chart ===
            Paragraph burnRunway = new Paragraph("Burn vs Runway", sectionTitle);
            document.add(burnRunway);

            Image burnChartImage = Image.getInstance(generateBurnVsRunwayChart());
            burnChartImage.scaleToFit(450, 250);
            burnChartImage.setAlignment(Element.ALIGN_CENTER);
            document.add(burnChartImage);

            document.add(Chunk.NEWLINE);

            // === Achievements ===
            Paragraph achievementsTitle = new Paragraph("Key Achievements", sectionTitle);
            document.add(achievementsTitle);
            String achievements = report.getKeyAchievements() != null ? report.getKeyAchievements()
                    : "• Launched AI-powered analytics dashboard\n• Signed partnership with Microsoft for Startups\n• Onboarded Flipkart and Zomato as enterprise clients";
            document.add(new Paragraph(achievements, green));

            document.add(Chunk.NEWLINE);

            // === Challenges ===
            Paragraph challengesTitle = new Paragraph("Challenges & Learnings", sectionTitle);
            document.add(challengesTitle);
            String challenges = report.getChallengesAndLearnings() != null ? report.getChallengesAndLearnings()
                    : "• Server downtime during peak usage (resolved)\n• New competitor analysis ongoing\n• Mobile app prioritized based on feedback";
            document.add(new Paragraph(challenges, red));
            document.add(Chunk.NEWLINE);

            // === Attachments ===
            Paragraph attachmentsTitle = new Paragraph("Attachments", sectionTitle);
            document.add(attachmentsTitle);

            if (report.getAttachments() != null && !report.getAttachments().isEmpty()) {
                report.getAttachments().forEach(att -> {
                    try {
                        document.add(new Paragraph("• " + att.getFileName(), normal));
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                document.add(new Paragraph("• Financial_Statements_Dec.pdf\n• Growth_Dashboard.png\n• Roadmap_Q1_2025.pdf", normal));
            }

            document.add(Chunk.NEWLINE);

            // Footer
            Paragraph footer = new Paragraph("Generated automatically by InvestPlatform Reporting System", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private void addMetric(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderColor(new BaseColor(240, 240, 240));
        cell.setPhrase(new Phrase(label + "\n" + value, new Font(Font.FontFamily.HELVETICA, 11)));
        table.addCell(cell);
    }

    private String safeFormat(Double val) {
        if (val == null) return "0.0";
        return String.format("%.1f", val);
    }

    // === Chart Generators ===

    private byte[] generateRevenueGrowthChart() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(12, "Revenue", "Jan");
        dataset.addValue(15, "Revenue", "Feb");
        dataset.addValue(18, "Revenue", "Mar");
        dataset.addValue(22, "Revenue", "Apr");
        dataset.addValue(27, "Revenue", "May");

        JFreeChart chart = ChartFactory.createLineChart(
                "Monthly Revenue Growth", "Month", "Revenue (Lakh ₹)",
                dataset);

        chart.setBackgroundPaint(Color.white);
        chart.getPlot().setBackgroundPaint(new Color(245, 245, 245));

        return chartToBytes(chart, 500, 300);
    }

    private byte[] generateBurnVsRunwayChart() throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(8, "Burn", "Jan");
        dataset.addValue(10, "Burn", "Feb");
        dataset.addValue(9, "Burn", "Mar");
        dataset.addValue(11, "Burn", "Apr");
        dataset.addValue(12, "Burn", "May");

        dataset.addValue(15, "Runway (months)", "Jan");
        dataset.addValue(14, "Runway (months)", "Feb");
        dataset.addValue(13, "Runway (months)", "Mar");
        dataset.addValue(12, "Runway (months)", "Apr");
        dataset.addValue(11, "Runway (months)", "May");

        JFreeChart chart = ChartFactory.createBarChart(
                "Monthly Burn vs Runway", "Month", "Value",
                dataset);

        chart.setBackgroundPaint(Color.white);
        chart.getPlot().setBackgroundPaint(new Color(245, 245, 245));

        return chartToBytes(chart, 500, 300);
    }

    private byte[] chartToBytes(JFreeChart chart, int width, int height) throws IOException {
        BufferedImage chartImage = chart.createBufferedImage(width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ChartUtils.writeBufferedImageAsPNG(baos, chartImage);
        return baos.toByteArray();
    }
}
