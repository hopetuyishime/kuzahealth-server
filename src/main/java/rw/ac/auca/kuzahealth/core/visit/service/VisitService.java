package rw.ac.auca.kuzahealth.core.visit.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.repository.HealthWorkerRepository;
import rw.ac.auca.kuzahealth.core.visit.entity.Visit;
import rw.ac.auca.kuzahealth.core.visit.repository.VisitRepository;

@Service
public class VisitService {

    private final VisitRepository visitRepository;
    private final HealthWorkerRepository healthWorkerRepository;

    @Autowired
    public VisitService(VisitRepository visitRepository, HealthWorkerRepository healthWorkerRepository) {
        this.visitRepository = visitRepository;
        this.healthWorkerRepository = healthWorkerRepository;
    }

    public Visit createVisit(Visit visit) {
        // Parse the health worker ID from the nested structure
        UUID healthWorkerId = visit.getHealthWorker().getId();
    
        Optional<HealthWorker> healthWorkerOpt = healthWorkerRepository.findById(healthWorkerId);
    
        healthWorkerOpt.ifPresentOrElse(
            healthWorker -> {
                visit.setHealthWorker(healthWorker); // Associate the actual HealthWorker entity with the visit
            },
            () -> {
                throw new EntityNotFoundException("HealthWorker with ID " + healthWorkerId + " not found.");
            }
        );
    
        return visitRepository.save(visit);
    }
    public Optional<Visit> getVisitById(UUID id) {
        return visitRepository.findById(id);
    }

    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    public void deleteVisit(UUID id) {
        visitRepository.deleteById(id);
    }

    public Visit updateVisit(UUID id, Visit visitDetails) {
        Visit visit = visitRepository.findById(id).orElseThrow(() -> new RuntimeException("Visit not found"));
        visit.setScheduledTime(visitDetails.getScheduledTime());
        visit.setActualTime(visitDetails.getActualTime());
        visit.setVisitType(visitDetails.getVisitType());
        visit.setLocation(visitDetails.getLocation());
        visit.setHealthWorker(visitDetails.getHealthWorker());
        return visitRepository.save(visit);
    }
}
