package rw.ac.auca.kuzahealth.core.pregancyrecord.entity;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.parent.entity.Parent;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

@Getter
@Setter
@Table(name="pregnancy_record")
@Entity
public class PregnancyRecord extends BaseEntity{


    public String gravity;
    public int parity;
    public Date last_menstrual_period;
    public String medical_history;
    public String  pregnancy_complications;


    @ManyToOne
    private Parent parent;

}
