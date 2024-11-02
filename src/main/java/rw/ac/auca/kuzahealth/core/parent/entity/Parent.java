package rw.ac.auca.kuzahealth.core.parent.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.pregancyrecord.entity.PregnancyRecord;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

@Table(name="parent")
@Getter
@Setter
@Entity
public class Parent extends BaseEntity {


    public String first_name;
    public String last_name;
    public String email;
    public String phone;
    public String address;
    public Date expected_delivery_date;
    public boolean is_high_risk;

    @OneToMany
    private List<PregnancyRecord> pregancyrecord;
    
    
}
