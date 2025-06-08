package rw.ac.auca.kuzahealth.controller.auth.api;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.user.entity.User;
import rw.ac.auca.kuzahealth.core.user.service.UserService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> saveUser(@RequestBody User user) {
        try {
            userService.registerUser(user);
            return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Step 1: Generate OTP and send it to the user via email
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestBody User user) {
        try {
            userService.sendOtp(user);
            return new ResponseEntity<>("OTP sent successfully", HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Step 2: Verify OTP and log in the user if OTP is correct
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, @RequestParam String otp) {
        try {
            String jwt = userService.verifyOtpAndLogin(user, otp);
            return new ResponseEntity<>(jwt, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/reset-password-request")
    public ResponseEntity<String> resetPasswordRequest(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            userService.initiatePasswordReset(email);
            return ResponseEntity.ok("Password reset link has been sent to your email.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
    String token = request.get("token");
    String newPassword = request.get("password");
    try {
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password has been reset successfully.");
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}

}
