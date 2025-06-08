package rw.ac.auca.kuzahealth.core.user.entity;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
public class User extends BaseEntity implements UserDetails {
    // @Id
    // @GeneratedValue(strategy= GenerationType.IDENTITY)
    // @Column(updatable = false, nullable = false)
    // private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(unique = true, nullable = true)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private EUserType role;

    private boolean enabled = true;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(length = 512) // Allow enough space for a token
    private String otp;

    @Column(nullable = true)
    private long otpExpirationTime;

    @Column(nullable = true)
    private String resetToken;
    
    @Column(nullable = true)
    private Long resetTokenExpiration;

    // Protected constructor for JPA
    public User() {
    }

    // Private constructor for factory method
    public User(String firstName, String lastName, String email,
            String password, EUserType role, String phoneNumber, String username, long otpExpirationTime) {
        // this.id = UUID.randomUUID();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.otpExpirationTime = otpExpirationTime;
    }

    // Factory method - main way to create a User
    public static User create(String firstName, String lastName,
            String email, String password,
            EUserType role, String phoneNumber, String username, long otpExpirationTime) {
        return new User(
                firstName,
                lastName,
                email,
                password,
                role,
                phoneNumber, username, otpExpirationTime);
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // Implement according to your roles/authorities
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
