package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {
}
