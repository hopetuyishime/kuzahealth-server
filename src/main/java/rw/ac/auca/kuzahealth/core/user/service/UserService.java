package rw.ac.auca.kuzahealth.core.user.service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.core.user.entity.User;
import rw.ac.auca.kuzahealth.core.user.repository.UserRepository;
import rw.ac.auca.kuzahealth.security.JwtService;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;
    // private final MailSender mailSender;
    // private final BCryptPasswordEncoder passwordEncoder = new
    // BCryptPasswordEncoder();

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

    public User registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());

        User userData = User.create(user.getFirstName(), user.getLastName(), user.getEmail(), hashedPassword,
                user.getRole(), user.getPhoneNumber(), user.getUsername(),user.getOtpExpirationTime());

        return userRepository.save(userData);
    }

    public String verify(User user) {
        logger.info("User Data:" + user.getEmail() + user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        logger.info("Authentication: " + authenticate);
        if (authenticate.isAuthenticated()) {
            logger.info("Authenticated user: {}", user.getEmail());
            return jwtService.generateToken(user);
        }
        return "Authentication failed";
    }

    public void sendOtp(User user) throws Exception {
        // Check if the user already exists
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            // Use the existing user's record to resend OTP
            User existing = existingUser.get();

            // Generate and set a new OTP
            String otp = generateOtp();
            existing.setOtp(otp);
            long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
            logger.info("otp"+expirationTime);
            existing.setOtpExpirationTime(expirationTime);
            // existing.setOtpExpirationTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5)); 
            userRepository.save(existing);

            // Resend the OTP to the user's email
            sendOtpViaEmail(existing.getEmail(), otp);
        } else {
            throw new RuntimeException(
                    "User with email " + user.getEmail() + " does not exist. Please register first.");
        }
    }

    // Method to verify OTP and log the user in
    public String verifyOtpAndLogin(User user, String otp) throws Exception {
        // Fetch user from the database
        User storedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the OTP matches and is not expired
        if (storedUser.getOtp().equals(otp) && System.currentTimeMillis() < storedUser.getOtpExpirationTime()) {
            // OTP is valid, proceed with login (e.g., generate JWT token)
            String jwtToken = verify(user); // Implement this method to generate JWT

            // Optionally, you could return the JWT token to the user for subsequent
            // requests
            System.out.println("Login successful. JWT Token: " + jwtToken);
            return jwtToken;
        } else {
            throw new RuntimeException("Invalid or expired OTP");
        }
        // return "null";
    }

    // Helper method to generate a 6-digit OTP
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Generate a 6-digit OTP
        return String.valueOf(otp);
    }

    // Dummy method to send OTP via email (implement as needed)
    private void sendOtpViaEmail(String email, String otp) {
        // Use JavaMailSender or any third-party email service to send the OTP
        System.out.println("Sending OTP to email: " + email);
        System.out.println("OTP: " + otp);
    }

    // Dummy method to generate JWT (implement as needed)
    private String generateJwtToken(User user) {
        // You can use JWT utility methods here to generate and return a JWT token
        return "dummy-jwt-token";
    }

    public String getUserToken(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        return user.getOtp();
    }

    public boolean validateToken(String token, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found."));
        return token.equals(user.getOtp());
    }


    public void initiatePasswordReset(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User not found"));

        // Generate a reset token
        String token = UUID.randomUUID().toString();
        long expirationTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15); // 15 minutes validity

        // Store token and expiration time in the database
        user.setResetToken(token);
        user.setResetTokenExpiration(expirationTime);
        userRepository.save(user);

        // Send token to user's email
        sendResetEmail(email, token);
    }

    private void sendResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset Your Password");
        message.setText("Click the link to reset your password: http://example.com/reset-password?token=" + token);
        mailSender.send(message);
    }

    public void resetPassword(String token, String newPassword) throws Exception {
    User user = userRepository.findByResetToken(token)
            .orElseThrow(() -> new Exception("Invalid or expired reset token"));

    if (user.getResetTokenExpiration() < System.currentTimeMillis()) {
        throw new Exception("Reset token has expired");
    }

    // Update password
    user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
    user.setResetToken(null); // Clear the token
    user.setResetTokenExpiration(null);
    userRepository.save(user);
}

}
