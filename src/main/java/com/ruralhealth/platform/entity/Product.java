package com.ruralhealth.platform.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @Column(nullable = false, length = 150)
    private String name;

    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    private Boolean isService = true;
}
