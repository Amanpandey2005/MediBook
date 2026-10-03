package com.medbook.service;

import com.medbook.entity.Doctor;
import com.medbook.entity.User;
import com.medbook.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public Doctor createDoctor(User user, String specialization, String licenseNumber, 
                              String bio, Integer experienceYears) {
        Doctor doctor = new Doctor(user, specialization, licenseNumber, bio, experienceYears);
        return doctorRepository.save(doctor);
    }

    public Optional<Doctor> findByUserEmail(String email) {
        return doctorRepository.findByUserEmail(email);
    }

    public Doctor findById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public List<Doctor> findBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization);
    }

    public List<Doctor> searchDoctors(String keyword) {
        return doctorRepository.searchDoctors(keyword);
    }

    public List<String> getAllSpecializations() {
        return doctorRepository.findAllSpecializations();
    }

    public Doctor updateDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }
}