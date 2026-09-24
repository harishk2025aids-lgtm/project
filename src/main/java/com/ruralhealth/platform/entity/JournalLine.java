package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "journal_lines")
public class JournalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long journalLineId;

    @ManyToOne
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccount account;

    @Column(precision = 14, scale = 2)
    private BigDecimal debitAmount = BigDecimal.ZERO;

    @Column(precision = 14, scale = 2)
    private BigDecimal creditAmount = BigDecimal.ZERO;
}
