package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "consultations")
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long consultationId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(length = 2000)
    private String symptoms;

    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Double temperatureCelsius;
    private Integer spo2;
    private Integer respiratoryRate;

    @Column(precision = 5, scale = 2)
    private BigDecimal triageScore;

    private String triagePriority; // EMERGENCY, URGENT, NORMAL
    private String status = "WAITING"; // WAITING, IN_PROGRESS, COMPLETED

    private LocalDateTime createdAt = LocalDateTime.now();
}
