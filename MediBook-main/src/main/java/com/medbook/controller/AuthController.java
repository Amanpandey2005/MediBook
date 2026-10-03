package com.medbook.controller;

import com.medbook.entity.User;
import com.medbook.service.EmailService;
import com.medbook.service.UserService;
import com.medbook.service.DoctorService;
import com.medbook.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showregister(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> System.err.println("Validation Error: " + error.getDefaultMessage()));
            model.addAttribute("error", "Registration failed: Please correct the errors below.");
            return "register";
        }

        if (!user.getPassword().equals(user.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match!");
            return "register";
        }

        try {
            System.out.println("Attempting to register user: " + user.getEmail());
            userService.registerUser(user);
            model.addAttribute("success", "Registration successful! Please check your email for a welcome message.");
            return "redirect:/login?registered=true";
        } catch (RuntimeException e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }
        return "redirect:/";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        switch (user.getRole()) {
            case PATIENT:
                return "redirect:/patient/dashboard?loggedIn=true";
            case DOCTOR:
                return "redirect:/doctor/dashboard?loggedIn=true";
            case ADMIN:
                return "redirect:/admin/dashboard?loggedIn=true";
            default:
                return "redirect:/";
        }
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            System.out.println("=== AUTH CONTROLLER DEBUG ===");
            System.out.println("Received forgot password request for email: " + email);
            
            boolean success = passwordResetService.requestPasswordReset(email);
            
            if (success) {
                System.out.println("Password reset request processed successfully");
                model.addAttribute("success", "If an account with that email exists, we have sent a password reset link.");
            } else {
                System.out.println("Password reset request failed");
                model.addAttribute("error", "An error occurred while processing your request. Please try again.");
            }
            
            System.out.println("=== AUTH CONTROLLER DEBUG END ===");
        } catch (Exception e) {
            System.err.println("❌ Exception in AuthController.processForgotPassword: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "An error occurred while processing your request. Please try again.");
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        if (!passwordResetService.validateToken(token)) {
            model.addAttribute("error", "Invalid or expired reset token.");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @GetMapping("/test-email")
    public String testEmail() {
        try {
            System.out.println("=== TEST EMAIL DEBUG ===");
            System.out.println("Testing email configuration...");
            
            // Create a test user
            User testUser = new User();
            testUser.setEmail("test@example.com");
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            
            // Test email sending
            emailService.sendPasswordResetEmail(testUser, "test-token", "http://localhost:8080/reset-password?token=test-token");
            
            System.out.println("✅ Test email sent successfully");
            System.out.println("=== TEST EMAIL DEBUG END ===");
            return "redirect:/login?test=email_sent";
        } catch (Exception e) {
            System.err.println("❌ Test email failed: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?test=email_failed";
        }
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                     @RequestParam("password") String password,
                                     @RequestParam("confirmPassword") String confirmPassword,
                                     Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (passwordResetService.resetPassword(token, password)) {
            return "redirect:/login?reset=true";
        } else {
            model.addAttribute("error", "Invalid or expired reset token.");
            return "reset-password";
        }
    }
}