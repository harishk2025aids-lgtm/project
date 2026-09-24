package com.ruralhealth.platform.controller;

import com.ruralhealth.platform.dto.TriageRequest;
import com.ruralhealth.platform.dto.TriageResponse;
import com.ruralhealth.platform.entity.Consultation;
import com.ruralhealth.platform.service.TriageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/triage")
public class TriageController {

    private final TriageService triageService;

    public TriageController(TriageService triageService) {
        this.triageService = triageService;
    }

    /** Stage 4: submit a patient's symptoms + vitals, get back a triage score/priority. */
    @PostMapping("/evaluate")
    public TriageResponse evaluate(@RequestBody TriageRequest request) {
        return triageService.evaluate(request);
    }

    /** Current waiting queue, highest triage score (most urgent) first. */
    @GetMapping("/queue")
    public List<Consultation> queue() {
        return triageService.getQueue();
    }
}
