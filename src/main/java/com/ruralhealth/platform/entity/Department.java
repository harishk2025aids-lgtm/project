package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    @Column(nullable = false, length = 120)
    private String name;

    private String location;

    @Column(precision = 14, scale = 2)
    private BigDecimal monthlyBudget = BigDecimal.ZERO;
}
