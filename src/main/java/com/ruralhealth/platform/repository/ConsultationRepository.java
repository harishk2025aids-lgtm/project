package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    // Triage queue: waiting patients ordered by score (highest priority first)
    List<Consultation> findByStatusOrderByTriageScoreDesc(String status);

    List<Consultation> findByPatient_PatientIdOrderByCreatedAtDesc(Long patientId);
}
