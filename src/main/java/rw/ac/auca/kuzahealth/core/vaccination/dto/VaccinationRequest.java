package rw.ac.auca.kuzahealth.core.vaccination.dto;

import java.util.Date;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Vaccination requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationRequest {

    @NotNull
    private UUID infantId;

    @NotNull
    private UUID healthWorkerId;

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Date administeredDate;

    private Date nextDueDate;
    private String notes;
}
