package com.medbook.controller;

import com.medbook.entity.Doctor;
import com.medbook.entity.User;
import com.medbook.entity.Feedback;
import com.medbook.repository.UserRepository;
import com.medbook.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(required = false) String search) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User admin = userService.findByEmail(email).orElse(null);

        if (admin == null || !admin.getRole().equals(User.Role.ADMIN)) {
            return "redirect:/login";
        }

        // Get all doctors and patients
        List<Doctor> doctors = doctorService.getAllDoctors();
        List<User> patients = userService.findByRole(User.Role.PATIENT);
        
        // Filter doctors by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            doctors = doctors.stream()
                .filter(doctor -> 
                    doctor.getUser().getFirstName().toLowerCase().contains(searchLower) ||
                    doctor.getUser().getLastName().toLowerCase().contains(searchLower) ||
                    doctor.getSpecialization().toLowerCase().contains(searchLower) ||
                    doctor.getLicenseNumber().toLowerCase().contains(searchLower)
                )
                .collect(Collectors.toList());
        }

        // Get unread feedback count
        long unreadCount = feedbackService.getUnreadCount();

        // Create new objects for the form
        User newDoctorUser = new User();
        Doctor newDoctor = new Doctor();
        newDoctorUser.setRole(User.Role.DOCTOR);

        // Available clinics
        List<String> clinics = Arrays.asList(
            "Main Hospital", "Cardiology Center", "Neurology Clinic", 
            "Pediatrics Wing", "Emergency Department", "Surgery Center"
        );

        model.addAttribute("doctors", doctors);
        model.addAttribute("patients", patients);
        model.addAttribute("newDoctorUser", newDoctorUser);
        model.addAttribute("newDoctor", newDoctor);
        model.addAttribute("clinics", clinics);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("search", search);

        return "admin/dashboard";
    }

    @PostMapping("/create-doctor")
    public String createDoctor(@RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String email,
                              @RequestParam String phoneNumber,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              @RequestParam String specialization,
                              @RequestParam String licenseNumber,
                              @RequestParam(required = false) String bio,
                              @RequestParam(required = false) Integer experienceYears,
                              @RequestParam String clinic,
                              RedirectAttributes redirectAttributes) {
        try {
            // Validate passwords match
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match!");
                return "redirect:/admin/dashboard";
            }

            // Create user account
            User doctorUser = new User();
            doctorUser.setFirstName(firstName);
            doctorUser.setLastName(lastName);
            doctorUser.setEmail(email);
            doctorUser.setPhoneNumber(phoneNumber);
            doctorUser.setPassword(password);
            doctorUser.setConfirmPassword(confirmPassword); // Set confirmPassword to pass validation
            doctorUser.setRole(User.Role.DOCTOR);

            User savedUser = userService.registerUser(doctorUser);

            // Create doctor profile
            Doctor doctor = new Doctor(savedUser, specialization, licenseNumber, bio, experienceYears, clinic);
            doctorService.updateDoctor(doctor);

            redirectAttributes.addFlashAttribute("success", "Doctor account created successfully!");
            return "redirect:/admin/dashboard?created=true";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create doctor: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/doctor/{id}/edit")
    public String editDoctor(@PathVariable Long id, Model model) {
        try {
            Doctor doctor = doctorService.findById(id);
            User user = doctor.getUser();

            List<String> clinics = Arrays.asList(
                "Main Hospital", "Cardiology Center", "Neurology Clinic", 
                "Pediatrics Wing", "Emergency Department", "Surgery Center"
            );

            model.addAttribute("doctor", doctor);
            model.addAttribute("user", user);
            model.addAttribute("clinics", clinics);

            return "admin/doctor-edit";
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error=Doctor not found";
        }
    }

    @PostMapping("/doctor/{id}/edit")
    public String updateDoctor(@PathVariable Long id,
                              @RequestParam String firstName,
                              @RequestParam String lastName,
                              @RequestParam String phoneNumber,
                              @RequestParam String specialization,
                              @RequestParam String licenseNumber,
                              @RequestParam(required = false) String bio,
                              @RequestParam(required = false) Integer experienceYears,
                              @RequestParam String clinic,
                              RedirectAttributes redirectAttributes) {
        try {
            Doctor doctor = doctorService.findById(id);
            User user = doctor.getUser();

            // Update user information
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPhoneNumber(phoneNumber);
            userService.updateUser(user);

            // Update doctor information
            doctor.setSpecialization(specialization);
            doctor.setLicenseNumber(licenseNumber);
            doctor.setBio(bio);
            doctor.setExperienceYears(experienceYears);
            doctor.setClinic(clinic);
            doctorService.updateDoctor(doctor);

            redirectAttributes.addFlashAttribute("success", "Doctor updated successfully!");
            return "redirect:/admin/dashboard?updatedDoctor=true";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update doctor: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/doctor/{id}/delete")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            doctorService.deleteDoctor(id);
            redirectAttributes.addFlashAttribute("success", "Doctor deleted successfully!");
            return "redirect:/admin/dashboard?deletedDoctor=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete doctor: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @PostMapping("/patient/{id}/delete")
    public String deletePatient(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Patient deleted successfully!");
            return "redirect:/admin/dashboard?deletedPatient=true";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete patient: " + e.getMessage());
            return "redirect:/admin/dashboard";
        }
    }

    @GetMapping("/feedback")
    public String feedback(Model model) {
        List<Feedback> messages = feedbackService.getAllFeedback();
        model.addAttribute("messages", messages);
        return "admin/feedback";
    }

    @PostMapping("/feedback/{id}/read")
    public String markFeedbackAsRead(@PathVariable Long id) {
        feedbackService.markAsRead(id);
        return "redirect:/admin/feedback";
    }

    @PostMapping("/feedback/{id}/delete")
    public String deleteFeedback(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            feedbackService.deleteFeedback(id);
            redirectAttributes.addFlashAttribute("success", "Feedback deleted successfully!");
            return "redirect:/admin/feedback";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete feedback: " + e.getMessage());
            return "redirect:/admin/feedback";
        }
    }


}
