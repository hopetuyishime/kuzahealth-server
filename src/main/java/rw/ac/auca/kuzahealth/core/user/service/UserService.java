package rw.ac.auca.kuzahealth.core.user.service;

import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import rw.ac.auca.kuzahealth.controller.auth.dto.EmailRequest;
import rw.ac.auca.kuzahealth.controller.auth.dto.LoginResponse;
import rw.ac.auca.kuzahealth.controller.auth.dto.OtpResponse;
import rw.ac.auca.kuzahealth.core.user.entity.User;
import rw.ac.auca.kuzahealth.core.user.enums.EUserType;
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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserService.class);

    public User registerUser(User user) {
        logger.info("Registering user with email: {}", user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        logger.info("Role being saved: {}", user.getRole());

        EUserType role = user.getRole() != null ? user.getRole() : EUserType.HEALTH_WORKER;
        User userData = User.create(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                hashedPassword,
                user.getPhoneNumber(),
                user.getUsername(),
                user.getOtpExpirationTime(),
                user.getProvince(),
                user.getDistrict(),
                user.getSector(),
                user.getDate_of_Birth(),
                user.getPosition(),
                user.getGender(),
                role
                );

        logger.info("Registering user: " + userData.getEmail());

        return userRepository.save(userData);
    }

    public String verify(User user, String password) {
        logger.info("User Data: {} {}", user.getEmail(), user.getPassword());

        Optional<User> existingUserOpt = userRepository.findByEmail(user.getEmail());
        if (existingUserOpt.isEmpty()) {
            logger.error("User with email {} does not exist.", user.getEmail());
            return "User with email " + user.getEmail() + " does not exist.";
        }

        User existingUser = existingUserOpt.get();

        Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), password));

        logger.info("Authentication: {}", authenticate);

        if (authenticate.isAuthenticated()) {
            logger.info("Authenticated user: {}", user.getEmail());
            return jwtService.generateToken(existingUser); // <-- FIXED
        }

        return "Authentication failed";
    }

    public OtpResponse sendOtp(EmailRequest userRequest) throws Exception {
        Optional<User> existingUserOpt = userRepository.findByEmail(userRequest.getEmail());

        if (existingUserOpt.isEmpty()) {
            return new OtpResponse("ERROR",
                    "User with email " + userRequest.getEmail() + " does not exist. Please register first.");
        }

        User existingUser = existingUserOpt.get();

        if (!passwordEncoder.matches(userRequest.getPassword(), existingUser.getPassword())) {
            return new OtpResponse("ERROR", "Invalid credentials. Please try again.");
        }

        String otp = generateOtp();
        existingUser.setOtp(otp);
        existingUser.setOtpExpirationTime(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(20));
        userRepository.save(existingUser);

        sendOtpViaEmail(existingUser.getEmail(), otp);
        return new OtpResponse("SUCCESS", "OTP sent successfully. Please check your email.");
    }

    // Method to verify OTP and log the user in
    public LoginResponse verifyOtpAndLogin(User user, String otp) throws Exception {
        // Fetch user from the database
        User storedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the OTP matches and is not expired
        if (storedUser.getOtp().equals(otp) && System.currentTimeMillis() < storedUser.getOtpExpirationTime()) {
            // OTP is valid, proceed with login (e.g., generate JWT token)
            String jwtToken = verify(storedUser, user.getPassword()); // Implement this method to generate JWT

            // Optionally, you could return the JWT token to the user for subsequent
            // requests
            System.out.println("Login successful. JWT Token: " + jwtToken);
            LoginResponse loginResponse = LoginResponse.builder()
                    .token(jwtToken)
                    .email(storedUser.getEmail())
                    .userType(storedUser.getRole().name())
                    .message("Login successful")
                    .build();
            return loginResponse;
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
    private String sendOtpViaEmail(String email, String otp) {
        final String fromEmail = "tuyishimekyrie@gmail.com"; // Replace with your email
        final String password = "zcyttvobemuzrpar"; // Use app password or token if needed

        // Setup mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP server
        props.put("mail.smtp.port", "587");

        // Create session
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Create email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp);

            // Send email
            Transport.send(message);
            System.out.println("OTP email sent successfully to " + email);
            return "OTP sent successfully to " + email;
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send OTP email.");
            return "Failed to send OTP email: " + e.getMessage();
        }

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
