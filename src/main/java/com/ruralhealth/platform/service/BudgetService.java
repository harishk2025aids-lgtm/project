package com.ruralhealth.platform.service;

import com.ruralhealth.platform.dto.BudgetUtilizationReport;
import com.ruralhealth.platform.entity.Budget;
import com.ruralhealth.platform.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public Budget allocate(Budget budget) {
        return budgetRepository.save(budget);
    }

    /** Record spend against a department's budget line for a given month, e.g. from a received purchase order. */
    public Budget recordSpend(Long budgetId, BigDecimal amount) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));
        budget.setSpentAmount(budget.getSpentAmount().add(amount));
        return budgetRepository.save(budget);
    }

    public List<BudgetUtilizationReport> utilizationReport(Integer fiscalYear, Integer fiscalMonth) {
        List<Budget> budgets = budgetRepository.findByFiscalYearAndFiscalMonth(fiscalYear, fiscalMonth);
        return budgets.stream().map(b -> {
            BigDecimal remaining = b.getAllocatedAmount().subtract(b.getSpentAmount());
            double pct = b.getAllocatedAmount().compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                    b.getSpentAmount().divide(b.getAllocatedAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
            return new BudgetUtilizationReport(
                    b.getDepartment().getName(), b.getFiscalYear(), b.getFiscalMonth(),
                    b.getAllocatedAmount(), b.getSpentAmount(), remaining, pct
            );
        }).toList();
    }
}
