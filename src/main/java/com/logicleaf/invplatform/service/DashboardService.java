package com.logicleaf.invplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.logicleaf.invplatform.dto.FounderDashboardResponse;
import com.logicleaf.invplatform.dto.MonthlyMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DashboardService {

        @Autowired
        private ZohoService zohoService;

        public FounderDashboardResponse getFounderDashboardData(String founderEmail) {
                LocalDate now = LocalDate.now();
                LocalDate startDate = now.minusMonths(5).withDayOfMonth(1);
                LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String startDateStr = startDate.format(dateFormatter);
                String endDateStr = endDate.format(dateFormatter);

                // Fetch Zoho data in one request each
                JsonNode salesOrders = zohoService.fetchSalesOrdersForFounder(founderEmail, startDateStr, endDateStr);
                JsonNode expenses = zohoService.fetchExpensesForFounder(founderEmail, startDateStr, endDateStr);

                // Group by month (like "Jun", "Sep")
                Map<String, Double> monthlyRevenueMap = groupByMonthShortName(salesOrders, "salesorders", "total");
                Map<String, Double> monthlyExpenseMap = groupByMonthShortName(expenses, "expenses", "total");

                List<LocalDate> last6Months = IntStream.rangeClosed(0, 5)
                                .mapToObj(i -> now.minusMonths(5 - i).withDayOfMonth(1))
                                .collect(Collectors.toList());

                List<MonthlyMetric> revenueData = new ArrayList<>();
                List<MonthlyMetric> expenseData = new ArrayList<>();
                List<MonthlyMetric> burnRateData = new ArrayList<>();

                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

                for (LocalDate monthStart : last6Months) {
                        String monthKey = monthStart.format(monthFormatter);
                        double monthlyRevenue = monthlyRevenueMap.getOrDefault(monthKey, 0.0);
                        double monthlyExpense = monthlyExpenseMap.getOrDefault(monthKey, 0.0);

                        double netProfit = monthlyRevenue - monthlyExpense;
                        double burnRate = netProfit < 0 ? Math.abs(netProfit) : 0.0;

                        revenueData.add(new MonthlyMetric(monthKey, monthlyRevenue));
                        expenseData.add(new MonthlyMetric(monthKey, monthlyExpense));
                        burnRateData.add(new MonthlyMetric(monthKey, burnRate));
                }

                int cashRunwayMonths = (int) (14 + new Random().nextInt(3));
                int teamSize = getZohoUserCount(founderEmail);

                Map<String, Double> kpi = new LinkedHashMap<>();
                kpi.put("NPS", 30.0);
                kpi.put("Churn", 2.0);
                kpi.put("LTV", 35.0);
                kpi.put("CAC", 10.0);

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

        private Map<String, Double> groupByMonthShortName(JsonNode root, String arrayKey, String amountKey) {
                Map<String, Double> monthMap = new HashMap<>();
                if (root == null || !root.has(arrayKey))
                        return monthMap;

                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);

                for (JsonNode entry : root.get(arrayKey)) {
                        if (entry.has("date") && entry.has(amountKey)) {
                                String dateStr = entry.get("date").asText();
                                double amount = entry.get(amountKey).asDouble(0.0);
                                try {
                                        LocalDate date = LocalDate.parse(dateStr);
                                        String monthKey = date.format(monthFormatter);
                                        monthMap.merge(monthKey, amount, Double::sum);
                                } catch (Exception ignored) {
                                }
                        }
                }
                return monthMap;
        }

        public int getZohoUserCount(String founderEmail) {
                // Call the raw Zoho API
                JsonNode response = zohoService.fetchEmployeesFromZoho(founderEmail, 1, 200);

                // Extract users array
                JsonNode users = response.path("employees");

                // Return count safely
                if (users.isArray()) {
                        return users.size();
                }

                return 0;
        }
}
