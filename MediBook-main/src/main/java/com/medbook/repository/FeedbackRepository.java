package com.medbook.repository;

import com.medbook.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    List<Feedback> findAllByOrderByCreatedAtDesc();
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.readFlag = false")
    long countUnreadFeedback();
    
    List<Feedback> findByReadFlagFalseOrderByCreatedAtDesc();
}
