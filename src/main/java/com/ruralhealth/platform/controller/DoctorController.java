package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.entity.Doctor;
import com.ruralhealth.platform.repository.DoctorRepository;
import org.springframework.http.ResponseEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorRepository doctorRepository;

    public DoctorController(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @GetMapping
    public List<Doctor> getAll() {
        return doctorRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getById(@NonNull @PathVariable Long id) {
        return doctorRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Doctor create(@NonNull @RequestBody Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> update(@NonNull @PathVariable Long id, @NonNull @RequestBody Doctor updated) {
        return doctorRepository.findById(id).map(existing -> {
            updated.setDoctorId(id);
            return ResponseEntity.ok(doctorRepository.save(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@NonNull @PathVariable Long id) {
        if (!doctorRepository.existsById(id)) return ResponseEntity.notFound().build();
        doctorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
