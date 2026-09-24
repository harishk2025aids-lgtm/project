package com.ruralhealth.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetUtilizationReport {
    private String departmentName;
    private Integer fiscalYear;
    private Integer fiscalMonth;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private Double utilizationPercent;
}
