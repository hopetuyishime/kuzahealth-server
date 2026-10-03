package rw.ac.auca.kuzahealth.core.visit.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import rw.ac.auca.kuzahealth.core.visit.entity.Visit;

public interface VisitRepository extends JpaRepository<Visit, UUID>, JpaSpecificationExecutor<Visit> {
    List<Visit> findByParent_Id(UUID id);
}
