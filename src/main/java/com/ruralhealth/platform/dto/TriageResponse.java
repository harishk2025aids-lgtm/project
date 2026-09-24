package com.ruralhealth.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriageResponse {
    private Long consultationId;
    private BigDecimal triageScore;
    private String triagePriority; // EMERGENCY, URGENT, NORMAL
    private List<String> flaggedReasons;
}
