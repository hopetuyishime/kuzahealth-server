package rw.ac.auca.kuzahealth.core.visitnote.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.visit.entity.Visit;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

@Entity
@Getter
@Setter
@Table(name="visit_note")
public class VisitNote extends BaseEntity {

    private String observation;

    @Column(columnDefinition = "TEXT")
    private String vitalSigns;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @ElementCollection
    private List<String> attachments;

    @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false)
    @JsonBackReference
    private Visit visit;

}
