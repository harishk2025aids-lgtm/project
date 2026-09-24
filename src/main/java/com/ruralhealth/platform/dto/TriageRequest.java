package com.ruralhealth.platform.dto;

import lombok.Data;

@Data
public class TriageRequest {
    private Long patientId;
    private Long doctorId;
    private Long departmentId;
    private String symptoms;

    // Vitals
    private Integer heartRate;          // bpm
    private Integer systolicBp;         // mmHg
    private Integer diastolicBp;        // mmHg
    private Double temperatureCelsius;  // °C
    private Integer spo2;               // % oxygen saturation
    private Integer respiratoryRate;    // breaths/min
}
