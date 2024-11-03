package rw.ac.auca.kuzahealth.core.pregancyrecord.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.ac.auca.kuzahealth.core.pregancyrecord.entity.PregnancyRecord;
import rw.ac.auca.kuzahealth.core.pregancyrecord.exception.DuplicateResourceException;
import rw.ac.auca.kuzahealth.core.pregancyrecord.exception.ResourceNotFoundException;
import rw.ac.auca.kuzahealth.core.pregancyrecord.repository.PregnancyRecordRepository;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;


import java.util.List;
import java.util.UUID;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class PregnancyRecordService {
    
    private final PregnancyRecordRepository pregnancyRecordRepository;
    
    public PregnancyRecord createPregnancyRecord(PregnancyRecord pregnancyRecord) {
        // Check if a record already exists for this parent and LMP
        if (pregnancyRecordRepository.existsByParentAndLastMenstrualPeriod(
                pregnancyRecord.getParent(), 
                pregnancyRecord.getLast_menstrual_period())) {
            throw new DuplicateResourceException(
                "Pregnancy record already exists for this parent and last menstrual period date");
        }
        
        return pregnancyRecordRepository.save(pregnancyRecord);
    }
    
    public PregnancyRecord updatePregnancyRecord(UUID id, PregnancyRecord updatedRecord) {
        PregnancyRecord existingRecord = pregnancyRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pregnancy record not found with id: " + id));
            
        // Update fields
        existingRecord.setGravity(updatedRecord.getGravity());
        existingRecord.setParity(updatedRecord.getParity());
        existingRecord.setLast_menstrual_period(updatedRecord.getLast_menstrual_period());
        existingRecord.setMedical_history(updatedRecord.getMedical_history());
        existingRecord.setPregnancy_complications(updatedRecord.getPregnancy_complications());
        
        return pregnancyRecordRepository.save(existingRecord);
    }
    
    public PregnancyRecord getPregnancyRecord(UUID id) {
        return pregnancyRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Pregnancy record not found with id: " + id));
    }
    
    public List<PregnancyRecord> getAllPregnancyRecordsByParent(Parent parent) {
        return pregnancyRecordRepository.findByParentOrderByCreatedAtDesc(parent)
            .orElseThrow(() -> new ResourceNotFoundException("No pregnancy records found for parent"));
    }
    
    public void deletePregnancyRecord(UUID id) {
        if (!pregnancyRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pregnancy record not found with id: " + id);
        }
        pregnancyRecordRepository.deleteById(id);
    }
    
    public List<PregnancyRecord> getAllPregnancyRecords() {
        return pregnancyRecordRepository.findAll();
    }
    
    // Additional business logic methods
    
    public boolean hasActivePregnancy(Parent parent) {
        List<PregnancyRecord> records = pregnancyRecordRepository.findByParentOrderByCreatedAtDesc(parent)
            .orElse(List.of());
            
        if (records.isEmpty()) {
            return false;
        }
        
        PregnancyRecord latestRecord = records.get(0);
        // Calculate if the pregnancy is still active based on LMP
        Date lmp = latestRecord.getLast_menstrual_period();
        Date now = new Date();
        
        // Assuming pregnancy duration is 40 weeks (280 days)
        long pregnancyDuration = 280L * 24 * 60 * 60 * 1000; // in milliseconds
        return (now.getTime() - lmp.getTime()) <= pregnancyDuration;
    }
    
    public int calculateWeeksOfPregnancy(PregnancyRecord record) {
        Date lmp = record.getLast_menstrual_period();
        Date now = new Date();
        
        long diffInMillies = now.getTime() - lmp.getTime();
        long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
        
        return (int) (diffInDays / 7);
    }
}