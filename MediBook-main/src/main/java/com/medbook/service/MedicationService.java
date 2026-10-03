package com.medbook.service;

import com.medbook.entity.Appointment;
import com.medbook.entity.Doctor;
import com.medbook.entity.Medication;
import com.medbook.entity.User;
import com.medbook.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    public Medication prescribeMedication(Appointment appointment, Doctor doctor, User patient,
                                        String medicationName, String dosage, String frequency,
                                        Integer durationDays, String instructions, String sideEffects) {
        Medication medication = new Medication(appointment, doctor, patient, medicationName, 
                                             dosage, frequency, durationDays);
        medication.setInstructions(instructions);
        medication.setSideEffects(sideEffects);
        
        return medicationRepository.save(medication);
    }

    public List<Medication> getPatientMedications(User patient) {
        return medicationRepository.findByPatientOrderByPrescribedAtDesc(patient);
    }

    public List<Medication> getPatientActiveMedications(User patient) {
        return medicationRepository.findByPatientAndStatus(patient, Medication.MedicationStatus.ACTIVE);
    }

    public List<Medication> getDoctorPrescribedMedications(Doctor doctor) {
        return medicationRepository.findByDoctorOrderByPrescribedAtDesc(doctor);
    }

    public Medication findById(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
    }

    public Medication updateMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    public void completeMedication(Long medicationId) {
        Medication medication = findById(medicationId);
        medication.setStatus(Medication.MedicationStatus.COMPLETED);
        medicationRepository.save(medication);
    }

    public void cancelMedication(Long medicationId) {
        Medication medication = findById(medicationId);
        medication.setStatus(Medication.MedicationStatus.CANCELLED);
        medicationRepository.save(medication);
    }
}