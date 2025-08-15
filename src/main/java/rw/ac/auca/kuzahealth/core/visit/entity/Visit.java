package rw.ac.auca.kuzahealth.core.visit.entity;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.visitnote.entity.VisitNote;
import rw.ac.auca.kuzahealth.utils.BaseEntity;


@Entity
@Getter
@Setter
@Table(name = "visit")
public class Visit extends BaseEntity {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "scheduled_time", nullable = false)
    private Date scheduledTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "actual_start_time")
    private Date actualStartTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "actual_end_time")
    private Date actualEndTime;

    @Column(name = "visit_type", nullable = false)
    private String visitType;

    @Column(nullable = false)
    private String location;

    @Column(name = "mode_of_communication", nullable = false)
    private String modeOfCommunication;

    @Column(name = "status", nullable = false)
    private String status = "Scheduled"; // default value

    @Column(columnDefinition = "TEXT")
    private String summary; // optional

    @ManyToOne
    @JsonBackReference
    private HealthWorker healthWorker;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;


    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    private List<VisitNote> visitNotes;

    @JsonProperty("healthWorkerId")
    public UUID getHealthWorkerId() {
        return healthWorker != null ? healthWorker.getId() : null;
    }

    @JsonProperty("parentId")
    public UUID getParentId() {
        return parent != null ? parent.getId() : null;
    }

    @JsonProperty("visitNoteIds")
    public List<UUID> getVisitNoteIds() {
        return visitNotes != null ? visitNotes.stream().map(VisitNote::getId).collect(Collectors.toList()) : null;
    }

    @JsonProperty("visitId")
    public UUID getVisitId() {
        return getId();
    }
}
