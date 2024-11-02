package rw.ac.auca.kuzahealth.core.healthworker.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.repository.HealthWorkerRepository;

@Service
public class HealthWorkerService {

    private final HealthWorkerRepository healthWorkerRepository;

    @Autowired
    public HealthWorkerService(HealthWorkerRepository healthWorkerRepository) {
        this.healthWorkerRepository = healthWorkerRepository;
    }

    public HealthWorker createHealthWorker(HealthWorker healthWorker) {
        // Check if the email already exists
        if (healthWorkerRepository.existsByEmail(healthWorker.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        return healthWorkerRepository.save(healthWorker);
    }

    public List<HealthWorker> getAllHealthWorkers() {
        return healthWorkerRepository.findAll();
    }
}
