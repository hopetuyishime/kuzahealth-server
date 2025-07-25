package rw.ac.auca.kuzahealth.controller.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rw.ac.auca.kuzahealth.core.healthworker.entity.HealthWorker;
import rw.ac.auca.kuzahealth.core.healthworker.service.HealthWorkerService;
import rw.ac.auca.kuzahealth.core.user.entity.User;
import rw.ac.auca.kuzahealth.core.user.enums.EUserType;
import rw.ac.auca.kuzahealth.core.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HealthWorkerService healthWorkerService;

    public ResponseEntity<?> registerUser(@RequestBody User user) {
        logger.info("Registering user: {}", user.getFirstName());
        logger.info("User role from request: {}", user.getRole());

        try {
            User registeredUser = userService.registerUser(user);
            logger.info("Registered user role: {}", registeredUser.getRole());

            // If the user has the HEALTH_WORKER role, also create a HealthWorker entity
            if (registeredUser.getRole() == EUserType.HEALTH_WORKER) {
                logger.info("Creating HealthWorker entity for user with HEALTH_WORKER role");

                HealthWorker healthWorker = new HealthWorker();
                healthWorker.setFirst_name(registeredUser.getFirstName());
                healthWorker.setLast_name(registeredUser.getLastName());
                healthWorker.setEmail(registeredUser.getEmail());
                healthWorker.setPhone_number(registeredUser.getPhoneNumber());

                // Set the reference to the User entity
                healthWorker.setUser(registeredUser);

                // Save the HealthWorker entity
                try {
                    HealthWorker createdHealthWorker = healthWorkerService.createHealthWorker(healthWorker);
                    logger.info("HealthWorker created successfully: {}", createdHealthWorker.getId());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body("User created successfully and registered as a Health Worker.");
                } catch (Exception e) {
                    logger.error("Error creating HealthWorker: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body("User created successfully but failed to register as a Health Worker: " + e.getMessage());
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully.");
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
