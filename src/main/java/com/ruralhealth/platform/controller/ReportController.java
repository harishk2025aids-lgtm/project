package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.dto.BalanceSheetReport;
import com.ruralhealth.platform.dto.ProfitLossReport;
import com.ruralhealth.platform.service.ReportService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Stage 5: Profit & Loss statement derived from the journal ledger. */
    @GetMapping("/profit-loss")
    public ProfitLossReport profitAndLoss(@RequestParam(required = false) Integer year) {
        return reportService.profitAndLoss(year);
    }

    /** Stage 5: Balance Sheet derived from the journal ledger. */
    @GetMapping("/balance-sheet")
    public BalanceSheetReport balanceSheet() {
        return reportService.balanceSheet();
    }
}
