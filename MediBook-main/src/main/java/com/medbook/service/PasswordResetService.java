package com.medbook.service;

import com.medbook.entity.PasswordResetToken;
import com.medbook.entity.User;
import com.medbook.repository.PasswordResetTokenRepository;
import com.medbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final SecureRandom secureRandom = new SecureRandom();

    public boolean requestPasswordReset(String email) {
        try {
            System.out.println("=== PASSWORD RESET REQUEST DEBUG ===");
            System.out.println("Processing password reset request for email: " + email);
            
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                System.out.println("No user found with email: " + email);
                // Don't reveal if email exists or not for security
                return true;
            }

            User user = userOpt.get();
            System.out.println("User found: " + user.getFullName() + " (" + user.getEmail() + ")");
            
            // Mark any existing tokens as used
            tokenRepository.markAllTokensAsUsedForUser(user);
            System.out.println("Marked existing tokens as used for user");
            
            // Generate new token
            String token = generateSecureToken();
            System.out.println("Generated new reset token");
            
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            tokenRepository.save(resetToken);
            System.out.println("Saved new reset token to database");
            
            // Send email
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            System.out.println("Sending password reset email with URL: " + resetUrl);
            emailService.sendPasswordResetEmail(user, token, resetUrl);
            
            System.out.println("✅ Password reset request processed successfully");
            System.out.println("=== PASSWORD RESET REQUEST DEBUG END ===");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error processing password reset request for " + email + ": " + e.getMessage());
            System.err.println("Error details: " + e.getClass().getSimpleName());
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        if (!resetToken.isValid()) {
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        // Send confirmation email
        emailService.sendPasswordResetConfirmation(user);

        return true;
    }

    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenOpt.get();
        return resetToken.isValid();
    }

    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}



