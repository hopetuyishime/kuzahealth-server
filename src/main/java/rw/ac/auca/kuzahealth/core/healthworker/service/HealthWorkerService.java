package rw.ac.auca.kuzahealth.core.healthworker.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.exception.BadRequestException;
import rw.ac.auca.kuzahealth.core.exception.DuplicateResourceException;
import rw.ac.auca.kuzahealth.core.exception.ResourceNotFoundException;
import rw.ac.auca.kuzahealth.core.healthworker.dto.HealthWorkerRequest;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.repository.HealthWorkerRepository;
import rw.ac.auca.kuzahealth.core.user.entity.User;
import rw.ac.auca.kuzahealth.core.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class HealthWorkerService {

    private static final Logger logger = LoggerFactory.getLogger(HealthWorkerService.class);

    private final HealthWorkerRepository healthWorkerRepository;
    private final UserRepository userRepository;

    /** Creates the staff record that accompanies a newly registered health worker account. */
    @Transactional
    public HealthWorker createForUser(User user) {
        HealthWorkerRequest request = new HealthWorkerRequest();
        request.setFirstName(user.getFirstName());
        request.setLastName(user.getLastName());
        request.setEmail(user.getEmail());
        request.setPhoneNumber(user.getPhoneNumber());
        return createHealthWorker(request);
    }

    @Transactional
    public HealthWorker createHealthWorker(HealthWorkerRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (healthWorkerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }
        HealthWorker healthWorker = new HealthWorker();
        healthWorker.setEmail(request.getEmail());
        apply(request, healthWorker);
        // Link to the login account with the same email, when there is one
        userRepository.findByEmail(request.getEmail()).ifPresent(healthWorker::setUser);

        HealthWorker saved = healthWorkerRepository.save(healthWorker);
        logger.info("HealthWorker saved with ID: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<HealthWorker> getAllHealthWorkers() {
        return healthWorkerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<HealthWorker> search(String q, Pageable pageable) {
        Specification<HealthWorker> spec = (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("firstName")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("serviceArea")), like));
        };
        return healthWorkerRepository.findAll(spec, pageable);
    }

    @Transactional
    public HealthWorker updateHealthWorker(UUID id, HealthWorkerRequest updates) {
        HealthWorker existing = getHealthWorkerById(id);

        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (healthWorkerRepository.existsByEmail(updates.getEmail())) {
                throw new DuplicateResourceException("Email already exists");
            }
            existing.setEmail(updates.getEmail());
            userRepository.findByEmail(updates.getEmail()).ifPresent(existing::setUser);
        }
        apply(updates, existing);

        return healthWorkerRepository.save(existing);
    }

    @Transactional
    public void deleteHealthWorker(UUID id) {
        healthWorkerRepository.delete(getHealthWorkerById(id));
        logger.info("Deleted HealthWorker with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public HealthWorker getHealthWorkerById(UUID id) {
        return healthWorkerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthWorker not found"));
    }

    /** The staff record of a signed-in user, matched by linked account and then by email. */
    @Transactional(readOnly = true)
    public HealthWorker getForUser(UUID userId, String email) {
        return healthWorkerRepository.findByUser_Id(userId)
                .or(() -> healthWorkerRepository.findByEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("No health worker record for this account"));
    }

    private static void apply(HealthWorkerRequest request, HealthWorker target) {
        if (request.getFirstName() != null) target.setFirstName(request.getFirstName());
        if (request.getLastName() != null) target.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) target.setPhoneNumber(request.getPhoneNumber());
        if (request.getQualification() != null) target.setQualification(request.getQualification());
        if (request.getServiceArea() != null) target.setServiceArea(request.getServiceArea());
    }
}
