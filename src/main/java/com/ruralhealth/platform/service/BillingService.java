package com.ruralhealth.platform.service;

import com.ruralhealth.platform.entity.*;
import com.ruralhealth.platform.repository.*;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final ConsultationRepository consultationRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final JournalService journalService;

    public BillingService(InvoiceRepository invoiceRepository, PatientRepository patientRepository,
                           ConsultationRepository consultationRepository, ProductRepository productRepository,
                           PaymentRepository paymentRepository, JournalService journalService) {
        this.invoiceRepository = invoiceRepository;
        this.patientRepository = patientRepository;
        this.consultationRepository = consultationRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.journalService = journalService;
    }

    public record InvoiceLineRequest(Long productId, Integer quantity) {}

    @Transactional
    public Invoice createInvoice(@NonNull Long patientId, Long consultationId, @NonNull List<InvoiceLineRequest> lines) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        Invoice invoice = new Invoice();
        invoice.setPatient(patient);

        if (consultationId != null) {
            Consultation consultation = consultationRepository.findById(consultationId)
                    .orElseThrow(() -> new IllegalArgumentException("Consultation not found: " + consultationId));
            invoice.setConsultation(consultation);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (InvoiceLineRequest line : lines) {
            Product product = productRepository.findById(java.util.Objects.requireNonNull(line.productId()))
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.productId()));

            InvoiceItem item = new InvoiceItem();
            item.setInvoice(invoice);
            item.setProduct(product);
            item.setQuantity(line.quantity());
            item.setUnitPrice(product.getUnitPrice());
            BigDecimal lineTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(line.quantity()));
            item.setLineTotal(lineTotal);

            invoice.getItems().add(item);
            total = total.add(lineTotal);
        }

        invoice.setTotalAmount(total);
        invoice.setStatus("UNPAID");
        invoice = invoiceRepository.save(invoice);

        // Post revenue recognition: Debit Accounts Receivable, Credit Diagnostic/Consultation Revenue
        journalService.postEntry(
                "Invoice #" + invoice.getInvoiceId() + " issued to patient " + patient.getFullName(),
                "INVOICE", invoice.getInvoiceId(),
                "1100", "4100", total
        );

        return invoice;
    }

    @Transactional
    public Payment recordPayment(@NonNull Long invoiceId, BigDecimal amount, String method) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmountPaid(amount);
        payment.setPaymentMethod(method);
        payment = paymentRepository.save(payment);

        BigDecimal totalPaid = paymentRepository.findByInvoice_InvoiceId(invoiceId).stream()
                .map(paymentRecord -> paymentRecord.getAmountPaid())
                .reduce(BigDecimal.ZERO, (left, right) -> left.add(right));

        if (totalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIAL");
        }
        invoiceRepository.save(invoice);

        // Post cash receipt: Debit Cash, Credit Accounts Receivable
        journalService.postEntry(
                "Payment received for invoice #" + invoiceId,
                "PAYMENT", payment.getPaymentId(),
                "1000", "1100", amount
        );

        return payment;
    }
}
