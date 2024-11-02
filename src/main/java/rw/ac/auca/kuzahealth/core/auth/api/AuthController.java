package rw.ac.auca.kuzahealth.core.auth.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import rw.ac.auca.kuzahealth.core.auth.dto.LoginRequest;

@RestController
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        // Logic for authenticating the user
        return ResponseEntity.ok("Logged in successfully");
    }
}
