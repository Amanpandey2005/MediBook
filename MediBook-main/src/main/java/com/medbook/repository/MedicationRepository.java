package com.medbook.repository;

import com.medbook.entity.Medication;
import com.medbook.entity.User;
import com.medbook.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    List<Medication> findByPatient(User patient);
    
    List<Medication> findByDoctor(Doctor doctor);
    
    List<Medication> findByPatientAndStatus(User patient, Medication.MedicationStatus status);
    
    @Query("SELECT m FROM Medication m WHERE m.patient = :patient ORDER BY m.prescribedAt DESC")
    List<Medication> findByPatientOrderByPrescribedAtDesc(@Param("patient") User patient);
    
    @Query("SELECT m FROM Medication m WHERE m.doctor = :doctor ORDER BY m.prescribedAt DESC")
    List<Medication> findByDoctorOrderByPrescribedAtDesc(@Param("doctor") Doctor doctor);
}