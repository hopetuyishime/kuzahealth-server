package rw.ac.auca.kuzahealth.core.parent.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.exception.ResourceNotFoundException;
import rw.ac.auca.kuzahealth.core.parent.consent.ConsentRecord;
import rw.ac.auca.kuzahealth.core.parent.consent.ConsentRecordRepository;
import rw.ac.auca.kuzahealth.core.parent.consent.ConsentType;
import rw.ac.auca.kuzahealth.core.parent.dto.ParentRequest;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.parent.repository.ParentRepository;
import rw.ac.auca.kuzahealth.utils.SoftDeleter;

@Service
@RequiredArgsConstructor
public class ParentServiceImpl {

    private final ParentRepository parentRepository;
    private final SoftDeleter softDeleter;
    private final ConsentRecordRepository consentRecordRepository;

    @Transactional
    public Parent registerParent(ParentRequest request, String recordedBy) {
        Parent parent = new Parent();
        apply(request, parent);
        Parent saved = parentRepository.save(parent);
        if (request.getSmsConsent() != null) {
            recordConsent(saved.getId(), ConsentType.SMS, request.getSmsConsent(), "Recorded at registration",
                    recordedBy);
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Parent> getAllParents() {
        return parentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Parent> search(String q, String district, Boolean highRisk, Pageable pageable) {
        Specification<Parent> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like),
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(root.get("phone"), like)));
            }
            if (district != null && !district.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("district")), district.trim().toLowerCase()));
            }
            if (highRisk != null) {
                predicates.add(cb.equal(root.get("isHighRisk"), highRisk));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return parentRepository.findAll(spec, pageable);
    }

    /** @return the parent, or null when there is none with this id */
    @Transactional(readOnly = true)
    public Parent getParentById(UUID id) {
        return id == null ? null : parentRepository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Parent requireParent(UUID id) {
        Parent parent = getParentById(id);
        if (parent == null) {
            throw new ResourceNotFoundException("Parent not found with id: " + id);
        }
        return parent;
    }

    /** @return the updated parent, or null when there is none with this id */
    @Transactional
    public Parent updateParent(UUID id, ParentRequest request, String recordedBy) {
        Parent parent = getParentById(id);
        if (parent == null) {
            return null;
        }
        apply(request, parent);
        Parent saved = parentRepository.save(parent);
        if (request.getSmsConsent() != null && request.getSmsConsent() != saved.isSmsConsent()) {
            recordConsent(id, ConsentType.SMS, request.getSmsConsent(), null, recordedBy);
        }
        return saved;
    }

    /** Marks the parent and everything recorded for her (infants, visits, pregnancies) as deleted. */
    @Transactional
    public boolean deleteParent(UUID id) {
        if (parentRepository.existsById(id)) {
            softDeleter.deleteParent(id);
            return true;
        }
        return false;
    }

    /** Records a consent decision and, for SMS, switches messaging on or off for the parent. */
    @Transactional
    public ConsentRecord recordConsent(UUID parentId, ConsentType type, boolean granted, String note,
            String recordedBy) {
        Parent parent = requireParent(parentId);
        if (type == ConsentType.SMS) {
            parent.setSmsConsent(granted);
            parentRepository.save(parent);
        }
        ConsentRecord record = new ConsentRecord();
        record.setParent(parent);
        record.setConsentType(type);
        record.setGranted(granted);
        record.setNote(note);
        record.setRecordedBy(recordedBy);
        return consentRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<ConsentRecord> getConsents(UUID parentId) {
        requireParent(parentId);
        return consentRecordRepository.findByParent_IdOrderByCreatedAtDesc(parentId);
    }

    private static void apply(ParentRequest request, Parent parent) {
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setEmail(request.getEmail());
        parent.setPhone(request.getPhone());
        parent.setExpectedDeliveryDate(request.getExpectedDeliveryDate());
        parent.setHighRisk(request.isHighRisk());
        parent.setBloodGroup(request.getBloodGroup());
        parent.setMaritalStatus(request.getMaritalStatus());
        parent.setEmergencyContactNumber(request.getEmergencyContactNumber());
        parent.setEmergencyContactFullName(request.getEmergencyContactFullName());
        parent.setEmergencyContactRelationship(request.getEmergencyContactRelationship());
        parent.setDistrict(request.getDistrict());
        parent.setSector(request.getSector());
        parent.setCell(request.getCell());
        parent.setVillage(request.getVillage());
        if (request.getPreferredLanguage() != null) {
            parent.setPreferredLanguage(request.getPreferredLanguage());
        }
    }
}
