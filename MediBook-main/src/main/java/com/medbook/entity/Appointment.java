package com.medbook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    @NotNull(message = "Patient is required")
    private User patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @NotNull(message = "Doctor is required")
    private Doctor doctor;

    @NotNull(message = "Appointment date and time is required")
    @Future(message = "Appointment must be in the future")
    private LocalDateTime appointmentDateTime;

    @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    @NotBlank(message = "Reason for visit is required")
    @Column(length = 1000)
    private String reasonForVisit;

    @Column(length = 2000)
    private String notes;

    @Column(length = 1000)
    private String symptoms;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructors
    public Appointment() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = AppointmentStatus.SCHEDULED;
    }

    public Appointment(User patient, Doctor doctor, LocalDateTime appointmentDateTime, String reasonForVisit) {
        this();
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDateTime = appointmentDateTime;
        this.reasonForVisit = reasonForVisit;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public enum AppointmentStatus {
        SCHEDULED, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}