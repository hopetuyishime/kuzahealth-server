package rw.ac.auca.kuzahealth.core.vaccination.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
import rw.ac.auca.kuzahealth.sms.service.PindoSmsService;
import rw.ac.auca.kuzahealth.utils.MailService;

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
    private final PindoSmsService smsService;

    @Value("${pindo.sender:PindoTest}")
    private String smsSender;

    @Override
    @Transactional
    public Vaccination createVaccination(VaccinationRequest request) {
        Vaccination vaccination = new Vaccination();
        apply(request, vaccination);
        Vaccination saved = vaccinationRepository.save(vaccination);

        Parent parent = saved.getInfant().getMother();
        if (parent.getPhone() != null && !parent.getPhone().isBlank()) {
            String message = String.format(
                    "%s vaccination was recorded for %s on %s.",
                    saved.getName(), infantName(saved.getInfant()), saved.getAdministeredDate());
            smsService.sendSingleSms(parent.getPhone(), message, smsSender);
        }
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
            Parent parent = vaccination.getInfant().getMother();
            if (parent.getEmail() == null || parent.getEmail().isBlank()) {
                continue;
            }
            try {
                sendVaccinationDueNotification(parent.getEmail(), infantName(vaccination.getInfant()),
                        vaccination.getName(), vaccination.getNextDueDate());
            } catch (MailException e) {
                logger.error("Could not send vaccination reminder for vaccination {}", vaccination.getId(), e);
                continue;
            }
            vaccination.setNotificationSent(true);
            vaccinationRepository.save(vaccination);
            notificationsSent++;
        }

        return notificationsSent;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        vaccinationRepository.delete(findById(id));
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

    private void sendVaccinationDueNotification(String email, String infantName, String vaccinationName, Date dueDate) {
        String subject = "Vaccination Due Reminder";
        String message = String.format(
                "Dear Parent,\n\n" +
                "This is a reminder that %s is due for %s vaccination on %s.\n\n" +
                "Please contact your healthcare provider to schedule an appointment.\n\n" +
                "Best regards,\n" +
                "KuzaHealth Team",
                infantName, vaccinationName, dueDate.toString());

        mailService.sendEmail(email, subject, message);
    }
}
