package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.dto.BudgetUtilizationReport;
import com.ruralhealth.platform.entity.Budget;
import com.ruralhealth.platform.repository.BudgetRepository;
import com.ruralhealth.platform.service.BudgetService;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    public BudgetController(BudgetRepository budgetRepository, BudgetService budgetService) {
        this.budgetRepository = budgetRepository;
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<Budget> getAll() {
        return budgetRepository.findAll();
    }

    /** Allocate a department's monthly budget. */
    @PostMapping
    public Budget allocate(@NonNull @RequestBody Budget budget) {
        return budgetService.allocate(budget);
    }

    /** Record spend against a budget line, e.g. after a purchase order is received. */
    @PatchMapping("/{id}/spend")
    public Budget recordSpend(@NonNull @PathVariable Long id, @NonNull @RequestBody Map<String, BigDecimal> body) {
        return budgetService.recordSpend(id, body.get("amount"));
    }

    /** Stage 5: departmental budget utilization report for a given month. */
    @GetMapping("/utilization")
    public List<BudgetUtilizationReport> utilization(@RequestParam Integer year, @RequestParam Integer month) {
        return budgetService.utilizationReport(year, month);
    }
}
