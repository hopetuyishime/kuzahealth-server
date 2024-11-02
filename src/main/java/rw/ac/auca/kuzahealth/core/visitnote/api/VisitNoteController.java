package rw.ac.auca.kuzahealth.core.visitnote.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import rw.ac.auca.kuzahealth.core.visitnote.entity.VisitNote;
import rw.ac.auca.kuzahealth.core.visitnote.service.VisitNoteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/visit-notes")
public class VisitNoteController {

    private final VisitNoteService visitNoteService;

    @Autowired
    public VisitNoteController(VisitNoteService visitNoteService) {
        this.visitNoteService = visitNoteService;
    }

    @PostMapping
    public ResponseEntity<VisitNote> createVisitNote(@RequestBody VisitNote visitNote) {
        VisitNote createdVisitNote = visitNoteService.createVisitNote(visitNote);
        return new ResponseEntity<>(createdVisitNote, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VisitNote>> getAllVisitNotes() {
        List<VisitNote> visitNotes = visitNoteService.getAllVisitNotes();
        return new ResponseEntity<>(visitNotes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitNote> getVisitNoteById(@PathVariable UUID id) {
        return visitNoteService.getVisitNoteById(id)
                .map(visitNote -> new ResponseEntity<>(visitNote, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVisitNoteById(@PathVariable UUID id) {
        visitNoteService.deleteVisitNoteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
