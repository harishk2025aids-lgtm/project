package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.entity.Supplier;
import com.ruralhealth.platform.repository.SupplierRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierRepository supplierRepository;

    public SupplierController(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @GetMapping
    public List<Supplier> getAll() {
        return supplierRepository.findAll();
    }

    @PostMapping
    public Supplier create(@NonNull @RequestBody Supplier supplier) {
        return supplierRepository.save(supplier);
    }
}
