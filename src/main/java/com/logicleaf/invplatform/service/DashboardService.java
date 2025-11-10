package com.logicleaf.invplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.logicleaf.invplatform.dto.FounderDashboardResponse;
import com.logicleaf.invplatform.dto.MonthlyMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DashboardService {

        @Autowired
        private ZohoService zohoService;

        public FounderDashboardResponse getFounderDashboardData(String founderEmail) {
                // Get past 6 months range
                LocalDate now = LocalDate.now();
                List<LocalDate> last6Months = IntStream.rangeClosed(0, 5)
                                .mapToObj(i -> now.minusMonths(5 - i))
                                .collect(Collectors.toList());

                List<MonthlyMetric> revenueData = new ArrayList<>();
                List<MonthlyMetric> expenseData = new ArrayList<>();
                List<MonthlyMetric> burnRateData = new ArrayList<>();

                for (LocalDate month : last6Months) {
                        int year = month.getYear();
                        int m = month.getMonthValue();

                        double monthlyRevenue = fetchMonthlyRevenue(founderEmail, year, m);
                        double monthlyExpense = fetchMonthlyExpenses(founderEmail, year, m);

                        // Compute net profit
                        double netProfit = monthlyRevenue - monthlyExpense;

                        // Compute burn rate (if loss)
                        double burnRate = netProfit < 0 ? Math.abs(netProfit) : 0.0;

                        String monthName = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

                        revenueData.add(new MonthlyMetric(monthName, monthlyRevenue));
                        expenseData.add(new MonthlyMetric(monthName, monthlyExpense));
                        burnRateData.add(new MonthlyMetric(monthName, burnRate));
                }

                int cashRunwayMonths = (int) (14 + new Random().nextInt(3)); 
                int teamSize = getZohoUserCount(founderEmail);

                // KPI donut sample (mock)
                Map<String, Double> kpi = new LinkedHashMap<>();
                kpi.put("NPS", 30.0);
                kpi.put("Churn", 15.0);
                kpi.put("LTV", 35.0);
                kpi.put("CAC", 20.0);

                // Monthly goal progress (sample)
                Map<String, Integer> goals = Map.of(
                                "Revenue Target", 83,
                                "Customer Acquisition", 67,
                                "Product Development", 92);

                return FounderDashboardResponse.builder()
                                .cashRunwayMonths(cashRunwayMonths)
                                .revenueGrowth(revenueData)
                                .burnRateAnalysis(burnRateData)
                                .keyPerformanceIndicators(kpi)
                                .monthlyGoalsProgress(goals)
                                .teamSize(teamSize)
                                .build();

        }

        private double fetchMonthlyRevenue(String founderEmail, int year, int month) {
                try {
                        JsonNode sales = zohoService.fetchSalesOrdersForFounder(
                                        founderEmail,
                                        String.format("%04d-%02d-01", year, month),
                                        String.format("%04d-%02d-%02d", year, month,
                                                        LocalDate.of(year, month, 1).lengthOfMonth()));
                        if (sales.has("salesorders")) {
                                double total = 0.0;
                                for (JsonNode order : sales.get("salesorders")) {
                                        total += order.path("total").asDouble(0.0);
                                }
                                return total;
                        }
                } catch (Exception e) {
                        System.out.println("⚠️ Using sample revenue for " + year + "-" + month);
                }
                // fallback data
                return switch (month) {
                        case 7 -> 6000.0;
                        case 8 -> 7000.0;
                        case 9 -> 8000.0;
                        case 10 -> 9000.0;
                        case 11 -> 10000.0;
                        case 12 -> 11000.0;
                        default -> 12000.5;
                };
        }

        private double fetchMonthlyExpenses(String founderEmail, int year, int month) {
                try {
                        JsonNode expenses = zohoService.fetchMonthlyExpensesForFounder(founderEmail, year, month);
                        if (expenses.has("expenses")) {
                                double total = 0.0;
                                for (JsonNode exp : expenses.get("expenses")) {
                                        total += exp.path("total").asDouble(0.0);
                                }
                                return total;
                        }
                } catch (Exception e) {
                        System.out.println("⚠️ Using sample expenses for " + year + "-" + month);
                }
                // fallback
                return switch (month) {
                        case 7 -> 10000.0;
                        case 8 -> 9000.5;
                        case 9 -> 9000.0;
                        case 10 -> 8000.6;
                        case 11 -> 8000.3;
                        case 12 -> 8000.0;
                        default -> 8000.2;
                };
        }

        public int getZohoUserCount(String founderEmail) {
                // Call the raw Zoho API
                JsonNode response = zohoService.fetchEmployeesFromZoho(founderEmail,1,200);

                // Extract users array
                JsonNode users = response.path("employees");

                // Return count safely
                if (users.isArray()) {
                        return users.size();
                }

                return 0;
        }
}
