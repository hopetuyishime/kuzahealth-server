package rw.ac.auca.kuzahealth.core.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import rw.ac.auca.kuzahealth.core.user.entity.User;
import rw.ac.auca.kuzahealth.core.user.enums.EUserType;
import rw.ac.auca.kuzahealth.core.user.repository.UserRepository;


@Service
public class UserService {
     @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(String firstName, String lastName, String email, String password, EUserType userType, String phoneNumber) {
        // Check if the email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with this email already exists.");
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);

        // Create a new User instance
        User user = User.create(firstName, lastName, email, hashedPassword, userType, phoneNumber);
        
        // Save the user to the database
        return userRepository.save(user);
    }
}

