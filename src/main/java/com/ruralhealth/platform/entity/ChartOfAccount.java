package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "chart_of_accounts")
public class ChartOfAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long accountId;

    @Column(nullable = false, unique = true, length = 20)
    private String accountCode;

    @Column(nullable = false, length = 150)
    private String accountName;

    @Column(nullable = false, length = 20)
    private String accountType; // ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
}
