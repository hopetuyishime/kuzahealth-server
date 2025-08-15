package rw.ac.auca.kuzahealth.core.vaccination.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.infant.entity.Infant;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

/**
 * Entity representing a vaccination record for an infant
 */
@Entity
@Table(name = "vaccination")
@Getter
@Setter
public class Vaccination extends BaseEntity {

    @ManyToOne
    private Infant infant;

    @ManyToOne
    private HealthWorker healthWorker;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date administeredDate;

    @Temporal(TemporalType.DATE)
    private Date nextDueDate;

    @Column(nullable = false)
    private boolean notificationSent = false;

    private String notes;
}