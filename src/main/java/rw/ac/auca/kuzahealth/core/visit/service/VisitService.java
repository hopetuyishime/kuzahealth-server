package rw.ac.auca.kuzahealth.core.visit.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.repository.HealthWorkerRepository;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.parent.repository.ParentRepository;
import rw.ac.auca.kuzahealth.core.visit.dto.VisitRequest;
import rw.ac.auca.kuzahealth.core.visit.entity.Visit;
import rw.ac.auca.kuzahealth.core.visit.repository.VisitRepository;
import rw.ac.auca.kuzahealth.core.visitnote.entity.VisitNote;
import rw.ac.auca.kuzahealth.sms.model.SmsRequest;
import rw.ac.auca.kuzahealth.sms.model.SmsResponse;
import rw.ac.auca.kuzahealth.sms.service.PindoSmsService;
import rw.ac.auca.kuzahealth.sms.service.SmsService;

@Service
@AllArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final HealthWorkerRepository healthWorkerRepository;
    private final ParentRepository parentRepository;
    private final PindoSmsService smsService;

    public Visit createVisit(VisitRequest request) {
        Visit visit = new Visit();
        visit.setScheduledTime(request.getScheduledTime());
        visit.setActualStartTime(request.getActualStartTime());
        visit.setActualEndTime(request.getActualEndTime());
        visit.setVisitType(request.getVisitType());
        visit.setLocation(request.getLocation());
        visit.setModeOfCommunication(request.getModeOfCommunication());
        visit.setSummary(request.getSummary());

        // Set HealthWorker
        HealthWorker healthWorker = healthWorkerRepository.findById(request.getHealthWorkerId())
                .orElseThrow(() -> new EntityNotFoundException("HealthWorker not found"));
        visit.setHealthWorker(healthWorker);

        // Set Parent
        Parent parent = parentRepository.findById(request.getParent_id())
                .orElseThrow(() -> new EntityNotFoundException("Parent not found"));
        visit.setParent(parent);

        // Handle visit notes if any
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
        SmsRequest smsRequest = new SmsRequest();
        smsRequest.setTo(parent.getPhone());
        smsRequest.setText("Hello! We have a scheduled screening for your child. Please stay tuned for more details.");
        smsRequest.setSender("PindoTest");
        SmsResponse response = smsService.sendSingleSms(smsRequest.getTo(),smsRequest.getText(), smsRequest.getSender());

        System.out.println( response.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.badRequest().body(response));
        return visitRepository.save(visit);
    }


    public Optional<Visit> getVisitById(UUID id) {
        return visitRepository.findById(id);
    }

    public Optional<List<Visit>> getVisitByParentId(UUID id) {
        return visitRepository.findByParentId(id);
    }

    public List<Visit> getAllVisits() {
        return visitRepository.findAll();
    }

    public void deleteVisit(UUID id) {
        visitRepository.deleteById(id);
    }

    public Visit updateVisit(UUID id, Visit visitDetails) {
        Visit visit = visitRepository.findById(id).orElseThrow(() -> new RuntimeException("Visit not found"));
        visit.setScheduledTime(visitDetails.getScheduledTime());
        visit.setActualEndTime(visitDetails.getActualEndTime());
        visit.setVisitType(visitDetails.getVisitType());
        visit.setLocation(visitDetails.getLocation());
        visit.setHealthWorker(visitDetails.getHealthWorker());
        return visitRepository.save(visit);
    }
}
