package rw.ac.auca.kuzahealth.core.visit.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.exception.BadRequestException;
import rw.ac.auca.kuzahealth.core.exception.ResourceNotFoundException;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.repository.HealthWorkerRepository;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.parent.repository.ParentRepository;
import rw.ac.auca.kuzahealth.core.visit.dto.VisitRequest;
import rw.ac.auca.kuzahealth.core.visit.entity.Visit;
import rw.ac.auca.kuzahealth.core.visit.enums.VisitStatus;
import rw.ac.auca.kuzahealth.core.visit.repository.VisitRepository;
import rw.ac.auca.kuzahealth.core.visitnote.entity.VisitNote;
import rw.ac.auca.kuzahealth.sms.service.PindoSmsService;

@Service
@RequiredArgsConstructor
public class VisitService {
    private final VisitRepository visitRepository;
    private final HealthWorkerRepository healthWorkerRepository;
    private final ParentRepository parentRepository;
    private final PindoSmsService smsService;

    @Value("${pindo.sender:PindoTest}")
    private String smsSender;

    @Transactional
    public Visit createVisit(VisitRequest request) {
        require(request.getScheduledTime(), "scheduledTime");
        require(request.getVisitType(), "visitType");
        require(request.getLocation(), "location");
        require(request.getModeOfCommunication(), "modeOfCommunication");
        require(request.getHealthWorkerId(), "healthWorkerId");
        require(request.getParentId(), "parent_id");

        Visit visit = new Visit();
        visit.setScheduledTime(request.getScheduledTime());
        visit.setActualStartTime(request.getActualStartTime());
        visit.setActualEndTime(request.getActualEndTime());
        visit.setVisitType(request.getVisitType());
        visit.setLocation(request.getLocation());
        visit.setModeOfCommunication(request.getModeOfCommunication());
        visit.setSummary(request.getSummary());
        if (request.getStatus() != null) {
            visit.setStatus(request.getStatus());
        }
        visit.setHealthWorker(findHealthWorker(request.getHealthWorkerId()));
        Parent parent = findParent(request.getParentId());
        visit.setParent(parent);

        if (request.getVisitNotes() != null) {
            List<VisitNote> notes = request.getVisitNotes().stream().map(noteReq -> {
                VisitNote note = new VisitNote();
                note.setObservation(noteReq.getObservation());
                note.setVitalSigns(noteReq.getVitalSigns());
                note.setRecommendations(noteReq.getRecommendations());
                note.setAttachments(noteReq.getAttachments());
                note.setVisit(visit); // Set back-reference
                return note;
            }).toList();
            visit.setVisitNotes(notes);
        }

        Visit saved = visitRepository.save(visit);

        if (parent.getPhone() != null && !parent.getPhone().isBlank()) {
            smsService.sendSingleSms(parent.getPhone(),
                    "Hello! We have a scheduled screening for your child. Please stay tuned for more details.",
                    smsSender);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Visit> getVisitById(UUID id) {
        return visitRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Visit> getVisitByParentId(UUID id) {
        return visitRepository.findByParent_Id(id);
    }

    @Transactional(readOnly = true)
    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Visit> search(VisitStatus status, UUID healthWorkerId, UUID parentId, Date from, Date to,
            Pageable pageable) {
        Specification<Visit> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (healthWorkerId != null) {
                predicates.add(cb.equal(root.get("healthWorker").get("id"), healthWorkerId));
            }
            if (parentId != null) {
                predicates.add(cb.equal(root.get("parent").get("id"), parentId));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledTime"), from));
            }
            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("scheduledTime"), to));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return visitRepository.findAll(spec, pageable);
    }

    @Transactional
    public void deleteVisit(UUID id) {
        visitRepository.delete(requireVisit(id));
    }

    @Transactional
    public Visit updateVisit(UUID id, VisitRequest request) {
        Visit visit = requireVisit(id);

        if (request.getScheduledTime() != null) {
            visit.setScheduledTime(request.getScheduledTime());
        }
        if (request.getActualStartTime() != null) {
            visit.setActualStartTime(request.getActualStartTime());
        }
        if (request.getActualEndTime() != null) {
            visit.setActualEndTime(request.getActualEndTime());
        }
        if (request.getVisitType() != null) {
            visit.setVisitType(request.getVisitType());
        }
        if (request.getLocation() != null) {
            visit.setLocation(request.getLocation());
        }
        if (request.getModeOfCommunication() != null) {
            visit.setModeOfCommunication(request.getModeOfCommunication());
        }
        if (request.getSummary() != null) {
            visit.setSummary(request.getSummary());
        }
        if (request.getStatus() != null) {
            visit.setStatus(request.getStatus());
        }
        if (request.getHealthWorkerId() != null) {
            visit.setHealthWorker(findHealthWorker(request.getHealthWorkerId()));
        }
        if (request.getParentId() != null) {
            visit.setParent(findParent(request.getParentId()));
        }
        // Visit notes are managed through the visit-notes endpoints.

        return visitRepository.save(visit);
    }

    public Visit requireVisit(UUID id) {
        return visitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Visit not found"));
    }

    private HealthWorker findHealthWorker(UUID id) {
        return healthWorkerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthWorker not found"));
    }

    private Parent findParent(UUID id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parent not found"));
    }

    private static void require(Object value, String field) {
        if (value == null || (value instanceof String text && text.isBlank())) {
            throw new BadRequestException(field + " is required");
        }
    }
}
