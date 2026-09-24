package com.ruralhealth.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceSheetReport {
    private Map<String, BigDecimal> assets;
    private BigDecimal totalAssets;
    private Map<String, BigDecimal> liabilities;
    private BigDecimal totalLiabilities;
    private Map<String, BigDecimal> equity;
    private BigDecimal totalEquity;
}
