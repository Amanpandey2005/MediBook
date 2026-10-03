package com.medbook.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "doctors")
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Specialization is required")
    @Size(max = 100)
    private String specialization;

    @NotBlank(message = "License number is required")
    @Size(max = 50)
    @Column(unique = true)
    private String licenseNumber;

    @Size(max = 1000)
    private String bio;

    private Integer experienceYears;

    @Size(max = 100)
    private String clinic;

    @ElementCollection
    @CollectionTable(name = "doctor_working_hours", joinColumns = @JoinColumn(name = "doctor_id"))
    @Column(name = "working_hour")
    private List<String> workingHours;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Medication> prescribedMedications;

    // Constructors
    public Doctor() {}

    public Doctor(User user, String specialization, String licenseNumber, String bio, Integer experienceYears) {
        this.user = user;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.bio = bio;
        this.experienceYears = experienceYears;
    }

    public Doctor(User user, String specialization, String licenseNumber, String bio, Integer experienceYears, String clinic) {
        this.user = user;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.bio = bio;
        this.experienceYears = experienceYears;
        this.clinic = clinic;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getClinic() {
        return clinic;
    }

    public void setClinic(String clinic) {
        this.clinic = clinic;
    }

    public List<String> getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(List<String> workingHours) {
        this.workingHours = workingHours;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    public List<Medication> getPrescribedMedications() {
        return prescribedMedications;
    }

    public void setPrescribedMedications(List<Medication> prescribedMedications) {
        this.prescribedMedications = prescribedMedications;
    }

    // Convenience method to get full name
    public String getFullName() {
        if (user != null) {
            return user.getFirstName() + " " + user.getLastName();
        }
        return "";
    }
}