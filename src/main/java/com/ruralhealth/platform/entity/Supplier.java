package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long supplierId;

    @Column(nullable = false, length = 150)
    private String name;

    private String contactPerson;
    private String phone;
    private String email;
}
