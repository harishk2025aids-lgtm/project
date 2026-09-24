package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long journalEntryId;

    private LocalDateTime entryDate = LocalDateTime.now();
    private String description;
    private String referenceType; // INVOICE, PAYMENT, PURCHASE_ORDER, MANUAL
    private Long referenceId;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalLine> lines = new ArrayList<>();
}
