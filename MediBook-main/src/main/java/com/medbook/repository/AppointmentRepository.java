package com.medbook.repository;

import com.medbook.entity.Appointment;
import com.medbook.entity.Doctor;
import com.medbook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient(User patient);
    
    List<Appointment> findByDoctor(Doctor doctor);
    
    List<Appointment> findByPatientAndStatus(User patient, Appointment.AppointmentStatus status);
    
    List<Appointment> findByDoctorAndStatus(Doctor doctor, Appointment.AppointmentStatus status);
    
    @Query("SELECT a FROM Appointment a WHERE a.doctor = :doctor AND a.appointmentDateTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorAndDateRange(@Param("doctor") Doctor doctor, 
                                               @Param("start") LocalDateTime start, 
                                               @Param("end") LocalDateTime end);
    
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :start AND :end")
    List<Appointment> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    boolean existsByDoctorAndAppointmentDateTime(Doctor doctor, LocalDateTime appointmentDateTime);
}