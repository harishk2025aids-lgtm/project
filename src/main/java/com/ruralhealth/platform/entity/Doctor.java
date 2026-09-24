package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long doctorId;

    @Column(nullable = false, length = 120)
    private String fullName;

    private String specialization;
    private String phone;
    private String email;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;
}
