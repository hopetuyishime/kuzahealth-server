package rw.ac.auca.kuzahealth.core.healthworker.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.repository.HealthWorkerRepository;

@Service
public class HealthWorkerService {
    private static final Logger logger = LoggerFactory.getLogger(HealthWorkerService.class);

    private final HealthWorkerRepository healthWorkerRepository;

    @Autowired
    public HealthWorkerService(HealthWorkerRepository healthWorkerRepository) {
        this.healthWorkerRepository = healthWorkerRepository;
    }

    public HealthWorker createHealthWorker(HealthWorker healthWorker) {
        logger.info("Creating HealthWorker with email: {}", healthWorker.getEmail());

        // Check if the email already exists
        boolean emailExists = healthWorkerRepository.existsByEmail(healthWorker.getEmail());
        logger.info("Email exists check: {}", emailExists);

        if (emailExists) {
            logger.error("HealthWorker with email {} already exists", healthWorker.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        HealthWorker savedHealthWorker = healthWorkerRepository.save(healthWorker);
        logger.info("HealthWorker saved with ID: {}", savedHealthWorker.getId());

        return savedHealthWorker;
    }

    public List<HealthWorker> getAllHealthWorkers() {
        return healthWorkerRepository.findAll();
    }
}
