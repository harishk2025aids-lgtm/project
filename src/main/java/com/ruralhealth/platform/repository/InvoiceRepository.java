package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatient_PatientIdOrderByInvoiceDateDesc(Long patientId);
    List<Invoice> findByStatus(String status);
}
