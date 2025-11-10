package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.TimelyReport;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    public void sendTimelyReportEmail(
            TimelyReport report,
            String founderEmail,
            String investorEmail,
            String startupName) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(founderEmail);
        helper.setTo(investorEmail);
        helper.setSubject("New Timely Report from " + startupName);
        helper.setText(buildEmailBodyForTimelyReport(report, startupName));

        mailSender.send(message);
    }

    public void sendTimelyReportWithPdf(
            String founderEmail,
            String investorEmail,
            String startupName,
            TimelyReport report,
            byte[] pdfBytes,
            String pdfFileName
    ) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(founderEmail);
        helper.setTo(investorEmail);
        helper.setSubject("Monthly Report: " + startupName + " (" +
                (report.getReportingPeriod() != null ? report.getReportingPeriod() :
                        LocalDate.now().format(DateTimeFormatter.ofPattern("MMM yyyy"))) + ")");

        // ✉️ Email body
        helper.setText(buildEmailBodyForTimelyReport(report, startupName), false);

        // 📎 Attach PDF

        helper.addAttachment(pdfFileName, new ByteArrayResource(pdfBytes));

        mailSender.send(message);
    }

    /**
     * 🧩 Helper — Build readable email body
     */
    private String buildEmailBodyForTimelyReport(TimelyReport report, String startupName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Dear Investor,\n\n")
                .append("Please find attached the latest monthly report from *").append(startupName).append("*.\n\n")
                .append("Highlights:\n")
                .append("• Monthly Revenue: ₹").append(report.getMonthlyRevenue() != null ? report.getMonthlyRevenue() : "0").append("L\n")
                .append("• Monthly Burn: ₹").append(report.getMonthlyBurn() != null ? report.getMonthlyBurn() : "0").append("L\n")
                .append("• Cash Runway: ").append(report.getCashRunway() != null ? report.getCashRunway() + " months" : "N/A").append("\n")
                .append("• Team Size: ").append(report.getTeamSize() != null ? report.getTeamSize() : "N/A").append("\n\n")
                .append("Key Achievements:\n")
                .append(report.getKeyAchievements() != null ? report.getKeyAchievements() : "• Launched new analytics dashboard\n• Closed partnership with Microsoft for Startups\n\n")
                .append("Challenges & Learnings:\n")
                .append(report.getChallengesAndLearnings() != null ? report.getChallengesAndLearnings() : "• Server downtime resolved during peak hours\n• Mobile app updates in progress\n\n")
                .append("Best Regards,\n")
                .append(startupName)
                .append("\n\n— Powered by InvestPlatform");

        return sb.toString();
    }

    // private String buildEmailBodyForTimelyReport(TimelyReport report, String startupName) {
    //     StringBuilder sb = new StringBuilder();
    //     sb.append("Hello,\n\n");
    //     sb.append("You have received a new update from ").append(startupName).append(":\n\n");
    //     sb.append("📊 Title: ").append(report.getTitle()).append("\n");
    //     sb.append("🗓 Reporting Period: ").append(report.getReportingPeriod()).append("\n\n");
    //     sb.append("💰 Monthly Revenue: ₹").append(report.getMonthlyRevenue()).append("\n");
    //     sb.append("🔥 Monthly Burn: ₹").append(report.getMonthlyBurn()).append("\n");
    //     sb.append("💵 Cash Runway: ").append(report.getCashRunway()).append(" months\n");
    //     sb.append("👥 Team Size: ").append(report.getTeamSize()).append("\n\n");
    //     sb.append("🏆 Key Achievements:\n").append(report.getKeyAchievements()).append("\n\n");
    //     sb.append("⚙️ Challenges & Learnings:\n").append(report.getChallengesAndLearnings()).append("\n\n");
    //     sb.append("📈 Other Metrics:\n").append(report.getOtherKeyMetrics()).append("\n\n");
    //     sb.append("🙏 Asks from Investors:\n").append(report.getAsksFromInvestors()).append("\n\n");
    //     sb.append("---\nSent via Logicleaf Investor Platform");
    //     return sb.toString();
    // }

    public void sendConnectionEmail(String toEmail, String senderName, String mappingId, boolean fromFounder)
            throws MessagingException {

        String subject = fromFounder
                ? senderName + " has invited you to connect!"
                : senderName + " wants to connect with your startup!";

        String acceptUrl = baseUrl + "/api/" + (fromFounder ? "founder" : "investor") 
        + "/connections/" + mappingId + (fromFounder ? "/approve" : "/accept");
        String rejectUrl = baseUrl + "/api/" + (fromFounder ? "investor" : "founder") + "/connections/" + mappingId
                + "/reject";

        String html = """
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <h2>New Invitation</h2>
                        <p>%s</p>
                        <p>
                            <a href="%s" style="background-color:#28a745;color:white;padding:10px 15px;text-decoration:none;border-radius:5px;">Accept</a>
                            <a href="%s" style="background-color:#dc3545;color:white;padding:10px 15px;text-decoration:none;border-radius:5px;">Reject</a>
                        </p>
                        <p style="margin-top:20px;color:gray;font-size:12px;">This is an automated message. Please do not reply.</p>
                    </body>
                    </html>
                """
                .formatted(subject, acceptUrl, rejectUrl);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(message);
    }

    public void sendRejectionEmail(String toEmail, String rejectedByName, String startupName, boolean byFounder)
            throws MessagingException {

        String subject = byFounder
                ? "Your request was declined by " + startupName
                : "Your invitation was declined by " + rejectedByName;

        String body = byFounder
                ? String.format("""
                            <html>
                            <body style="font-family: Arial, sans-serif;">
                                <h2>Request Declined</h2>
                                <p>We're sorry, but your request to connect with <b>%s</b> has been declined.</p>
                                <p style="color:gray;">You can explore other startups to invest in on our platform.</p>
                            </body>
                            </html>
                        """, startupName)
                : String.format(
                        """
                                    <html>
                                    <body style="font-family: Arial, sans-serif;">
                                        <h2>Invitation Declined</h2>
                                        <p>We're sorry, but <b>%s</b> has declined your invitation to connect with <b>%s</b>.</p>
                                        <p style="color:gray;">You can invite other investors anytime.</p>
                                    </body>
                                    </html>
                                """,
                        rejectedByName, startupName);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    public void sendConnectionStatusEmail(String fromEmail,
                                          String fromName,
                                          String toEmail,
                                          String startupName,
                                          String statusAction,
                                          String actorRole) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String subject = switch (statusAction.toLowerCase()) {
                case "approved" -> "Request Approved";
                case "accepted" -> "Invitation Accepted";
                case "rejected" -> "Request Rejected";
                default -> "Connection Update";
            };

            String title = switch (statusAction.toLowerCase()) {
                case "approved" -> "Your request for Connection Has Been Approved!";
                case "accepted" -> "Your Invitation Has Been Accepted!";
                case "rejected" -> "Your request for Connection Has Been Declined.";
                default -> "Connection Update";
            };

            String message = "%s (%s) has %s your connection for %s."
                    .formatted(fromName, actorRole, statusAction, startupName);

            String html = """
                <div style="font-family: Arial, sans-serif; background-color:#f7f8fa; padding:30px;">
                  <div style="max-width:600px; margin:auto; background:white; padding:25px; border-radius:10px; box-shadow:0 2px 6px rgba(0,0,0,0.1);">
                    <h2 style="color:#2E86C1; text-align:center;">%s</h2>
                    <p style="font-size:16px; color:#333; text-align:center;">%s</p>
                    <div style="text-align:center; margin-top:20px;">
                      <a href="https://logicleaf.com/dashboard"
                         style="background-color:#2E86C1; color:white; padding:10px 20px; text-decoration:none; border-radius:5px;">
                         View Dashboard
                      </a>
                    </div>
                    <hr style="border:none; border-top:1px solid #eee; margin:25px 0;">
                    <p style="color:#999; font-size:12px; text-align:center;">
                      Sent directly from %s via Logicleaf Investment Platform.
                    </p>
                  </div>
                </div>
                """.formatted(title, message, fromName);

            helper.setText(html, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setFrom(fromEmail);

            mailSender.send(mimeMessage);
            System.out.println("✅ Connection status email sent from %s to %s".formatted(fromEmail, toEmail));

        } catch (Exception e) {
            System.err.println("❌ Failed to send connection status email: " + e.getMessage());
        }
    }
}
