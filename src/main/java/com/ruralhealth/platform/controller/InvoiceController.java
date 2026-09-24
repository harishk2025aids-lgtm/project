package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.entity.Invoice;
import com.ruralhealth.platform.repository.InvoiceRepository;
import com.ruralhealth.platform.service.BillingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final BillingService billingService;

    public InvoiceController(InvoiceRepository invoiceRepository, BillingService billingService) {
        this.invoiceRepository = invoiceRepository;
        this.billingService = billingService;
    }

    public record InvoiceLineDto(Long productId, Integer quantity) {}
    public record CreateInvoiceRequest(Long patientId, Long consultationId, List<InvoiceLineDto> lines) {}

    @GetMapping
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getById(@PathVariable Long id) {
        return invoiceRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public List<Invoice> getByPatient(@PathVariable Long patientId) {
        return invoiceRepository.findByPatient_PatientIdOrderByInvoiceDateDesc(patientId);
    }

    /** Diagnostic service billing: creates an invoice from a list of billable products/services. */
    @PostMapping
    public Invoice create(@RequestBody CreateInvoiceRequest req) {
        List<BillingService.InvoiceLineRequest> lines = req.lines().stream()
                .map(l -> new BillingService.InvoiceLineRequest(l.productId(), l.quantity()))
                .toList();
        return billingService.createInvoice(req.patientId(), req.consultationId(), lines);
    }
}
