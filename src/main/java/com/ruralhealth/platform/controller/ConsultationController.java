package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.entity.Consultation;
import com.ruralhealth.platform.repository.ConsultationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationRepository consultationRepository;

    public ConsultationController(ConsultationRepository consultationRepository) {
        this.consultationRepository = consultationRepository;
    }

    @GetMapping
    public List<Consultation> getAll() {
        return consultationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consultation> getById(@PathVariable Long id) {
        return consultationRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    public List<Consultation> getByPatient(@PathVariable Long patientId) {
        return consultationRepository.findByPatient_PatientIdOrderByCreatedAtDesc(patientId);
    }

    /** Move a consultation through the workflow, e.g. { "status": "IN_PROGRESS" } or "COMPLETED". */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Consultation> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return consultationRepository.findById(id).map(c -> {
            c.setStatus(body.get("status"));
            return ResponseEntity.ok(consultationRepository.save(c));
        }).orElse(ResponseEntity.notFound().build());
    }
}
