package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "budgets")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long budgetId;

    @ManyToOne
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private Integer fiscalYear;

    @Column(nullable = false)
    private Integer fiscalMonth;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal allocatedAmount;

    @Column(precision = 14, scale = 2)
    private BigDecimal spentAmount = BigDecimal.ZERO;
}
