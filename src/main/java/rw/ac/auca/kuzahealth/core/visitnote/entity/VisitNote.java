package rw.ac.auca.kuzahealth.core.visitnote.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
    private String vitalSigns; // Use appropriate type for JSON-like structure
    private String recommendations;
    private List<String> attachments;
 @ManyToOne
    @JoinColumn(name = "visit_id", nullable = false) // Specify the foreign key column
    @JsonBackReference // This will be the back reference
    private Visit visit;

}
