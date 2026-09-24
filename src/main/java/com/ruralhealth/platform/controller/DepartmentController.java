package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.entity.Department;
import com.ruralhealth.platform.repository.DepartmentRepository;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @GetMapping
    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    @PostMapping
    public Department create(@NonNull @RequestBody Department department) {
        return departmentRepository.save(department);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Department> update(@NonNull @PathVariable Long id, @NonNull @RequestBody Department updated) {
        return departmentRepository.findById(id).map(existing -> {
            updated.setDepartmentId(id);
            return ResponseEntity.ok(departmentRepository.save(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable Long id) {
        if (!departmentRepository.existsById(id)) return ResponseEntity.notFound().build();
        departmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
