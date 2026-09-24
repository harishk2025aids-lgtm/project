package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.entity.*;
import com.ruralhealth.platform.repository.*;
import com.ruralhealth.platform.service.JournalService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final JournalService journalService;

    public PurchaseOrderController(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository,
                                    ProductRepository productRepository, JournalService journalService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.journalService = journalService;
    }

    public record PurchaseLineDto(Long productId, Integer quantity, BigDecimal unitCost) {}
    public record CreatePurchaseOrderRequest(Long supplierId, List<PurchaseLineDto> lines) {}

    @GetMapping
    public List<PurchaseOrder> getAll() {
        return purchaseOrderRepository.findAll();
    }

    @PostMapping
    @Transactional
    public PurchaseOrder create(@RequestBody CreatePurchaseOrderRequest req) {
        Supplier supplier = supplierRepository.findById(req.supplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + req.supplierId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseLineDto line : req.lines()) {
            Product product = productRepository.findById(line.productId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + line.productId()));

            PurchaseItem item = new PurchaseItem();
            item.setPurchaseOrder(po);
            item.setProduct(product);
            item.setQuantity(line.quantity());
            item.setUnitCost(line.unitCost());
            BigDecimal lineTotal = line.unitCost().multiply(BigDecimal.valueOf(line.quantity()));
            item.setLineTotal(lineTotal);

            po.getItems().add(item);
            total = total.add(lineTotal);
        }
        po.setTotalAmount(total);
        return purchaseOrderRepository.save(po);
    }

    /** Mark a PO as received: posts Debit Medical Supplies Expense / Credit Cash (or Accounts Payable). */
    @PatchMapping("/{id}/receive")
    @Transactional
    public ResponseEntity<PurchaseOrder> receive(@PathVariable Long id) {
        return purchaseOrderRepository.findById(id).map(po -> {
            po.setStatus("RECEIVED");
            purchaseOrderRepository.save(po);
            journalService.postEntry(
                    "Goods received for PO #" + id,
                    "PURCHASE_ORDER", id,
                    "5000", "2000", po.getTotalAmount()
            );
            return ResponseEntity.ok(po);
        }).orElse(ResponseEntity.notFound().build());
    }
}
