package com.medbook.repository;

import com.medbook.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserEmail(String email);
    
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    
    List<Doctor> findBySpecialization(String specialization);
    
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.user.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.licenseNumber) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Doctor> searchDoctors(@Param("keyword") String keyword);
    
    @Query("SELECT DISTINCT d.specialization FROM Doctor d")
    List<String> findAllSpecializations();
}