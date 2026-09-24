package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    @Column(nullable = false, length = 120)
    private String fullName;

    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private String bloodGroup;
    private String emergencyContact;

    private LocalDateTime createdAt = LocalDateTime.now();
}
