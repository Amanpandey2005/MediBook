package com.medbook.repository;

import com.medbook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<User> findByRole(User.Role role);
    
    @Query("SELECT u FROM User u WHERE u.role = 'DOCTOR' AND u.id IN " +
           "(SELECT d.user.id FROM Doctor d WHERE d.specialization = :specialization)")
    List<User> findDoctorsBySpecialization(@Param("specialization") String specialization);
}