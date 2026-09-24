package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long invoiceId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

    private LocalDateTime invoiceDate = LocalDateTime.now();

    @Column(precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private String status = "UNPAID"; // UNPAID, PARTIAL, PAID

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();
}
