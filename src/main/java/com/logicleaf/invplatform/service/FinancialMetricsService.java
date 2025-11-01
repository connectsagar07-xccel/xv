package com.logicleaf.invplatform.service;

import com.logicleaf.invplatform.model.FinancialMetrics;
import com.logicleaf.invplatform.repository.FinancialMetricsRepository;
import com.invplatform.generated.salesorder.api.SalesOrderApi;
import com.invplatform.generated.salesorder.model.ListSalesOrdersResponseSalesordersInner;
import com.invplatform.generated.expenses.api.ExpensesApi;
import com.invplatform.generated.expenses.model.ListExpensesResponseExpensesInner;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FinancialMetricsService {

    private final FinancialMetricsRepository financialMetricsRepository;
    private final SalesOrderApi salesOrderApi;
    private final ExpensesApi expensesApi;

    public FinancialMetricsService(FinancialMetricsRepository financialMetricsRepository,
                                   SalesOrderApi salesOrderApi,
                                   ExpensesApi expensesApi) {
        this.financialMetricsRepository = financialMetricsRepository;
        this.salesOrderApi = salesOrderApi;
        this.expensesApi = expensesApi;
    }

    public FinancialMetrics calculateAndSaveFinancialMetrics(String startupId, String month, String organizationId) {
        List<ListSalesOrdersResponseSalesordersInner> salesOrders = fetchMonthlySalesOrders(organizationId, month);
        List<ListExpensesResponseExpensesInner> expenses = fetchMonthlyExpenses(organizationId, month);

        double monthlyRevenue = salesOrders.stream().mapToDouble(ListSalesOrdersResponseSalesordersInner::getTotal).sum();
        double monthlyExpenses = expenses.stream().mapToDouble(ListExpensesResponseExpensesInner::getTotal).sum();
        double cogs = 0.0; // Placeholder
        double grossProfit = monthlyRevenue - cogs;
        double grossMarginPercentage = (monthlyRevenue > 0) ? (grossProfit / monthlyRevenue) * 100 : 0;
        double netProfit = monthlyRevenue - monthlyExpenses;
        double netMarginPercentage = (monthlyRevenue > 0) ? (netProfit / monthlyRevenue) * 100 : 0;
        double burnRate = (netProfit < 0) ? Math.abs(netProfit) : 0;

        FinancialMetrics metrics = FinancialMetrics.builder()
                .startupId(startupId)
                .month(month)
                .monthlyRevenue(monthlyRevenue)
                .monthlyExpenses(monthlyExpenses)
                .cogs(cogs)
                .grossProfit(grossProfit)
                .grossMarginPercentage(grossMarginPercentage)
                .netProfit(netProfit)
                .netMarginPercentage(netMarginPercentage)
                .burnRate(burnRate)
                .mrr(null) // Placeholder
                .arr(null)   // Placeholder
                .calculatedAt(LocalDateTime.now())
                .build();

        return financialMetricsRepository.save(metrics);
    }

    private List<ListSalesOrdersResponseSalesordersInner> fetchMonthlySalesOrders(String organizationId, String month) {
        List<ListSalesOrdersResponseSalesordersInner> allSalesOrders = new ArrayList<>();
        int page = 1;
        boolean hasMorePages = true;

        while (hasMorePages) {
            try {
                var salesOrderList = salesOrderApi.listSalesOrders(organizationId, null, null, null, null, null, null, null, null, null, null, month, null, null, null, null, null, null, null, null, null, null, page, null);
                if (salesOrderList != null && !salesOrderList.getSalesorders().isEmpty()) {
                    allSalesOrders.addAll(salesOrderList.getSalesorders());
                    // The SalesOrder API response does not have pagination details, so we assume there's only one page.
                    hasMorePages = false;
                } else {
                    hasMorePages = false;
                }
            } catch (Exception e) {
                // Proper error handling should be implemented.
                throw new RuntimeException("Error fetching sales orders from Zoho: " + e.getMessage(), e);
            }
        }
        return allSalesOrders;
    }

    private List<ListExpensesResponseExpensesInner> fetchMonthlyExpenses(String organizationId, String month) {
        List<ListExpensesResponseExpensesInner> allExpenses = new ArrayList<>();
        int page = 1;
        boolean hasMorePages = true;

        while (hasMorePages) {
            try {
                 var expenseList = expensesApi.listExpenses(organizationId, null, null, month, null, null, null, null, null, null, null, null, null, null, null, null, page, null);
                if (expenseList != null && !expenseList.getExpenses().isEmpty()) {
                    allExpenses.addAll(expenseList.getExpenses());
                    if (expenseList.getPageContext() != null && !expenseList.getPageContext().isEmpty() && expenseList.getPageContext().get(0).getPage() != null) {
                        page++;
                    } else {
                        hasMorePages = false;
                    }
                } else {
                    hasMorePages = false;
                }
            } catch (Exception e) {
                throw new RuntimeException("Error fetching expenses from Zoho: " + e.getMessage(), e);
            }
        }
        return allExpenses;
    }
}
