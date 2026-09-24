package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
