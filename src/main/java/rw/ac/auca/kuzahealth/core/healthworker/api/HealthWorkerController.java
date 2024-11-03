package rw.ac.auca.kuzahealth.core.healthworker.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.service.HealthWorkerService;

@RestController
@RequestMapping("/api/health-workers")
public class HealthWorkerController {

    private final HealthWorkerService healthWorkerService;

    @Autowired
    public HealthWorkerController(HealthWorkerService healthWorkerService) {
        this.healthWorkerService = healthWorkerService;
    }

    @PostMapping
    public ResponseEntity<HealthWorker> createHealthWorker(@RequestBody HealthWorker healthWorker) {
        HealthWorker createdHealthWorker = healthWorkerService.createHealthWorker(healthWorker);
        return new ResponseEntity<>(createdHealthWorker, HttpStatus.CREATED);
    }
}
