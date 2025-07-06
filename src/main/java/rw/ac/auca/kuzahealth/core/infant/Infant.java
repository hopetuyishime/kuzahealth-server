package rw.ac.auca.kuzahealth.core.infant;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.core.vaccination.entity.Vaccination;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

@Table(name = "infant")
@Entity
@Getter
@Setter
public class Infant extends BaseEntity {

    private String firstName;
    private String lastName;

    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    private String gender;

    @Column(name = "birth_weight_grams")
    private Double birthWeight;

    @Column(name = "birth_height_cm")
    private Double birthHeight;

    private String bloodGroup;

    @Column(name = "special_conditions")
    private String specialConditions;

    @ManyToOne
    private Parent mother;

    @OneToMany(mappedBy = "infant")
    @JsonIgnore
    private List<Vaccination> vaccinations;
}
