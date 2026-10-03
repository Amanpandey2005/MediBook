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

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private MedicationService medicationService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        Doctor doctor = doctorService.findByUserEmail(email).orElse(null);
        if (doctor == null) {
            return "redirect:/login";
        }

        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctor);
        List<Medication> prescribedMedications = medicationService.getDoctorPrescribedMedications(doctor);

        model.addAttribute("doctor", doctor);
        model.addAttribute("appointments", appointments);
        model.addAttribute("prescribedMedications", prescribedMedications);

        return "doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctor doctor = doctorService.findByUserEmail(email).orElse(null);

        if (doctor == null) {
            return "redirect:/login";
        }

        List<Appointment> appointments = appointmentService.getDoctorAppointments(doctor);
        model.addAttribute("appointments", appointments);

        return "doctor/appointments";
    }

    @GetMapping("/appointment/{id}")
    public String viewAppointment(@PathVariable Long id, Model model) {
        try {
            Appointment appointment = appointmentService.findById(id);
            model.addAttribute("appointment", appointment);
            model.addAttribute("medication", new Medication());
            return "doctor/appointment-detail";
        } catch (RuntimeException e) {
            model.addAttribute("error", "Appointment not found: " + e.getMessage());
            return "redirect:/doctor/appointments";
        }
    }

    @PostMapping("/appointment/{id}/complete")
    public String completeAppointment(@PathVariable Long id, @RequestParam String notes) {
        Appointment appointment = appointmentService.findById(id);
        appointment.setNotes(notes);
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointmentService.updateAppointment(appointment);
        return "redirect:/doctor/appointments";
    }

    @PostMapping("/prescribe-medication")
    public String prescribeMedication(@ModelAttribute Medication medication,
                                    @RequestParam Long appointmentId,
                                    @RequestParam String instructions,
                                    @RequestParam String sideEffects) {
        try {
            Appointment appointment = appointmentService.findById(appointmentId);
            Doctor doctor = doctorService.findByUserEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
            
            if (doctor == null) {
                return "redirect:/doctor/dashboard";
            }

            medicationService.prescribeMedication(
                appointment, doctor, appointment.getPatient(),
                medication.getMedicationName(), medication.getDosage(),
                medication.getFrequency(), medication.getDurationDays(),
                instructions, sideEffects
            );

            return "redirect:/doctor/appointment/" + appointmentId + "?success=true";
        } catch (RuntimeException e) {
            return "redirect:/doctor/appointment/" + appointmentId + "?error=" + e.getMessage();
        }
    }

    @GetMapping("/medications")
    public String medications(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Doctor doctor = doctorService.findByUserEmail(email).orElse(null);

        if (doctor == null) {
            return "redirect:/login";
        }

        List<Medication> medications = medicationService.getDoctorPrescribedMedications(doctor);
        model.addAttribute("medications", medications);

        return "doctor/medications";
    }

    @PostMapping("/medication/{id}/complete")
    public String completeMedication(@PathVariable Long id) {
        medicationService.completeMedication(id);
        return "redirect:/doctor/medications";
    }

    @PostMapping("/medication/{id}/cancel")
    public String cancelMedication(@PathVariable Long id) {
        medicationService.cancelMedication(id);
        return "redirect:/doctor/medications";
    }
}