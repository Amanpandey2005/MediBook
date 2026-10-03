package com.medbook.service;

import com.medbook.entity.Appointment;
import com.medbook.entity.Doctor;
import com.medbook.entity.User;
import com.medbook.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EmailService emailService;

    public Appointment createAppointment(User patient, Doctor doctor, LocalDateTime appointmentDateTime, 
                                       String reasonForVisit, String symptoms) {
        // Validate appointment date and time
        validateAppointmentDateTime(appointmentDateTime);
        
        // Check if the time slot is available
        if (appointmentRepository.existsByDoctorAndAppointmentDateTime(doctor, appointmentDateTime)) {
            throw new RuntimeException("This time slot is already booked. Please choose a different time.");
        }

        Appointment appointment = new Appointment(patient, doctor, appointmentDateTime, reasonForVisit);
        appointment.setSymptoms(symptoms);
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        
        Appointment savedAppointment = appointmentRepository.save(appointment);
        
        // Send confirmation email
        emailService.sendAppointmentConfirmation(savedAppointment);
        
        return savedAppointment;
    }
    
    private void validateAppointmentDateTime(LocalDateTime appointmentDateTime) {
        LocalDateTime now = LocalDateTime.now();
        
        // Check if appointment is in the past
        if (appointmentDateTime.isBefore(now)) {
            throw new RuntimeException("Please select a future date and time for your appointment.");
        }
        
        // Check if appointment is on weekend (Saturday = 6, Sunday = 7)
        int dayOfWeek = appointmentDateTime.getDayOfWeek().getValue();
        if (dayOfWeek == 6 || dayOfWeek == 7) {
            throw new RuntimeException("Appointments are not available on weekends. Please select a weekday (Monday to Friday).");
        }
        
        // Check if appointment is too far in the future (more than 3 months)
        LocalDateTime threeMonthsFromNow = now.plusMonths(3);
        if (appointmentDateTime.isAfter(threeMonthsFromNow)) {
            throw new RuntimeException("Appointments can only be booked up to 3 months in advance.");
        }
        
        // Check if appointment is during business hours (8 AM to 6 PM)
        int hour = appointmentDateTime.getHour();
        if (hour < 8 || hour >= 18) {
            throw new RuntimeException("Appointments are only available between 8:00 AM and 6:00 PM.");
        }
    }

    public List<Appointment> getPatientAppointments(User patient) {
        return appointmentRepository.findByPatient(patient);
    }

    public List<Appointment> getDoctorAppointments(Doctor doctor) {
        return appointmentRepository.findByDoctor(doctor);
    }

    public List<Appointment> getPatientAppointmentsByStatus(User patient, Appointment.AppointmentStatus status) {
        return appointmentRepository.findByPatientAndStatus(patient, status);
    }

    public List<Appointment> getDoctorAppointmentsByStatus(Doctor doctor, Appointment.AppointmentStatus status) {
        return appointmentRepository.findByDoctorAndStatus(doctor, status);
    }

    public Appointment findById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
    }

    public Appointment updateAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = findById(appointmentId);
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    public List<Appointment> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDateRange(start, end);
    }

    public List<Appointment> getDoctorAppointmentsByDateRange(Doctor doctor, LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDoctorAndDateRange(doctor, start, end);
    }
}