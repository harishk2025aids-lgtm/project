package com.ruralhealth.platform.service;

import com.ruralhealth.platform.dto.BalanceSheetReport;
import com.ruralhealth.platform.dto.ProfitLossReport;
import com.ruralhealth.platform.entity.ChartOfAccount;
import com.ruralhealth.platform.entity.JournalEntry;
import com.ruralhealth.platform.entity.JournalLine;
import com.ruralhealth.platform.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Derives standard financial statements directly from the double-entry
 * journal (chart_of_accounts + journal_entries + journal_lines), the same
 * ledger that invoices, payments, and purchase orders post into.
 */
@Service
@SuppressWarnings("null")
public class ReportService {

    private final JournalEntryRepository journalEntryRepository;

    public ReportService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    /** Net movement (debits - credits, or credits - debits depending on account type) per account. */
    private Map<ChartOfAccount, BigDecimal> netBalancesByType(List<String> accountTypes, boolean creditPositive) {
        Map<ChartOfAccount, BigDecimal> balances = new LinkedHashMap<>();
        List<JournalEntry> entries = journalEntryRepository.findAll();

        for (JournalEntry entry : entries) {
            for (JournalLine line : entry.getLines()) {
                ChartOfAccount account = line.getAccount();
                if (!accountTypes.contains(account.getAccountType())) continue;

                BigDecimal delta = creditPositive
                        ? line.getCreditAmount().subtract(line.getDebitAmount())
                        : line.getDebitAmount().subtract(line.getCreditAmount());

                balances.merge(account, delta, BigDecimal::add);
            }
        }
        return balances;
    }

    public ProfitLossReport profitAndLoss(Integer fiscalYear) {
        Map<ChartOfAccount, BigDecimal> revenue = netBalancesByType(List.of("REVENUE"), true);
        Map<ChartOfAccount, BigDecimal> expenses = netBalancesByType(List.of("EXPENSE"), false);

        Map<String, BigDecimal> revenueByName = new LinkedHashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (var e : revenue.entrySet()) {
            revenueByName.put(e.getKey().getAccountName(), e.getValue());
            totalRevenue = totalRevenue.add(e.getValue());
        }

        Map<String, BigDecimal> expensesByName = new LinkedHashMap<>();
        BigDecimal totalExpenses = BigDecimal.ZERO;
        for (var e : expenses.entrySet()) {
            expensesByName.put(e.getKey().getAccountName(), e.getValue());
            totalExpenses = totalExpenses.add(e.getValue());
        }

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);
        return new ProfitLossReport(fiscalYear, revenueByName, totalRevenue, expensesByName, totalExpenses, netProfit);
    }

    public BalanceSheetReport balanceSheet() {
        Map<ChartOfAccount, BigDecimal> assets = netBalancesByType(List.of("ASSET"), false);
        Map<ChartOfAccount, BigDecimal> liabilities = netBalancesByType(List.of("LIABILITY"), true);
        Map<ChartOfAccount, BigDecimal> equity = netBalancesByType(List.of("EQUITY"), true);

        Map<String, BigDecimal> assetMap = new LinkedHashMap<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        for (var e : assets.entrySet()) { assetMap.put(e.getKey().getAccountName(), e.getValue()); totalAssets = totalAssets.add(e.getValue()); }

        Map<String, BigDecimal> liabilityMap = new LinkedHashMap<>();
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        for (var e : liabilities.entrySet()) { liabilityMap.put(e.getKey().getAccountName(), e.getValue()); totalLiabilities = totalLiabilities.add(e.getValue()); }

        // Retained earnings (net profit to date) is folded into equity so the sheet balances.
        ProfitLossReport pl = profitAndLoss(null);
        Map<String, BigDecimal> equityMap = new LinkedHashMap<>();
        BigDecimal totalEquity = BigDecimal.ZERO;
        for (var e : equity.entrySet()) { equityMap.put(e.getKey().getAccountName(), e.getValue()); totalEquity = totalEquity.add(e.getValue()); }
        equityMap.put("Retained Earnings", pl.getNetProfit());
        totalEquity = totalEquity.add(pl.getNetProfit());

        return new BalanceSheetReport(assetMap, totalAssets, liabilityMap, totalLiabilities, equityMap, totalEquity);
    }
}
