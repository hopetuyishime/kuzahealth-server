package rw.ac.auca.kuzahealth.core.infant.service;

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
import rw.ac.auca.kuzahealth.core.infant.dto.InfantRequest;
import rw.ac.auca.kuzahealth.core.infant.entity.Infant;
import rw.ac.auca.kuzahealth.core.infant.repository.InfantRepository;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.parent.repository.ParentRepository;

/**
 * Implementation of the InfantService interface
 */
@Service
@RequiredArgsConstructor
public class InfantServiceImpl implements InfantService {

    private final InfantRepository infantRepository;
    private final ParentRepository parentRepository;

    @Override
    @Transactional
    public Infant createInfant(InfantRequest request) {
        Infant infant = new Infant();
        apply(request, infant);
        return infantRepository.save(infant);
    }

    @Override
    @Transactional
    public Infant updateInfant(UUID id, InfantRequest request) {
        Infant infant = findById(id);
        apply(request, infant);
        return infantRepository.save(infant);
    }

    @Override
    @Transactional(readOnly = true)
    public Infant findById(UUID id) {
        return infantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Infant not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Infant> findAll() {
        return infantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Infant> search(String q, UUID motherId, Pageable pageable) {
        Specification<Infant> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("firstName")), like),
                        cb.like(cb.lower(root.get("lastName")), like)));
            }
            if (motherId != null) {
                predicates.add(cb.equal(root.get("mother").get("id"), motherId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        return infantRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Infant> findByMother(Parent mother) {
        return infantRepository.findByMother(mother);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Infant> findByMotherId(UUID motherId) {
        return infantRepository.findByMother_Id(motherId);
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        infantRepository.delete(findById(id));
    }

    private void apply(InfantRequest request, Infant infant) {
        Parent mother = parentRepository.findById(request.getMotherId())
                .orElseThrow(() -> new ResourceNotFoundException("Mother not found with id: " + request.getMotherId()));
        infant.setFirstName(request.getFirstName());
        infant.setLastName(request.getLastName());
        infant.setDateOfBirth(request.getDateOfBirth());
        infant.setGender(request.getGender());
        infant.setBirthWeight(request.getBirthWeight());
        infant.setBirthHeight(request.getBirthHeight());
        infant.setBloodGroup(request.getBloodGroup());
        infant.setSpecialConditions(request.getSpecialConditions());
        infant.setMother(mother);
    }
}
