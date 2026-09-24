package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
