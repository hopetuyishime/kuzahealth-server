package rw.ac.auca.kuzahealth.core.healthworker.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.visit.entity.Visit;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

@Entity
@Getter
@Setter
@Table(name = "health_worker")
public class HealthWorker extends BaseEntity {

    // public int organization_id FK
    public String first_name;
    public String last_name;
    @Column(unique = true, nullable = false)
    private String email;
    public String phone_number;
    public String qualification;
    public String service_area;

    // Inside HealthWorker class
    @OneToMany(mappedBy = "healthWorker")
    @JsonManagedReference
    private List<Visit> visits;

}
