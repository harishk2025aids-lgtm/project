package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByInvoice_InvoiceId(Long invoiceId);
}
