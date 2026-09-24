package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.entity.Payment;
import com.ruralhealth.platform.service.BillingService;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final BillingService billingService;

    public PaymentController(BillingService billingService) {
        this.billingService = billingService;
    }

    public record PaymentRequest(Long invoiceId, BigDecimal amount, String method) {}

    @PostMapping
    public Payment recordPayment(@NonNull @RequestBody PaymentRequest req) {
        return billingService.recordPayment(java.util.Objects.requireNonNull(req.invoiceId()), req.amount(), req.method());
    }
}
