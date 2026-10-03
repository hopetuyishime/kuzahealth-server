package rw.ac.auca.kuzahealth.core.vaccination.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.exception.ResourceNotFoundException;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.repository.HealthWorkerRepository;
import rw.ac.auca.kuzahealth.core.infant.entity.Infant;
import rw.ac.auca.kuzahealth.core.infant.repository.InfantRepository;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.vaccination.dto.VaccinationRequest;
import rw.ac.auca.kuzahealth.core.vaccination.entity.Vaccination;
import rw.ac.auca.kuzahealth.core.vaccination.repository.VaccinationRepository;
import rw.ac.auca.kuzahealth.core.notification.NotificationService;
import rw.ac.auca.kuzahealth.core.notification.SmsPurpose;
import rw.ac.auca.kuzahealth.utils.MailService;
import rw.ac.auca.kuzahealth.utils.SoftDeleter;

/**
 * Implementation of the VaccinationService interface
 */
@Service
@RequiredArgsConstructor
public class VaccinationServiceImpl implements VaccinationService {

    private static final Logger logger = LoggerFactory.getLogger(VaccinationServiceImpl.class);

    private final VaccinationRepository vaccinationRepository;
    private final InfantRepository infantRepository;
    private final HealthWorkerRepository healthWorkerRepository;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final SoftDeleter softDeleter;

    @Override
    @Transactional
    public Vaccination createVaccination(VaccinationRequest request) {
        Vaccination vaccination = new Vaccination();
        apply(request, vaccination);
        Vaccination saved = vaccinationRepository.save(vaccination);

        notificationService.notifyParent(saved.getInfant().getMother(), SmsPurpose.VACCINATION_RECORDED,
                "vaccination.recorded", saved.getName(), infantName(saved.getInfant()),
                notificationService.formatDate(saved.getAdministeredDate()));
        return saved;
    }

    @Override
    @Transactional
    public Vaccination updateVaccination(UUID id, VaccinationRequest request) {
        Vaccination vaccination = findById(id);
        apply(request, vaccination);
        return vaccinationRepository.save(vaccination);
    }

    @Override
    @Transactional(readOnly = true)
    public Vaccination findById(UUID id) {
        return vaccinationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vaccination> findAll() {
        return vaccinationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Vaccination> search(UUID infantId, UUID healthWorkerId, Pageable pageable) {
        Specification<Vaccination> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (infantId != null) {
                predicates.add(cb.equal(root.get("infant").get("id"), infantId));
            }
            if (healthWorkerId != null) {
                predicates.add(cb.equal(root.get("healthWorker").get("id"), healthWorkerId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return vaccinationRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vaccination> findByInfant(Infant infant) {
        return vaccinationRepository.findByInfant(infant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vaccination> findByInfantId(UUID infantId) {
        return vaccinationRepository.findByInfant_Id(infantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vaccination> findByHealthWorker(HealthWorker healthWorker) {
        return vaccinationRepository.findByHealthWorker(healthWorker);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vaccination> findByHealthWorkerId(UUID healthWorkerId) {
        return vaccinationRepository.findByHealthWorker_Id(healthWorkerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vaccination> findByParentId(UUID parentId) {
        return vaccinationRepository.findByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vaccination> findDueVaccinations(Date date) {
        return vaccinationRepository.findByNextDueDateLessThanEqualAndNotificationSentFalse(date);
    }

    @Override
    @Transactional
    public int sendDueVaccinationNotifications(Date date) {
        int notificationsSent = 0;

        for (Vaccination vaccination : findDueVaccinations(date)) {
            if (remind(vaccination)) {
                notificationsSent++;
            }
        }

        return notificationsSent;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        softDeleter.delete(Vaccination.class, findById(id).getId());
    }

    private void apply(VaccinationRequest request, Vaccination vaccination) {
        Infant infant = infantRepository.findById(request.getInfantId())
                .orElseThrow(() -> new ResourceNotFoundException("Infant not found with id: " + request.getInfantId()));
        HealthWorker healthWorker = healthWorkerRepository.findById(request.getHealthWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Health Worker not found with id: " + request.getHealthWorkerId()));

        vaccination.setInfant(infant);
        vaccination.setHealthWorker(healthWorker);
        vaccination.setName(request.getName());
        vaccination.setDescription(request.getDescription() != null ? request.getDescription() : "");
        vaccination.setAdministeredDate(request.getAdministeredDate());
        vaccination.setNextDueDate(request.getNextDueDate());
        vaccination.setNotes(request.getNotes());
    }

    private static String infantName(Infant infant) {
        return ((infant.getFirstName() != null ? infant.getFirstName() : "") + " "
                + (infant.getLastName() != null ? infant.getLastName() : "")).trim();
    }

    /**
     * Reminds the mother by SMS, and by email when she has one, that the next dose is due.
     *
     * @return whether at least one of the two went out; only then is the reminder marked as sent
     */
    @Override
    @Transactional
    public boolean remind(Vaccination vaccination) {
        Parent parent = vaccination.getInfant().getMother();
        String infantName = infantName(vaccination.getInfant());
        String dueDate = notificationService.formatDate(vaccination.getNextDueDate());

        boolean sent = notificationService.notifyParent(parent, SmsPurpose.VACCINATION_DUE, "vaccination.due",
                vaccination.getName(), infantName, dueDate).getStatus().isSent();

        if (parent.getEmail() != null && !parent.getEmail().isBlank()) {
            try {
                mailService.sendEmail(parent.getEmail(), "Vaccination Due Reminder", String.format(
                        "Dear Parent,\n\n"
                        + "This is a reminder that %s is due for %s vaccination on %s.\n\n"
                        + "Please contact your healthcare provider to schedule an appointment.\n\n"
                        + "Best regards,\n"
                        + "KuzaHealth Team",
                        infantName, vaccination.getName(), dueDate));
                sent = true;
            } catch (MailException e) {
                logger.error("Could not email vaccination reminder for vaccination {}", vaccination.getId(), e);
            }
        }

        if (sent) {
            vaccination.setNotificationSent(true);
            vaccinationRepository.save(vaccination);
        }
        return sent;
    }
}
