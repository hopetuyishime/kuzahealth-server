package rw.ac.auca.kuzahealth.core.visit.entity;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.visitnote.entity.VisitNote;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "visit")
public class Visit extends BaseEntity {

    @Column(name = "scheduled_time", nullable = false)
    private Date scheduledTime;

    @Column(name = "actual_time")
    private Date actualTime;

    @Column(name = "visit_type", nullable = false)
    private String visitType;

    @Column(name = "location", nullable = false)
    private String location;

    @ManyToOne
     @JsonBackReference 
    private HealthWorker healthWorker;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // This will be the managed side of the relationship
    private List<VisitNote> visitNotes;

    // Protected constructor for JPA
    public Visit() {
    }

    // Public constructor for easy creation of a Visit object
    public Visit(Date scheduledTime, Date actualTime, String visitType, String location, HealthWorker healthWorker) {
        this.scheduledTime = scheduledTime;
        this.actualTime = actualTime;
        this.visitType = visitType;
        this.location = location;
        this.healthWorker = healthWorker;
    }
}
