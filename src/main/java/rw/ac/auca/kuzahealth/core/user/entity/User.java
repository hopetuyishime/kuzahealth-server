package rw.ac.auca.kuzahealth.core.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import rw.ac.auca.kuzahealth.core.user.enums.EUserType;
import rw.ac.auca.kuzahealth.utils.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {
//    @Id
//     @GeneratedValue(strategy= GenerationType.IDENTITY)
//     @Column(updatable = false, nullable = false)
//     private UUID id;

    
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;


    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EUserType userType;

    private boolean enabled = true;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Protected constructor for JPA
    public User() {
    }

    // Private constructor for factory method
    public User(String firstName, String lastName, String email,
            String password, EUserType userType, String phoneNumber) {
        // this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.phoneNumber = phoneNumber;
    }

    // Factory method - main way to create a User
    public static User create(String firstName, String lastName,
            String email, String password,
            EUserType userType, String phoneNumber) {
        return new User(
                firstName,
                lastName,
                email,
                password, // Note: password should be encrypted before reaching here
                userType,
                phoneNumber);
    }

    // Domain methods
    public void updateProfile(String firstName, String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public void disable() {
        this.enabled = false;
    }

    public void enable() {
        this.enabled = true;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

}

