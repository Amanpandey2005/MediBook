package com.medbook.controller;

import com.medbook.entity.Appointment;
import com.medbook.entity.Doctor;
import com.medbook.entity.Medication;
import com.medbook.entity.User;
import com.medbook.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private AIRecommendationService aiRecommendationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(required = false) String success) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User patient = userService.findByEmail(email).orElse(null);

        if (patient == null) {
            return "redirect:/login";
        }

        List<Appointment> allAppointments = appointmentService.getPatientAppointments(patient);
        List<Medication> medications = medicationService.getPatientActiveMedications(patient);
        
        // Filter out cancelled appointments for the count
        long activeAppointmentsCount = allAppointments.stream()
            .filter(appointment -> appointment.getStatus() != Appointment.AppointmentStatus.CANCELLED)
            .count();

        model.addAttribute("patient", patient);
        model.addAttribute("appointments", allAppointments);
        model.addAttribute("activeAppointmentsCount", activeAppointmentsCount);
        model.addAttribute("medications", medications);
        
        // Handle success messages
        if ("appointment_booked".equals(success)) {
            model.addAttribute("success", "Appointment booked successfully! You will receive a confirmation email shortly.");
        }

        return "patient/dashboard";
    }

    @GetMapping("/book-appointment")
    public String bookAppointment(Model model) {
        List<Doctor> doctors = doctorService.getAllDoctors();
        model.addAttribute("doctors", doctors);
        model.addAttribute("appointment", new Appointment());
        return "patient/book-appointment";
    }

    @PostMapping("/book-appointment")
    public String bookAppointment(@ModelAttribute Appointment appointment,
                                @RequestParam String symptoms,
                                Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User patient = userService.findByEmail(email).orElse(null);

            if (patient == null) {
                return "redirect:/login";
            }

            Doctor doctor = doctorService.findById(appointment.getDoctor().getId());
            appointmentService.createAppointment(patient, doctor, appointment.getAppointmentDateTime(),
                                               appointment.getReasonForVisit(), symptoms);

            return "redirect:/patient/dashboard?success=appointment_booked";
        } catch (RuntimeException e) {
            System.out.println("=== APPOINTMENT BOOKING ERROR ===");
            System.out.println("Error: " + e.getMessage());
            System.out.println("=== APPOINTMENT BOOKING ERROR END ===");
            
            model.addAttribute("error", e.getMessage());
            List<Doctor> doctors = doctorService.getAllDoctors();
            model.addAttribute("doctors", doctors);
            model.addAttribute("appointment", appointment); // Preserve form data
            return "patient/book-appointment";
        } catch (Exception e) {
            System.err.println("❌ Unexpected error booking appointment: " + e.getMessage());
            e.printStackTrace();
            
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            List<Doctor> doctors = doctorService.getAllDoctors();
            model.addAttribute("doctors", doctors);
            model.addAttribute("appointment", appointment); // Preserve form data
            return "patient/book-appointment";
        }
    }

    @GetMapping("/ai-recommendation")
    public String aiRecommendation(Model model) {
        return "patient/ai-recommendation";
    }

    @PostMapping("/ai-recommendation")
    public String getAIRecommendation(@RequestParam String illnessDescription, Model model) {
        List<Doctor> recommendedDoctors = aiRecommendationService.recommendDoctors(illnessDescription);
        String explanation = aiRecommendationService.getRecommendationExplanation(illnessDescription);
        String detailedAdvice = aiRecommendationService.getDetailedMedicalAdvice(illnessDescription);

        model.addAttribute("recommendedDoctors", recommendedDoctors);
        model.addAttribute("explanation", explanation);
        model.addAttribute("detailedAdvice", detailedAdvice);
        model.addAttribute("illnessDescription", illnessDescription);

        return "patient/ai-recommendation";
    }

    @GetMapping("/appointments")
    public String appointments(Model model, @RequestParam(required = false) String error, @RequestParam(required = false) String success) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User patient = userService.findByEmail(email).orElse(null);

        if (patient == null) {
            return "redirect:/login";
        }

        List<Appointment> appointments = appointmentService.getPatientAppointments(patient);
        model.addAttribute("appointments", appointments);
        
        // Handle success messages
        if ("cancelled".equals(success)) {
            model.addAttribute("success", "Appointment cancelled successfully.");
        }
        
        // Handle error messages
        if ("cancel_failed".equals(error)) {
            model.addAttribute("error", "Failed to cancel appointment. Please try again.");
        }

        return "patient/appointments";
    }

    @GetMapping("/medications")
    public String medications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User patient = userService.findByEmail(email).orElse(null);

        if (patient == null) {
            return "redirect:/login";
        }

        List<Medication> medications = medicationService.getPatientMedications(patient);
        model.addAttribute("medications", medications);

        return "patient/medications";
    }

    @PostMapping("/cancel-appointment/{id}")
    public String cancelAppointment(@PathVariable Long id) {
        try {
            System.out.println("=== CANCEL APPOINTMENT DEBUG ===");
            System.out.println("Received cancel appointment request for ID: " + id);
            appointmentService.cancelAppointment(id);
            System.out.println("Appointment " + id + " cancelled successfully");
            System.out.println("=== CANCEL APPOINTMENT DEBUG END ===");
            return "redirect:/patient/appointments?success=cancelled";
        } catch (Exception e) {
            System.err.println("❌ Error cancelling appointment " + id + ": " + e.getMessage());
            e.printStackTrace();
            return "redirect:/patient/appointments?error=cancel_failed";
        }
    }

    @GetMapping("/cancel-appointment/{id}")
    public String cancelAppointmentGet(@PathVariable Long id) {
        System.out.println("=== CANCEL APPOINTMENT GET REQUEST ===");
        System.out.println("GET request received for cancel appointment ID: " + id);
        System.out.println("Redirecting to appointments page");
        System.out.println("=== CANCEL APPOINTMENT GET REQUEST END ===");
        return "redirect:/patient/appointments";
    }
}