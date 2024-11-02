package rw.ac.auca.kuzahealth.core.visitnote.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import rw.ac.auca.kuzahealth.core.visitnote.entity.VisitNote;
import rw.ac.auca.kuzahealth.core.visitnote.repository.VisitNoteRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VisitNoteService {

    private final VisitNoteRepository visitNoteRepository;

    @Autowired
    public VisitNoteService(VisitNoteRepository visitNoteRepository) {
        this.visitNoteRepository = visitNoteRepository;
    }

    public VisitNote createVisitNote(VisitNote visitNote) {
        return visitNoteRepository.save(visitNote);
    }

    public List<VisitNote> getAllVisitNotes() {
        return visitNoteRepository.findAll();
    }

    public Optional<VisitNote> getVisitNoteById(UUID id) {
        return visitNoteRepository.findById(id);
    }

    public void deleteVisitNoteById(UUID id) {
        visitNoteRepository.deleteById(id);
    }
}
