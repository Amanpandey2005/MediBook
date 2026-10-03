package com.medbook.service;

import com.medbook.entity.User;
import com.medbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.medbook.service.EmailService;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Validate password confirmation if confirmPassword is provided
        if (user.getConfirmPassword() != null && !user.getConfirmPassword().isEmpty()) {
            if (!user.getPassword().equals(user.getConfirmPassword())) {
                throw new RuntimeException("Passwords do not match");
            }
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(User.Role.PATIENT);
        }
        
        try {
            User registeredUser = userRepository.save(user);
            System.out.println("User registered successfully: " + registeredUser.getEmail());
            
            // Send welcome email
            try {
                emailService.sendWelcomeEmail(registeredUser);
            } catch (Exception e) {
                System.err.println("Failed to send welcome email, but user was registered: " + e.getMessage());
                // Don't fail registration if email fails
            }
            
            return registeredUser;
        } catch (Exception e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> findDoctorsBySpecialization(String specialization) {
        return userRepository.findDoctorsBySpecialization(specialization);
    }

    public List<User> findByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}