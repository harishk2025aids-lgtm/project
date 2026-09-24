package com.ruralhealth.platform.service;

import com.ruralhealth.platform.dto.TriageRequest;
import com.ruralhealth.platform.dto.TriageResponse;
import com.ruralhealth.platform.entity.Consultation;
import com.ruralhealth.platform.entity.Department;
import com.ruralhealth.platform.entity.Doctor;
import com.ruralhealth.platform.entity.Patient;
import com.ruralhealth.platform.repository.ConsultationRepository;
import com.ruralhealth.platform.repository.DepartmentRepository;
import com.ruralhealth.platform.repository.DoctorRepository;
import com.ruralhealth.platform.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Rule-based triage engine.
 *
 * This mirrors a simplified early-warning score (inspired by NEWS2 style
 * scoring) built from vitals plus keyword flags from free-text symptoms.
 * It can later be swapped for an ML model behind the same
 * TriageService interface (e.g. calling out to a Python microservice
 * or an embedded model) without changing any controller/service caller.
 */
@Service
public class TriageService {

    private final ConsultationRepository consultationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    // Keywords that force an EMERGENCY-level flag regardless of vitals
    private static final List<String> RED_FLAG_KEYWORDS = List.of(
            "chest pain", "difficulty breathing", "unconscious", "unresponsive",
            "severe bleeding", "seizure", "stroke", "not breathing", "choking"
    );

    private static final List<String> AMBER_FLAG_KEYWORDS = List.of(
            "high fever", "vomiting blood", "severe pain", "fracture",
            "allergic reaction", "confusion", "dizziness"
    );

    public TriageService(ConsultationRepository consultationRepository,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          DepartmentRepository departmentRepository) {
        this.consultationRepository = consultationRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    public TriageResponse evaluate(TriageRequest req) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        // ---- Heart Rate ----
        if (req.getHeartRate() != null) {
            int hr = req.getHeartRate();
            if (hr < 40 || hr > 130) { score += 3; reasons.add("Critical heart rate: " + hr + " bpm"); }
            else if (hr < 50 || hr > 110) { score += 2; reasons.add("Abnormal heart rate: " + hr + " bpm"); }
            else if (hr < 60 || hr > 100) { score += 1; reasons.add("Borderline heart rate: " + hr + " bpm"); }
        }

        // ---- Blood Pressure (systolic) ----
        if (req.getSystolicBp() != null) {
            int sbp = req.getSystolicBp();
            if (sbp < 80 || sbp > 200) { score += 3; reasons.add("Critical systolic BP: " + sbp + " mmHg"); }
            else if (sbp < 90 || sbp > 180) { score += 2; reasons.add("Abnormal systolic BP: " + sbp + " mmHg"); }
            else if (sbp < 100 || sbp > 160) { score += 1; reasons.add("Borderline systolic BP: " + sbp + " mmHg"); }
        }

        // ---- SpO2 ----
        if (req.getSpo2() != null) {
            int spo2 = req.getSpo2();
            if (spo2 < 90) { score += 3; reasons.add("Critical SpO2: " + spo2 + "%"); }
            else if (spo2 < 94) { score += 2; reasons.add("Low SpO2: " + spo2 + "%"); }
            else if (spo2 < 96) { score += 1; reasons.add("Borderline SpO2: " + spo2 + "%"); }
        }

        // ---- Temperature ----
        if (req.getTemperatureCelsius() != null) {
            double temp = req.getTemperatureCelsius();
            if (temp >= 40.0 || temp < 35.0) { score += 3; reasons.add("Critical temperature: " + temp + "°C"); }
            else if (temp >= 39.0) { score += 2; reasons.add("High fever: " + temp + "°C"); }
            else if (temp >= 38.0) { score += 1; reasons.add("Mild fever: " + temp + "°C"); }
        }

        // ---- Respiratory Rate ----
        if (req.getRespiratoryRate() != null) {
            int rr = req.getRespiratoryRate();
            if (rr < 8 || rr > 30) { score += 3; reasons.add("Critical respiratory rate: " + rr + "/min"); }
            else if (rr > 24 || rr < 10) { score += 2; reasons.add("Abnormal respiratory rate: " + rr + "/min"); }
        }

        // ---- Symptom keyword flags ----
        if (req.getSymptoms() != null) {
            String symptomsLower = req.getSymptoms().toLowerCase(Locale.ROOT);
            for (String kw : RED_FLAG_KEYWORDS) {
                if (symptomsLower.contains(kw)) { score += 5; reasons.add("Red-flag symptom: " + kw); }
            }
            for (String kw : AMBER_FLAG_KEYWORDS) {
                if (symptomsLower.contains(kw)) { score += 2; reasons.add("Warning symptom: " + kw); }
            }
        }

        String priority;
        if (score >= 7) priority = "EMERGENCY";
        else if (score >= 3) priority = "URGENT";
        else priority = "NORMAL";

        // Persist as a Consultation record
        Consultation consultation = new Consultation();
        Long patientId = Objects.requireNonNull(req.getPatientId());
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + req.getPatientId()));
        consultation.setPatient(patient);

        if (req.getDoctorId() != null) {
            Long doctorId = Objects.requireNonNull(req.getDoctorId());
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + req.getDoctorId()));
            consultation.setDoctor(doctor);
        }
        if (req.getDepartmentId() != null) {
            Long departmentId = Objects.requireNonNull(req.getDepartmentId());
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("Department not found: " + req.getDepartmentId()));
            consultation.setDepartment(dept);
        }

        consultation.setSymptoms(req.getSymptoms());
        consultation.setHeartRate(req.getHeartRate());
        consultation.setSystolicBp(req.getSystolicBp());
        consultation.setDiastolicBp(req.getDiastolicBp());
        consultation.setTemperatureCelsius(req.getTemperatureCelsius());
        consultation.setSpo2(req.getSpo2());
        consultation.setRespiratoryRate(req.getRespiratoryRate());
        consultation.setTriageScore(BigDecimal.valueOf(score));
        consultation.setTriagePriority(priority);
        consultation.setStatus("WAITING");

        consultation = consultationRepository.save(consultation);

        return new TriageResponse(consultation.getConsultationId(), BigDecimal.valueOf(score), priority, reasons);
    }

    /** Returns the current triage queue: waiting patients ordered highest-priority first. */
    public List<Consultation> getQueue() {
        return consultationRepository.findByStatusOrderByTriageScoreDesc("WAITING");
    }
}
