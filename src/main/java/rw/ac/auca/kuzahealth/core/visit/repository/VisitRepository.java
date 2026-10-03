package rw.ac.auca.kuzahealth.core.visit.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import rw.ac.auca.kuzahealth.core.visit.entity.Visit;
import rw.ac.auca.kuzahealth.core.visit.enums.VisitStatus;

public interface VisitRepository extends JpaRepository<Visit, UUID>, JpaSpecificationExecutor<Visit> {
    List<Visit> findByParent_Id(UUID id);

    List<Visit> findByStatusAndReminderSentFalseAndScheduledTimeBetween(VisitStatus status, Date from, Date to);

    /** Visits that were never started and whose time passed before the cutoff. */
    List<Visit> findByStatusAndActualStartTimeIsNullAndScheduledTimeBefore(VisitStatus status, Date cutoff);
}
