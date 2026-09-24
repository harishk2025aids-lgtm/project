package com.ruralhealth.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossReport {
    private Integer fiscalYear;
    private Map<String, BigDecimal> revenueByAccount;
    private BigDecimal totalRevenue;
    private Map<String, BigDecimal> expensesByAccount;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
}
