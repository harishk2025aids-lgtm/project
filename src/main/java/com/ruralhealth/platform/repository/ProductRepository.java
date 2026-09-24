package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
