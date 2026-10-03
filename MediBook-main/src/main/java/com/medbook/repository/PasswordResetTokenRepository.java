package com.medbook.repository;

import com.medbook.entity.PasswordResetToken;
import com.medbook.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    
    Optional<PasswordResetToken> findByToken(String token);
    
    Optional<PasswordResetToken> findByUserAndUsedFalse(User user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiryDate < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetToken p SET p.used = true WHERE p.user = :user AND p.used = false")
    void markAllTokensAsUsedForUser(@Param("user") User user);
}



