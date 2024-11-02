package rw.ac.auca.kuzahealth.core.visitnote.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import rw.ac.auca.kuzahealth.core.visitnote.entity.VisitNote;

public interface VisitNoteRepository extends JpaRepository<VisitNote,UUID>{
    
}
